package com.logginghub.logging.repository;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class SofBlockStreamRotatingWriter implements Asynchronous {

    private static final Logger logger = Logger.getLoggerFor(SofBlockStreamRotatingWriter.class);

    private volatile SofBlockStreamWriter writer;
    private File folder;
    private String prefix;
    private String postfix;

    private TimeProvider timeProvider = new SystemTimeProvider();
    private int blocksize = (int) ByteUtils.megabytes(10);
    private SofConfiguration configuration;

    private long totalFileSizeLimit;

    public enum RotationTrigger {
        Size,
        Time
    }

    private RotationTrigger trigger = RotationTrigger.Size;

    private long currentFileTime = 0;
    private long rotationTime = TimeUtils.minutes(10);
    private long rotationSize = ByteUtils.megabytes(128);
    private BufferedOutputStream stream;

    private long maximumFlushInterval;

    private WorkerThread flushThread;

    private boolean useEventTimes = false;

    public SofBlockStreamRotatingWriter(File folder, String prefix, String postfix, SofConfiguration configuration) {
        this.folder = folder;
        this.prefix = prefix;
        this.postfix = postfix;
        this.configuration = configuration;
    }

    public void setBlocksize(int blocksize) {
        this.blocksize = blocksize;
    }

    public void setRotationSize(long rotationSize) {
        this.rotationSize = rotationSize;
    }

    public void setRotationTime(long rotationTime) {
        this.rotationTime = rotationTime;
    }

    public void setTrigger(RotationTrigger trigger) {
        this.trigger = trigger;
    }

    private void openFile(long time) throws IOException {

        if (trigger == RotationTrigger.Time) {
            time = TimeUtils.chunk(time, rotationTime);
        }

        FileDateFormat format = new FileDateFormat();
        String filename = String.format("%s%s.", prefix, format.format(new Date(time)));

        File file = FileUtils.getUniqueFile(folder, filename, postfix);

        logger.info("Opening file '{}'", file.getAbsolutePath());

        currentFileTime = time;
        stream = new BufferedOutputStream(new FileOutputStream(file));
        writer = new SofBlockStreamWriter(stream, configuration, blocksize);
        logger.fine("Writer created : {}", writer);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public synchronized void close() throws SofException, IOException {
        if (writer != null) {
            writer.close();
            writer = null;
            stream.close();
            stream = null;
        }
    }

    public synchronized void send(SerialisableObject object) throws IOException, SofException {

        long time;
        if (useEventTimes && object instanceof TimeProvider) {
            TimeProvider objectTimeProvider = (TimeProvider) object;
            time = objectTimeProvider.getTime();
        }
        else {
            time = timeProvider.getTime();
        }

        if (writer == null) {
            openFile(time);
        }

        writer.write(object);

        boolean rotate = false;
        if (trigger == RotationTrigger.Size) {
            if (writer.getBytesWritten() >= rotationSize) {
                logger.info("Rolling file as size is greater than the rotation size");
                rotate = true;
            }
        }
        else if (trigger == RotationTrigger.Time) {
            if (time - currentFileTime > rotationTime) {
                logger.info("Rolling file as the time is greater than the rotation interval");
                rotate = true;
            }
        }

        if (rotate) {
            close();
            checkForDirectoryLimit();
        }
    }

    private void checkForDirectoryLimit() {
        long size = 0;
        File[] sortedFileList = RotatingHelper.getSortedFileList(folder, prefix, postfix);
        for (File file : sortedFileList) {
            size += file.length();
        }

        logger.info("Total file size is now '{}' ({} files) vs the limit of '{}'",
                    ByteUtils.format(size),
                    sortedFileList.length,
                    ByteUtils.format(totalFileSizeLimit));

        int index = 0;
        while (size > totalFileSizeLimit) {
            File file = sortedFileList[index++];
            size -= file.length();

            logger.info("Deleting '{}' to maintain the total file size limit", file.getAbsolutePath());
            boolean delete = file.delete();
            if (!delete) {
                logger.warn("Attempted to delete oldest file '{}' to maintain the total file size limit of '{}', but the delete failed. We'll have to try again next time, but if this continues we can't guarantee the file size limit",
                            file.getAbsolutePath(),
                            ByteUtils.format(totalFileSizeLimit));
            }

        }
    }

    public void setTotalFileSizeLimit(String limit) {
        this.totalFileSizeLimit = ByteUtils.parse(limit);
    }

    public void flush() throws SofException, IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    public void visitLatest(long start, long end, Destination<SerialisableObject> destination) throws EOFException, SofException {
        if (writer != null) {
            logger.fine("Visiting current writing");
            writer.visitLatest(start, end, destination);
        }else{
            logger.fine("No writer to visit");
        }
    }

    public void setMaximumFlushInterval(long maximumFlushInterval) {
        this.maximumFlushInterval = maximumFlushInterval;
    }

    public long getMaximumFlushInterval() {
        return maximumFlushInterval;
    }

    @Override public void start() {
        stop();

        this.flushThread = WorkerThread.every("LoggingHub-SofBlockStreamRotatingWriter-PeriodicFlushThread",
                                              maximumFlushInterval,
                                              TimeUnit.MILLISECONDS,
                                              new Runnable() {
                                                  @Override public void run() {
                                                      if (writer != null) {
                                                          try {
                                                              logger.fine("Forcing periodic writer flush");
                                                              writer.periodicFlush();
                                                          }
                                                          catch (SofException e) {
                                                              logger.warn(e, "Failed to periodically flush writer");
                                                          }
                                                          catch (IOException e) {
                                                              logger.warn(e, "Failed to periodically flush writer");
                                                          }
                                                      }
                                                  }
                                              });

    }

    @Override public void stop() {
        if (flushThread != null) {
            flushThread.stop();
            flushThread = null;
        }
    }

    public void setUseEventTimes(boolean useEventTimes) {
        this.useEventTimes = useEventTimes;
    }

}
