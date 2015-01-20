package com.logginghub.logging.modules;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.TimestampFixedRollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.utils.AggregatedFileLogger;
import com.logginghub.logging.utils.BaseFileLogger;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

/**
 * Variation of the {@link RollingFileLogger} that uses timestamps to keep the files unique,
 * avoiding the need to rename swathes of files to keep everything in sync
 * 
 * @author James
 * 
 */
public class TimestampFixedRollingFileLogger extends BaseFileLogger implements FilteredMessageSender, Closeable, AggregatedFileLogger,
                Destination<LogEvent>, Module<TimestampFixedRollingFileLoggerConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(TimestampFixedRollingFileLogger.class);

    private TimeProvider timeProvider = new SystemTimeProvider();

    private long currentTime = -1;
    private int numberOfFilesAtCurrentTime = 0;
    private long currentFileStart;

    public TimestampFixedRollingFileLogger() {
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
                logger.fine("File size would be exceeded on this write, rolling the files...");
                FileUtils.closeQuietly(getCurrentWriter());
                moveCurrentFile();
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
            }
        }
        catch (IOException e) {
            throw new LoggingMessageSenderException(String.format("Failed to write to current file '%s', something has gone badly wrong.",
                                                                  getCurrentFile().getAbsolutePath()), e);
        }

    }

    private void moveCurrentFile() {
        long date = currentFileStart;
        String buildDateFileName = buildDateFileName(date);
        File toFolder = getFolder();
        toFolder.mkdirs();
        File to = new File(toFolder, buildDateFileName);
        File currentFile = getCurrentFile();
        try {
            FileUtils.moveRuntime(currentFile, to);
            logger.finer("Moving '{}' to '{}'", getCurrentFile().getAbsolutePath(), to.getAbsolutePath());
            FileUtils.deleteLoudly(getDataFile());
        }catch(RuntimeException e) {
            logger.warn("Failed to move current log file - did someone delete it or move it already?");
        }
    }

    private void rollFile() {
        zipFiles();
        deleteFiles();
    }

    private void zipFiles() {
        File[] listFiles = getFolder().listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                String fullTimeRegex = "{}\\.\\d{4}_\\d{2}_\\d{2}_\\d{6}(\\.\\d+)?{}";
                return StringUtils.matches(file.getName(), fullTimeRegex, getFileName(), getFileExtension());
            }
        });

        if (listFiles.length >= getNumberOfFiles()) {
            sortArray(listFiles);
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
                return file.getName().startsWith(getFileName()) && file.getName().endsWith(".zip");
            }
        });

        if (listFiles.length > getNumberOfFilesCompressed()) {
            logger.finer("We have {} files, and the maximum number of compressed files to keep is {}", listFiles.length, getNumberOfFilesCompressed());
            sortArray(listFiles);

            int numberToDelete = listFiles.length - getNumberOfFilesCompressed();
            for (int i = 0; i < numberToDelete; i++) {
                File fileToDelete = listFiles[i];
                logger.finer("Deleting '{}'", fileToDelete);
                FileUtils.deleteLoudly(fileToDelete);
            }
        }
    }

    public void sortArray(File[] listFiles) {
        Arrays.sort(listFiles, new Comparator<File>() {
            @Override public int compare(File a, File b) {

                String aName = a.getName();
                String bName = b.getName();

                String aTimePart = StringUtils.between(aName, getFileName() + ".", getFileExtension());
                String bTimePart = StringUtils.between(bName, getFileName() + ".", getFileExtension());

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
                    Date aDate = getDateFormat().parse(aJustTimePart);
                    Date bDate = getDateFormat().parse(bJustTimePart);

                    return CompareUtils.add(aDate, bDate).add(aIncrement, bIncrement).compare();
                }
                catch (ParseException e) {
                    throw new RuntimeException(String.format("Failed to parse date part of files %s and %s", a.getAbsolutePath(), b.getAbsoluteFile()),
                                               e);
                }

            }
        });
    }

    private void openFile() throws IOException {

        String name = buildFileName();
        File currentFile = new File(getFolder(), name);
        setCurrentFile(currentFile);

        if (currentFile.exists()) {
            handleCurrentFileExisting();
        }

        File parentFile = currentFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        logger.fine("Opening file '{}'", currentFile.getAbsolutePath());
        setCurrentBytesWritten(currentFile.length());
        boolean openWithAppend = isOpenWithAppend();
        setCurrentWriter(new BufferedWriter(new FileWriter(currentFile, openWithAppend)));

        File dataFile = getDataFile();
        currentFileStart = timeProvider.getTime();
        FileUtils.writeLong(dataFile, currentFileStart);
    }
    
    private void reopenFile() throws IOException {
        String name = buildFileName();
        File currentFile = new File(getFolder(), name);
        setCurrentFile(currentFile);

        logger.fine("Re-opening file '{}'", currentFile.getAbsolutePath());
        setCurrentBytesWritten(currentFile.length());
        setCurrentWriter(new BufferedWriter(new FileWriter(currentFile, true)));
    }

    private File getDataFile() {
        File dataFile = new File(getFolder(), buildFileName() + ".timedata");
        return dataFile;
    }

    private String buildFileName() {
        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(getFileName());
        filenameBuilder.append(getFileExtension());
        String name = filenameBuilder.toString();
        return name;
    }

    private String buildDateFileName(long time) {
        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(getFileName());
        filenameBuilder.append('.');

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

        File file = FileUtils.getUniqueFile(getFolder(), filenameBuilder.toString(), getFileExtension(), ".", fileNumber);
        return file.getName();
    }

    private void handleCurrentFileExisting() {
        File dataFile = getDataFile();
        if (dataFile.exists()) {
            long date = FileUtils.readLong(dataFile);
            String buildDateFileName = buildDateFileName(date);
            FileUtils.moveRuntime(getCurrentFile(), new File(getFolder(), buildDateFileName));
        }
        else {
            logger.warn("The current log file '{}' already exists, and when we tried to rename it we couldn't find the time data file - this is strange and might mean something is wrong with your configuration (are you running a RollingFileLogger in this folder with the same name maybe?)",
                        getCurrentFile().getAbsolutePath());
        }
    }

    public synchronized void close() {
        if (getCurrentWriter() != null) {
            numberOfFilesAtCurrentTime = 0;
        }

        super.close();
    }

    // /**
    // * @deprecated Use the Module configuration approach from now one
    // * @param logConfiguration
    // * @return
    // */
    // public static TimestampFixedRollingFileLogger
    // fromConfiguration(TimestampVariableRollingFileLoggerConfiguration logConfiguration) {
    // TimestampFixedRollingFileLogger timestampAggregatedFileLogger = new
    // TimestampFixedRollingFileLogger();
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

    @Override public void configure(TimestampFixedRollingFileLoggerConfiguration configuration, ServiceDiscovery discovery) {
        super.configure(configuration, discovery);
    }

    @Override
    public int getConnectionType() {
        return 0;
    }
}
