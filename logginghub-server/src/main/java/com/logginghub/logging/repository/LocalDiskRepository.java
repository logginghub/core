package com.logginghub.logging.repository;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.List;
import java.util.Timer;
import java.util.zip.Deflater;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.repository.config.LocalDiskRepositoryConfiguration;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;

public class LocalDiskRepository implements LogEventListener {

    private int bufferSize;

    private long lastEventTime;

    private LocalDiskRepositoryConfiguration configuration;
    private Output buffer;
    private ByteBuffer compressionBuffer;

    private int flushAt;
    private Deflater deflator = new Deflater();

    private DateFormat fileDateFormat = new FileDateFormat();

    private long currentPeriodStart = 0;
    private long currentPeriodEnd = 0;

    private Kryo kryo;
    private File outputFolder;
    private DataOutputStream outputStream;

    private int events = 0;

    private File currentFile;

    private Timer statsTimer;

    private static final Logger logger = Logger.getLoggerFor(LocalDiskRepository.class);

    public LocalDiskRepository(LocalDiskRepositoryConfiguration configuration) {

        this.configuration = configuration;
        setBufferSize(10 * 1024 * 1024);

        this.configuration = configuration;
        kryo = new KryoWrapper();

        outputFolder = new File(configuration.getDataFolder());
        outputFolder.mkdirs();
    }

    private ByteBuffer allocateBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        return buffer;
    }

    private void run() {

        SocketClient socketClient = new SocketClient();
        List<InetSocketAddress> connectionPoints = NetUtils.toInetSocketAddressList(configuration.getHubConnectionString(),
                                                                                    LoggingPorts.getSocketHubDefaultPort());
        for (InetSocketAddress inetSocketAddress : connectionPoints) {
            socketClient.addConnectionPoint(inetSocketAddress);
        }

        socketClient.setAutoSubscribe(true);
        socketClient.addLogEventListener(new LogEventListener() {
            public void onNewLogEvent(LogEvent event) {
                try {
                    process(event);
                }
                catch (Exception e) {
                    System.err.println("Failed to process log event : " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        });

        SocketClientManager clientManager = new SocketClientManager(socketClient);
        clientManager.start();

        //
        // boolean connected = false;
        //
        // while (!connected) {
        // try {
        // socketClient.connect();
        // connected = true;
        // }
        // catch (ConnectorException e) {
        // System.err.println(e.getMessage());
        // ThreadUtils.sleep(1000);
        // }
        // }

        startStatsTimer();
    }

    public void startStatsTimer() {
        statsTimer = TimerUtils.everySecond("StatsTimer", new Runnable() {
            public void run() {
                logger.info("Events processed {} : buffer size {} MB ({}%)",
                            events,
                            buffer.position() / 1024f / 1024f,
                            100f * (buffer.position() / (float) flushAt));
                events = 0;
            }
        });
    }

    public synchronized void process(LogEvent event) {
        events++;
        if (currentPeriodStart == 0) {
            // First entry
            long periodStart = getCurrentPeriodStart();
            openFile(periodStart);
            currentPeriodStart = periodStart;
            currentPeriodEnd = periodStart + configuration.getFileDurationMilliseconds();
        }

        long eventTime = System.currentTimeMillis();
        if (eventTime > currentPeriodEnd) {
            logger.info("Event time '{}' is greater than the current period end time '{}', so rolling the files",
                        Logger.toDateString(eventTime),
                        Logger.toDateString(currentPeriodEnd));
            flushAndCloseCurrentFile();

            long periodStart = getCurrentPeriodStart();
            openFile(periodStart);
            currentPeriodStart = periodStart;
            currentPeriodEnd = periodStart + configuration.getFileDurationMilliseconds();
        }

        // Force the event time. Useful if the source times are out of step.
        if (configuration.getOverrideEventTime()) {
            overrideEventTime(event);
        }

        kryo.writeObject(buffer, event);

        if (buffer.position() > flushAt) {
            flush();
        }

        lastEventTime = eventTime;
    }

    private void overrideEventTime(LogEvent event) {
        DefaultLogEvent mutableEvent = (DefaultLogEvent) event;
        mutableEvent.setLocalCreationTimeMillis(System.currentTimeMillis());
    }

    private long getCurrentPeriodStart() {
        long now = System.currentTimeMillis();
        long periodStart = now - now % configuration.getFileDurationMilliseconds();
        return periodStart;
    }

    public void flushAndCloseCurrentFile() {
        flush();
        closeAndRenameCurrentFile();
    }

    public File closeAndRenameCurrentFile() {
        if (outputStream != null) {
            FileUtils.closeQuietly(outputStream);

            File moveTo = new File(currentFile.getParentFile(), currentFile.getName().substring(0, currentFile.getName().lastIndexOf('.')));

            if (moveTo.exists()) {
                logger.warning("WARNING : completed data file '{}' already exists, so we will not overwrite it. If this happens, you may have issues with the timestamps on your incomming log stream being too far out of sequence between different event sources",
                               moveTo.getAbsolutePath());
            }
            else {
                try {
                    FileUtils.move(currentFile, moveTo);
                }
                catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to move current output file '{}' to '{}'",
                                                             currentFile.getAbsolutePath(),
                                                             moveTo.getAbsolutePath()), e);
                }
            }

            return moveTo;
        }
        else {
            return null;
        }

    }

    public File closeAndRenameCurrentFileUnique() {
        if (outputStream != null) {
            FileUtils.closeQuietly(outputStream);

            File dir = currentFile.getParentFile();
            String name = StringUtils.before(currentFile.getName(), ".writing");

            File moveTo = null;
            
            int integer = 0;
            boolean unique = false;
            while (!unique) {
                moveTo = new File(dir, name + "." + integer);
                unique = !moveTo.exists();
                integer++;
            }

            try {
                FileUtils.move(currentFile, moveTo);                
            }
            catch (IOException e) {
                throw new RuntimeException(String.format("Failed to move current output file '{}' to '{}'",
                                                         currentFile.getAbsolutePath(),
                                                         moveTo.getAbsolutePath()), e);
            }

            return moveTo;
        }
        else {
            return null;
        }

    }

    private void openFile(long periodStart) {

        String filename = DataFileNameFactory.getWritingTemporaryFilename(configuration.getPrefix(), periodStart);
        currentFile = new File(outputFolder, filename);
        logger.info("Opening file '{}'", currentFile.getAbsolutePath());

        boolean append = true;
        try {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile, append)));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Failed to open file '{}' for writing (append=true)", currentFile.getAbsolutePath()), e);
        }
    }

    public void flush() {

        // We make the bold assumption that the compressed data will be smaller
        // than the original. As we are concatenating quite a few events
        // together, this is bound to be true! (I hope!)

        int inflatedSize = buffer.position();
        deflator.setInput(buffer.getBuffer(), 0, inflatedSize);
        deflator.finish();
        int deflatedSize = deflator.deflate(compressionBuffer.array());
        deflator.reset();

        if (outputStream != null) {
            try {
                outputStream.writeInt(deflatedSize);
                outputStream.writeInt(inflatedSize);
                outputStream.write(compressionBuffer.array(), 0, deflatedSize);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to write compressed data to stream", e);
            }
        }

        buffer.clear();
        compressionBuffer.clear();
    }

    public static void main(String[] args) {

        logger.info("Starting LocalDiskRepository...");
        LocalDiskRepositoryConfiguration configuration = new LocalDiskRepositoryConfiguration();
        if (args.length > 0) {
            configuration = LocalDiskRepositoryConfiguration.loadConfiguration(args[0]);
        }

        LocalDiskRepository diskRepository = new LocalDiskRepository(configuration);
        diskRepository.run();
    }

    public File close() {
        if (outputStream != null) {
            if (statsTimer != null) {
                statsTimer.cancel();
                statsTimer = null;
            }
            flush();
            return closeAndRenameCurrentFileUnique();
        }
        else {
            return null;
        }

    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setBufferSize(long sizeInBytes) {
        bufferSize = (int) sizeInBytes;
        buffer = new Output(bufferSize);
        compressionBuffer = allocateBuffer();
        flushAt = (int) (bufferSize * 0.8f);
    }

    @Override public void onNewLogEvent(LogEvent event) {
        process(event);
    }
}
