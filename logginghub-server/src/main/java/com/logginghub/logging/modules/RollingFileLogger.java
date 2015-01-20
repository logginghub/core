package com.logginghub.logging.modules;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.utils.AggregatedFileLogger;
import com.logginghub.logging.utils.BaseFileLogger;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;

@Provides(LogEvent.class) public class RollingFileLogger extends BaseFileLogger implements FilteredMessageSender, Closeable, AggregatedFileLogger,
                Destination<LogEvent>, Module<RollingFileLoggerConfiguration> {

    protected synchronized void write(LogEvent logEvent) throws LoggingMessageSenderException {
        String formatted = getFormatter().format(logEvent);

        try {
            if (getCurrentWriter() == null) {
                openFile();
            }

            if (willExceedFileSize(formatted)) {
                try {
                    rollFile();
                    openFile();
                }
                catch (IOException e) {
                    throw new LoggingMessageSenderException("Failed to roll log files", e);
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
            throw new LoggingMessageSenderException("Failed to write message", e);
        }

    }

    private void reopenFile() throws IOException {
        setCurrentBytesWritten(getCurrentFile().length());
        setCurrentWriter(new BufferedWriter(new FileWriter(getCurrentFile(), true)));
    }

    private void rollFile() throws IOException {
        // Starting from the end, delete the max and then rename the compressed
        // archives
        for (int i = getNumberOfFilesCompressed() - 1; i >= 0; i--) {
            String filename = getFileName() + getFileExtension() + "." + filenumber(i + getNumberOfFiles()) + ".zip";
            File file = new File(getFolder(), filename);
            if (file.exists()) {
                if (i == getNumberOfFilesCompressed() - 1) {
                    if (!file.delete()) {
                        System.err.println(String.format("Failed to delete log file '%s'", file.getAbsolutePath()));
                    }
                }
                else {
                    String targetFilename = getFileName() + getFileExtension() + "." + filenumber(i + 1 + getNumberOfFiles()) + ".zip";
                    File target = new File(getFolder(), targetFilename);
                    if (!file.renameTo(target)) {
                        System.err.println(String.format("Failed to rename log file '%s' to '%s'", file.getAbsolutePath(), target.getAbsolutePath()));
                    }
                }
            }
        }

        // Starting from the end, compress the max and then rename the
        // uncompressed archives
        for (int i = getNumberOfFiles() - 1; i >= 0; i--) {
            String filename = getFileName() + getFileExtension() + "." + filenumber(i);
            File file = new File(getFolder(), filename);
            if (file.exists()) {
                if (i == getNumberOfFiles() - 1) {
                    String targetFilename = getFileName() + getFileExtension() + "." + filenumber(getNumberOfFiles()) + ".zip";
                    File target = new File(getFolder(), targetFilename);
                    zipTo(file, target);
                    if (!file.delete()) {
                        // What todo ?
                        System.err.println(String.format("Failed to delete log file '%s'", file.getAbsolutePath()));
                    }
                }
                else {
                    String targetFilename = getFileName() + getFileExtension() + "." + filenumber(i + 1);
                    File target = new File(getFolder(), targetFilename);
                    if (!file.renameTo(target)) {
                        // What to do?
                        System.err.println(String.format("Failed to rename log file '%s' to '%s'", file.getAbsolutePath(), target.getAbsolutePath()));
                    }
                }
            }
        }

        // This should free up the names so we can sort out the active file
        FileUtils.closeQuietly(getCurrentWriter());
        String targetFilename = getFileName() + getFileExtension() + "." + filenumber(1);
        File target = new File(getFolder(), targetFilename);
        if (!getCurrentFile().renameTo(target)) {
            // What to do?
            System.err.println(String.format("Failed to rename log file '%s' to '%s'", getCurrentFile().getAbsolutePath(), target.getAbsolutePath()));
        }
    }

    private String filenumber(int i) {
        int padding = (int) Math.log10(getNumberOfFiles() + getNumberOfFilesCompressed()) + 1;

        String filenumber = Integer.toString(i);
        while (filenumber.length() < padding) {
            filenumber = '0' + filenumber;
        }

        return filenumber;
    }

    private void zipTo(File file, File target) throws IOException {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(target);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        out.setMethod(ZipOutputStream.DEFLATED);
        byte data[] = new byte[4096];
        FileInputStream fi = new FileInputStream(file);
        origin = new BufferedInputStream(fi, 4096);
        ZipEntry entry = new ZipEntry(file.getName());
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, 4096)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
        out.close();
    }

    private void openFile() throws IOException {
        String filename = getFileName() + getFileExtension();
        File currentFile = new File(getFolder(), filename);
        setCurrentFile(currentFile);

        boolean openWithAppend = isOpenWithAppend();
        if (!openWithAppend && getCurrentFile().exists()) {
            rollFile();
        }

        File parentFile = getCurrentFile().getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        setCurrentBytesWritten(getCurrentFile().length());
        setCurrentWriter(new BufferedWriter(new FileWriter(currentFile, openWithAppend)));
    }

    // /**
    // * @deprecated use the module approach to configuratio now please
    // * @param aggregatedFileLogConfiguration
    // * @return
    // */
    // public static RollingFileLogger fromConfiguration(RollingFileLoggerConfiguration
    // aggregatedFileLogConfiguration) {
    // RollingFileLogger aggregatedFileLogger = new RollingFileLogger();
    // aggregatedFileLogger.setFileName(aggregatedFileLogConfiguration.getFileName());
    // aggregatedFileLogger.setMaximumFileSize(aggregatedFileLogConfiguration.getMaximumFileSize());
    // aggregatedFileLogger.setNumberOfFiles(aggregatedFileLogConfiguration.getNumberOfFiles());
    // aggregatedFileLogger.setNumberOfFilesCompressed(aggregatedFileLogConfiguration.getNumberOfCompressedFiles());
    // aggregatedFileLogger.setOpenWithAppend(aggregatedFileLogConfiguration.getOpenWithAppend());
    // aggregatedFileLogger.setForceFlush(aggregatedFileLogConfiguration.getForceFlush());
    //
    // FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
    // aggregatedFileLogger.setFormatter(formatter);
    // return aggregatedFileLogger;
    // }

    @Override public void configure(RollingFileLoggerConfiguration configuration, ServiceDiscovery discovery) {
        super.configure(configuration, discovery);
    }


    @Override
    public int getConnectionType() {
        return 0;
    }
}
