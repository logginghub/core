package com.logginghub.logging.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.CountingInputStream;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.Stream;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.logging.Logger;

public class KryoVersion1Decoder {

    private static final Logger logger = Logger.getLoggerFor(KryoVersion1Decoder.class);
    private boolean outputStats;

    public static void main(String[] args) throws IOException {
        File file = new File("D:\\Development\\July2012\\HSBCLogs\\20130308.140000.logdata");
        KryoVersion1Decoder reader = new KryoVersion1Decoder();

        Stream<LogEventBlockElement> blockStream = new Stream<LogEventBlockElement>();
        Stream<LogEvent> destination = new Stream<LogEvent>();

        destination.addListener(new StreamListener<LogEvent>() {
            @Override public void onNewItem(LogEvent t) {
                System.out.println(t);
            }
        });

        reader.readFileInternal(file, blockStream, destination);
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

                ByteBuffer input = ByteBuffer.wrap(inflatedData);
                while (input.position() < input.limit()) {
                    byte nullByte = input.get();
                    if (nullByte == 0) {
                        // Event was null, ignore it
                    }
                    else {

                        DefaultLogEvent event = decodeEvent(input);

                        events.increment();

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
                }
                blockStream.onNewItem(block);

            }
            catch (EOFException eofe) {
                eof = true;
            }
        }

        dis.close();
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

            ByteBuffer input = ByteBuffer.wrap(dataBlock);
            while (input.position() < input.limit()) {
                byte nullByte = input.get();
                if (nullByte == 0) {
                    // Event was null, ignore it
                }
                else {
                    DefaultLogEvent event = decodeEvent(input);
                    destination.onNewItem(event);
                }
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

    private void logProcess(File file, final long length, CountingInputStream cis) {
        if (outputStats) {
            double percentage = 100d * (cis.getCount() / (double) length);
            logger.info("Import '{}' : {} %", file.getName(), NumberFormat.getInstance().format(percentage));
        }
    }

    private DefaultLogEvent decodeEvent(ByteBuffer input) {
        String formattedException = readString(input);
        String[] formattedObject = readArray(input);
        int level = readInt(input, true);
        long timestamp = readLong(input, false);
        String loggerName = readString(input);
        String message = readString(input);
        long sequence = readLong(input, false);
        String sourceAddress = readString(input);
        String sourceApplication = readString(input);
        String sourceClassname = readString(input);
        String sourceHost = readString(input);
        String sourceMethodName = readString(input);
        String threadName = readString(input);

        DefaultLogEvent event = new DefaultLogEvent();
        event.setFormattedException(formattedException);
        event.setFormattedObject(formattedObject);
        event.setLevel(level);
        event.setLocalCreationTimeMillis(timestamp);
        event.setLoggerName(loggerName);
        event.setMessage(message);
        event.setPid(0);
        event.setSequenceNumber(sequence);
        event.setSourceAddress(sourceAddress);
        event.setSourceApplication(sourceApplication);
        event.setSourceClassName(sourceClassname);
        event.setSourceHost(sourceHost);
        event.setSourceMethodName(sourceMethodName);
        event.setThreadName(threadName);
        return event;
    }

    private String[] readArray(ByteBuffer input) {

        byte b = input.get();
        if (b == 0) {
            return null;
        }

        throw new RuntimeException("Not implemented");

        // return null;

    }

    public String readString(ByteBuffer buffer) {
        byte b = buffer.get();
        if (b == 0) {
            return null;
        }

        int charCount = readInt(buffer, true);
        char[] chars = new char[charCount];
        int c, charIndex = 0;
        while (charIndex < charCount) {
            c = buffer.get() & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    chars[charIndex++] = (char) c;
                    break;
                case 12:
                case 13:
                    chars[charIndex++] = (char) ((c & 0x1F) << 6 | buffer.get() & 0x3F);
                    break;
                case 14:
                    chars[charIndex++] = (char) ((c & 0x0F) << 12 | (buffer.get() & 0x3F) << 6 | (buffer.get() & 0x3F) << 0);
                    break;

            }
        }
        return new String(chars, 0, charCount);
    }

    public long readLong(ByteBuffer buffer, boolean optimizePositive) {
        long result = 0;
        for (int offset = 0; offset < 64; offset += 7) {
            byte b = buffer.get();
            result |= (long) (b & 0x7F) << offset;
            if ((b & 0x80) == 0) {
                if (!optimizePositive) {
                    result = (result >>> 1) ^ -(result & 1);
                }
                return result;
            }
        }
        throw new RuntimeException("Malformed long.");
    }

    public int readInt(ByteBuffer buffer, boolean optimizePositive) {
        for (int offset = 0, result = 0; offset < 32; offset += 7) {
            int b = buffer.get();
            result |= (b & 0x7F) << offset;
            if ((b & 0x80) == 0) {
                if (!optimizePositive) {
                    result = (result >>> 1) ^ -(result & 1);
                }
                return result;
            }
        }
        throw new RuntimeException("Malformed integer.");
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

    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }

}
