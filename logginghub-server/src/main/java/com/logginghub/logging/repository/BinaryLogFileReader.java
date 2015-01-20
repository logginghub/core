package com.logginghub.logging.repository;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.esotericsoftware.kryo.io.Input;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.LogEventBlockDataProvider;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.CountingInputStream;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;

public class BinaryLogFileReader {

    private static final Logger logger = Logger.getLoggerFor(BinaryLogFileReader.class);

    private KryoWrapper kryo = new KryoWrapper();

    public static void read(String filename, final LogEventListener listener) throws IOException {
        File file = new File(filename);

        BinaryLogFileReader reader = new BinaryLogFileReader();
        List<LogDataProcessor> listeners = new ArrayList<LogDataProcessor>();
        listeners.add(new LogDataProcessor() {
            @Override public void onNewLogEvent(LogEvent event) {
                listener.onNewLogEvent(event);
            }

            @Override public void processingStarted(File resultsFolder) {}

            @Override public void processingEnded() {}
        });
        reader.readFileInternal(file, null, listeners);

    }

    public void readFile(File file, File resultsFolder, LogDataProcessor... listeners) {
        List<LogDataProcessor> list = new ArrayList<LogDataProcessor>();
        for (LogDataProcessor logDataProcessor : listeners) {
            list.add(logDataProcessor);
        }

        readFile(file, resultsFolder, list);
    }

    public void readFile(File file, File resultsFolder, List<LogDataProcessor> listeners) {
        try {
            readFileInternal(file, resultsFolder, listeners);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to parse binary log events from file '%s'", file.getAbsolutePath()), e);
        }
    }

    @Deprecated// Use the new method with streams and block support in preference
    public void readFileInternal(File file, File resultsFolder, List<LogDataProcessor> listeners) throws IOException {

        final long length = file.length();
        final CountingInputStream cis = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
        DataInputStream dis = new DataInputStream(cis);

        Inflater inflater = new Inflater();

        for (LogDataProcessor listener : listeners) {
            listener.processingStarted(resultsFolder);
        }

        final MutableIntegerValue eventsRead = new MutableIntegerValue("Read", 0);
        Timer everySecond = TimerUtils.everySecond("File reader stats", new Runnable() {
            public void run() {
                System.out.println(String.format("%,d events read/second | %.2f of %.2f MB read | %.1f %%",
                                                 eventsRead.value,
                                                 cis.getCount() / 1024f / 1024f,
                                                 length / 1024f / 1024f,
                                                 100d * (cis.getCount() / (double) length)));
                eventsRead.value = 0;
            }
        });

        boolean eof = false;
        while (!eof) {
            try {
                int deflatedLengthOfNextBlock = dis.readInt();
                int inflatedLengthOfNextBlock = dis.readInt();

                byte[] deflatedData = new byte[deflatedLengthOfNextBlock];
                byte[] inflatedData = new byte[inflatedLengthOfNextBlock];

                dis.readFully(deflatedData);
                inflater.setInput(deflatedData);
                inflater.finished();
                try {
                    inflater.inflate(inflatedData);
                }
                catch (DataFormatException e) {
                    throw new RuntimeException(e);
                }
                inflater.reset();

                Input input = new Input(inflatedData);
                while (input.position() < input.limit()) {
                    DefaultLogEvent logEvent = kryo.readObject(input, DefaultLogEvent.class);
                    eventsRead.increment(1);
                    for (LogEventListener listener : listeners) {
                        listener.onNewLogEvent(logEvent);
                    }
                }

            }
            catch (EOFException eofe) {
                eof = true;
            }
        }

        FileUtils.closeQuietly(dis);
        everySecond.cancel();

        for (LogDataProcessor listener : listeners) {
            listener.processingEnded();
        }
    }
    
    public void readFileInternal(File file, StreamListener<LogEventBlockElement> blockStream, StreamListener<LogEvent> destination)
                    throws IOException {
        readFileInternal(file, blockStream, destination, 0, Integer.MAX_VALUE);
    }

    public void readFileInternal(final File file,
                                 StreamListener<LogEventBlockElement> blockStream,
                                 StreamListener<LogEvent> destination,
                                 int fromRecord,
                                 int toRecord) throws IOException {

        final long length = file.length();

        final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        CountingInputStream cis = new CountingInputStream(bis);
        DataInputStream dis = new DataInputStream(cis);

        StatBundle bundle = new StatBundle();
        IntegerStat events = bundle.createStat("Event/sec");
        events.setIncremental(true);

        Inflater inflater = new Inflater();

        int count = 0;

        boolean eof = false;
        while (!eof) {
            try {
                final LogEventBlockElement block = new LogEventBlockElement();
                block.setStartPosition(cis.getCount());
                byte[] inflatedData = decompressNextBlock(dis, inflater);
                block.setEndPosition(cis.getCount());

                Input input = new Input(inflatedData);
                while (input.position() < input.limit()) {
                    DefaultLogEvent event = kryo.readObject(input, DefaultLogEvent.class);
                    events.increment(1);

                    if (count >= fromRecord && count < toRecord) {
                        destination.onNewItem(event);
                    }
                    count++;

                    if (count > toRecord) {
                        eof = true;
                    }

                    block.update(event);

                    if (count % 100000 == 0) {
                        logProcess(file, length, cis);
                    }

                    // This gives us a way of neatly providing access to individual blocks of
                    // data later on
                    block.setDataProvider(new LogEventBlockDataProvider() {
                        @Override public void provideData(long start, long end, StreamListener<LogEvent> destination) {
                            extractBlock(file, block, destination);
                        }
                    });
                }

                blockStream.onNewItem(block);

            }
            catch (EOFException eofe) {
                eof = true;
            }
        }

        dis.close();
    }

    private void logProcess(File file, final long length, CountingInputStream cis) {
        double percentage = 100d * (cis.getCount() / (double) length);
        logger.info("Import '{}' : {} %", file.getName(), NumberFormat.getInstance().format(percentage));
    }

    protected void extractBlock(File file, LogEventBlockElement block, StreamListener<LogEvent> destination) {

        DataInputStream dis = null;
        Inflater inflater = new Inflater();

        try {
            final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            CountingInputStream cis = new CountingInputStream(bis);
            dis = new DataInputStream(cis);

            dis.skip(block.getStartPosition());

            byte[] dataBlock = decompressNextBlock(dis, inflater);

            Input input = new Input(dataBlock);
            while (input.position() < input.limit()) {
                DefaultLogEvent logEvent = kryo.readObject(input, DefaultLogEvent.class);
                destination.onNewItem(logEvent);
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            FileUtils.closeQuietly(dis);
        }
    }

    private byte[] decompressNextBlock(DataInputStream dis, Inflater inflater) throws IOException {
        int deflatedLengthOfNextBlock = dis.readInt();
        int inflatedLengthOfNextBlock = dis.readInt();

        byte[] deflatedData = new byte[deflatedLengthOfNextBlock];
        byte[] inflatedData = new byte[inflatedLengthOfNextBlock];

        dis.readFully(deflatedData);
        inflater.setInput(deflatedData);
        inflater.finished();
        try {
            inflater.inflate(inflatedData);
        }
        catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
        inflater.reset();
        return inflatedData;
    }

    public List<LogEvent> readAll(File currentFile) {
        final List<LogEvent> events = new ArrayList<LogEvent>();

        readFile(currentFile, null, new LogDataProcessor() {
            public void onNewLogEvent(LogEvent event) {
                events.add(event);
            }

            public void processingStarted(File resultsFolder) {}

            public void processingEnded() {}
        });
        return events;
    }

    public boolean canParse(File input) {
        // Attempt to parse the first block
        boolean canParse;

        LogEventBlockElement block = new LogEventBlockElement();
        block.setStartPosition(0);
        try {
            extractBlock(input, block, new StreamListener<LogEvent>() {
                @Override public void onNewItem(LogEvent t) {}
            });
            canParse = true;
        }
        catch (Throwable e) {
            canParse = false;
        }

        return canParse;
    }

}
