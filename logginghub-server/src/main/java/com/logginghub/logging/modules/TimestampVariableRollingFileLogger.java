package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.utils.AggregatedFileLogger;
import com.logginghub.logging.utils.BaseFileLogger;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Variation of the {@link RollingFileLogger} that uses timestamps to keep the files unique,
 * avoiding the need to rename swathes of files to keep everything in sync
 * 
 * @author James
 * 
 */
public class TimestampVariableRollingFileLogger extends BaseFileLogger implements FilteredMessageSender, Closeable, AggregatedFileLogger,
                Destination<LogEvent>, Module<TimestampVariableRollingFileLoggerConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(TimestampVariableRollingFileLogger.class);

    private TimeProvider timeProvider = new SystemTimeProvider();

    private long currentTime = -1;
    private int numberOfFilesAtCurrentTime = 0;

    public TimestampVariableRollingFileLogger() {
        setTimeFormat("yyyy_MM_dd_HHmmss");
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public synchronized void write(LogEvent logEvent) throws LoggingMessageSenderException {
        String formatted = getFormatter().format(logEvent);
        logger.finest("Writing event : {}", logEvent);

        try {
            if (getCurrentWriter() == null) {
                openFile();
            }

            if (willExceedFileSize(formatted)) {
                rollFile();
                try {
                    openFile();
                }
                catch (IOException e) {
                    throw new LoggingMessageSenderException(String.format("Failed to open new file to write to '%s'",
                                                                          getCurrentFile().getAbsolutePath()), e);
                }
            }

            try {
                write(formatted, getCurrentWriter());
            }
            catch (IOException e) {
                if (e.getMessage().equals("Stream closed")) {
                    reopenFile();
                    write(formatted, getCurrentWriter());
                }
                else {
                    throw e;
                }
            };
        }
        catch (IOException e) {
            throw new LoggingMessageSenderException(String.format("Failed to write to current file '%s', something has gone badly wrong.",
                                                                  getCurrentFile().getAbsolutePath()), e);
        }

    }

    private void reopenFile() throws IOException {
        logger.fine("Re-opening file '{}'", getCurrentFile().getAbsolutePath());
        setCurrentBytesWritten(getCurrentFile().length());
        setCurrentWriter(new BufferedWriter(new FileWriter(getCurrentFile(), true)));
    }

    private void rollFile() {
        zipFiles();
        deleteFiles();
        FileUtils.closeQuietly(getCurrentWriter());
    }

    private void zipFiles() {
        // File[] listFiles2 = getFolder().listFiles();
        // for (File file : listFiles2) {
        // System.out.println(file.getAbsolutePath());
        // }

        File[] listFiles = getFolder().listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                String fullTimeRegex = "{}\\.\\d\\{4\\}_\\d\\{2\\}_\\d\\{2\\}_\\d\\{6\\}(\\.\\d+)?{}";
                return StringUtils.matches(file.getName(), fullTimeRegex, getFileName(), getFileExtension());
            }
        });

        if (listFiles.length >= getNumberOfFiles()) {
            sortArrayByTimestamp(listFiles);
            int numberToCompress = 1 + listFiles.length - getNumberOfFiles();
            for (int i = 0; i < numberToCompress; i++) {
                File fileToZip = listFiles[i];
                File destination = new File(fileToZip.getParent(), fileToZip.getName() + ".zip");
                logger.finer("Zipping '{}' to '{}'", fileToZip, destination);
                FileUtils.zipTo(fileToZip, destination);
                FileUtils.deleteLoudly(fileToZip);
            }
        }
    }

    private void deleteFiles() {

        File[] listFiles = getFolder().listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                String fullTimeRegex = "{}\\.\\d\\{4\\}_\\d\\{2\\}_\\d\\{2\\}_\\d\\{6\\}(\\.\\d+)?{}\\.zip";
                return StringUtils.matches(file.getName(), fullTimeRegex, getFileName(), getFileExtension());
            }
        });

        if (listFiles.length > getNumberOfFilesCompressed()) {
            logger.finer("We have {} files, and the maximum number of compressed files to keep is {}", listFiles.length, getNumberOfFilesCompressed());
            sortArrayByTimestamp(listFiles);

            int numberToDelete = listFiles.length - getNumberOfFilesCompressed();
            for (int i = 0; i < numberToDelete; i++) {
                File fileToDelete = listFiles[i];
                logger.finer("Deleting '{}'", fileToDelete);
                FileUtils.deleteLoudly(fileToDelete);
            }
        }
    }

    private void openFile() throws IOException {

        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(getFileName());
        filenameBuilder.append('.');

        long time = timeProvider.getTime();

        int fileNumber = 0;
        if (time == currentTime) {
            numberOfFilesAtCurrentTime++;
            fileNumber = numberOfFilesAtCurrentTime;
        }
        else {
            currentTime = time;
            numberOfFilesAtCurrentTime = 0;
        }

        filenameBuilder.append(getDateFormat().format(new Date(time)));
        if (fileNumber > 0) {
            filenameBuilder.append('.');
            filenameBuilder.append(fileNumber);
        }
        filenameBuilder.append(getFileExtension());

        String name = filenameBuilder.toString();
        setCurrentFile(new File(getFolder(), name));
        logger.fine("Opening file '{}'", getCurrentFile().getAbsolutePath());

        File parentFile = getCurrentFile().getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        setCurrentBytesWritten(getCurrentFile().length());
        setCurrentWriter(new BufferedWriter(new FileWriter(getCurrentFile(), isOpenWithAppend())));
    }

    // /**
    // * @deprecated Use the Module configuration approach from now one
    // * @param logConfiguration
    // * @return
    // */
    // public static TimestampVariableRollingFileLogger
    // fromConfiguration(TimestampVariableRollingFileLoggerConfiguration logConfiguration) {
    // TimestampVariableRollingFileLogger timestampAggregatedFileLogger = new
    // TimestampVariableRollingFileLogger();
    // timestampAggregatedFileLogger.setFileName(logConfiguration.getFilename());
    // timestampAggregatedFileLogger.setFileExtension(logConfiguration.getExtension());
    // timestampAggregatedFileLogger.setFolder(new File(logConfiguration.getFolder()));
    // timestampAggregatedFileLogger.setMaximumFileSize(logConfiguration.getMaximumFileSize());
    // timestampAggregatedFileLogger.setNumberOfFiles(logConfiguration.getNumberOfFiles());
    // timestampAggregatedFileLogger.setNumberOfFilesCompressed(logConfiguration.getNumberOfCompressedFiles());
    // timestampAggregatedFileLogger.setOpenWithAppend(logConfiguration.getOpenWithAppend());
    // timestampAggregatedFileLogger.setForceFlush(logConfiguration.getForceFlush());
    //
    // FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
    // timestampAggregatedFileLogger.setFormatter(formatter);
    //
    // return timestampAggregatedFileLogger;
    // }

    @Override public void configure(TimestampVariableRollingFileLoggerConfiguration configuration, ServiceDiscovery discovery) {
        super.configure(configuration, discovery);
    }

    @Override
    public int getConnectionType() {
        return 0;
    }
}
