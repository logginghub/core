package com.logginghub.logging.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventCollection;
import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.BaseFileLoggerConfiguration;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.logging.messages.LogEventCollectionMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.servers.ChannelMultiplexer;
import com.logginghub.logging.servers.DispatchQueue;
import com.logginghub.logging.servers.DispatchQueue.DispatchQueueConfiguration;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.StacktraceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.module.ServiceDiscovery;

public abstract class BaseFileLogger implements AggregatedFileLogger {

    private ChannelMultiplexer channelMultiplexer;
    private LogEventFormatter formatter = new FullEventSingleLineTextFormatter();

    private SimpleDateFormat dateFormat;
    private String fileName = "log";
    private File folder;
    private String fileExtension = ".txt";

    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.SystemErr);

    private boolean forceFlush = false;
    private boolean autoNewline = true;
    private long maximumFileSize = 50 * 1024 * 1024;
    private boolean openWithAppend = true;
    private int numberOfFiles = 3;
    private int numberOfFilesCompressed = 5;

    private long currentBytesWritten = 0;

    private BufferedWriter currentWriter = null;
    private File currentFile;
    private DispatchQueue<LogEvent> queue;
    
    // jshaw - temporary debugging logic to help get to the bottom of Matt Wood's issue
    protected boolean deubgClosings = Boolean.getBoolean("baseFileLogger.debugClosings");

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        if (message instanceof LogEventMessage) {
            LogEvent logEvent = ((LogEventMessage) message).getLogEvent();
            write(logEvent);
        }
        else if (message instanceof LogEventCollectionMessage) {
            LogEventCollection logEventCollection = ((LogEventCollectionMessage) message).getLogEventCollection();
            for (LogEvent logEvent : logEventCollection) {
                write(logEvent);
            }
        }
    }

    @Override public int getLevelFilter() {
        return Level.ALL.intValue();
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }

    public boolean isForceFlush() {
        return forceFlush;
    }

    public void setAutoNewline(boolean autoNewline) {
        this.autoNewline = autoNewline;
    }

    public boolean isAutoNewline() {
        return autoNewline;
    }

    protected void checkForNewline(BufferedWriter currentWriter) throws IOException {
        if (autoNewline) {
            currentWriter.newLine();
        }
    }

    protected void checkForForceFlush(BufferedWriter currentWriter) throws IOException {
        if (forceFlush) {
            currentWriter.flush();
        }
    }

    protected synchronized void write(String formatted, BufferedWriter currentWriter) throws IOException {
        currentWriter.write(formatted);
        checkForNewline(currentWriter);
        updateWrittenPointer(formatted);
        checkForForceFlush(currentWriter);
    }
    
    /**
     * Only for testing!
     */
    public void hackClose() {
        System.err.println("The force close method has been called on the file logger; this is a testing only method and should never happen in production.");
        StacktraceUtils.createPopulatedException().printStackTrace();
        
        try {
            currentWriter.close();
        }
        catch (IOException e) {
        }
    }

    protected boolean willExceedFileSize(String formatted) {
        return (currentBytesWritten + formatted.getBytes().length > maximumFileSize);
    }

    protected void updateWrittenPointer(String formatted) {
        currentBytesWritten += formatted.getBytes().length;
    }

    protected void setCurrentBytesWritten(long currentBytesWritten) {
        this.currentBytesWritten = currentBytesWritten;
    }

    public void setMaximumFileSize(long maximumFileSize) {
        this.maximumFileSize = maximumFileSize;
    }

    public void setOpenWithAppend(boolean openWithAppend) {
        this.openWithAppend = openWithAppend;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public void setNumberOfFilesCompressed(int numberOfFilesCompressed) {
        this.numberOfFilesCompressed = numberOfFilesCompressed;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getNumberOfFilesCompressed() {
        return numberOfFilesCompressed;
    }

    public boolean isOpenWithAppend() {
        return openWithAppend;
    }

    public File getFolder() {
        return folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public void setFolder(String folder) {
        this.folder = new File(folder);
    }

    public void sortArrayByTimestamp(File[] listFiles) {
        Arrays.sort(listFiles, new Comparator<File>() {
            @Override public int compare(File a, File b) {

                String aName = a.getName();
                String bName = b.getName();

                String aTimePart = StringUtils.between(aName, getFileName() + ".", fileExtension);
                String bTimePart = StringUtils.between(bName, getFileName() + ".", fileExtension);

                // Deal with the .1 bit we have to put on the end if we write
                // too many updates in one time period
                String aJustTimePart = StringUtils.before(aTimePart, ".");
                String bJustTimePart = StringUtils.before(bTimePart, ".");

                String aIncrementText = StringUtils.after(aTimePart, ".");
                String bIncrementText = StringUtils.after(bTimePart, ".");

                int aIncrement = 0;
                int bIncrement = 0;

                if (aIncrementText.length() > 0) {
                    aIncrement = Integer.parseInt(aIncrementText);
                }

                if (bIncrementText.length() > 0) {
                    bIncrement = Integer.parseInt(bIncrementText);
                }

                try {
                    Date aDate = dateFormat.parse(aJustTimePart);
                    Date bDate = dateFormat.parse(bJustTimePart);

                    return CompareUtils.add(aDate, bDate).add(aIncrement, bIncrement).compare();
                }
                catch (ParseException e) {
                    throw new RuntimeException(String.format("Failed to parse date part of files %s and %s", a.getAbsolutePath(), b.getAbsoluteFile()),
                                               e);
                }

            }
        });
    }

    public void configure(BaseFileLoggerConfiguration configuration, ServiceDiscovery discovery) {

        setFileName(configuration.getFilename());
        setFolder(new File(configuration.getFolder()));
        setMaximumFileSize(configuration.getMaximumFileSize());
        setNumberOfFiles(configuration.getNumberOfFiles());
        setNumberOfFilesCompressed(configuration.getNumberOfCompressedFiles());
        setOpenWithAppend(configuration.getOpenWithAppend());
        setForceFlush(configuration.getForceFlush());
        setAutoNewline(configuration.getAutoNewline());
        setFileExtension(configuration.getExtension());

        // Setup the formatter - people can use their own via reflection
        LogEventFormatter formatter;

        if (StringUtils.isNotNullOrEmpty(configuration.getFormatter())) {
            formatter = ReflectionUtils.instantiate(configuration.getFormatter());
            ReflectionUtils.invokeIfMethodExists("setPattern", formatter, configuration.getPattern());
        }
        else {
            formatter = new FullEventSingleLineTextFormatter();
        }

        setFormatter(formatter);

        // Process the channels setting - if we have channels set we hand over the channel
        // multiplexer to make sure we only process the correct events
        if (StringUtils.isNotNullOrEmpty(configuration.getChannels())) {
            Destination<LogEvent> writer = new Destination<LogEvent>() {
                @Override public void send(LogEvent t) {
                    sendInternal(t);
                }
            };

            this.channelMultiplexer = new ChannelMultiplexer();
            String[] channels = configuration.getChannels().split(",");
            for (String channel : channels) {
                channelMultiplexer.subscribe(channel.trim(), writer);
            }
        }

        // Bind to the event source
        @SuppressWarnings("unchecked") Source<LogEvent> eventSource = discovery.findService(Source.class, LogEvent.class, configuration.getSource());
        
        // Do we need to setup a queue?
        if (configuration.getWriteAsynchronously()) {
            queue = new DispatchQueue<LogEvent>();
            DispatchQueueConfiguration queueConfiguration = new DispatchQueueConfiguration();
            queueConfiguration.setName(this.getClass().getSimpleName());
            queueConfiguration.setAsynchronousQueueWarningSize(configuration.getAsynchronousQueueWarningSize());
            queueConfiguration.setAsynchronousQueueDiscardSize(configuration.getAsynchronousQueueDiscardSize());
            
            queue.configure(queueConfiguration, discovery);
            
            // Bind the queue in to the source, and then us into the queue
            eventSource.addDestination(queue);
            queue.addDestination(this);
        }
        else {
            // Bind directly to the source
            eventSource.addDestination(this);
        }

    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setTimeFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setFormatter(LogEventFormatter formatter) {
        this.formatter = formatter;
    }

    public LogEventFormatter getFormatter() {
        return formatter;
    }

    @Override public void send(LogEvent t) {
        if (channelMultiplexer != null) {
            channelMultiplexer.send(t);
        }
        else {
            sendInternal(t);
        }
    }

    private void sendInternal(LogEvent t) {
        try {
            write(t);
        }
        catch (LoggingMessageSenderException e) {
            exceptionPolicy.handle(e,
                                   "Failed to write event to rolling aggregated file logger writing to '{}'",
                                   getCurrentFile() != null ? getCurrentFile().getAbsolutePath() : "<null>");
        }
    }

    protected abstract void write(LogEvent t) throws LoggingMessageSenderException;

    public ExceptionPolicy getExceptionPolicy() {
        return exceptionPolicy;
    }

    public void setExceptionPolicy(ExceptionPolicy exceptionPolicy) {
        this.exceptionPolicy = exceptionPolicy;
    }

    public void flush() {
        
        if(queue != null) {
            queue.waitForQueueToDrain();
        }
        
        if (currentWriter != null) {
            try {
               currentWriter.flush();
            }
            catch (IOException e) {
                throw new FormattedRuntimeException("Failed to flush current output buffer", e);
            }
        }
    }

    public BufferedWriter getCurrentWriter() {
        return currentWriter;
    }

    public void setCurrentWriter(BufferedWriter currentWriter) {
        this.currentWriter = currentWriter;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public synchronized void close() {
        if (currentWriter != null) {
            FileUtils.closeQuietly(currentWriter);
            currentWriter = null;
            setCurrentBytesWritten(0);
            setCurrentFile(null);

            // james - annoyingly this looks like a jvm bug; the handle to that
            // file isn't actually released until a GC kicks in
            System.gc();
        }
    }
    
    public void start() {
        if(queue != null) {
            queue.start();
        }
    }

    public void stop() {
        if(queue != null) {
            queue.stop();
        }
        
        close();
    }

}
