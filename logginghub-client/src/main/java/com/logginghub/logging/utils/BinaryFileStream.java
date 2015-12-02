package com.logginghub.logging.utils;

import com.logginghub.logging.VLLogEvent;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messaging.LogEventCodex;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.LogEvent;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerPerformanceInterface;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;
import com.logginghub.utils.logging.LoggerStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Logging stream (for com.marketstreamer.util.logging) to write to a binary log file directly. It converts to a regular logging hub event and uses
 * the standard serialisers to turn it into a binary stream.
 */
public class BinaryFileStream implements LoggerStream, Destination<LogEvent> {
    private final File folder;
    private final String name;
    private final String sourceApplication;
    private int pid;
    private int levelFilter = Logger.info;
    private boolean autoFlush = true;
    private InetAddress localHost;
    private String hostAddress;
    private String hostName;
    private ExpandingByteBuffer writeBuffer = new ExpandingByteBuffer(10 * 1024);
    private FileChannel channel;
    private boolean ignoreOldStyle = false;

    public BinaryFileStream(File folder, String name, String sourceApplication) {
        this.folder = folder;
        this.name = name;
        this.sourceApplication = sourceApplication;

        localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
            hostAddress = localHost.getHostAddress();
            hostName = localHost.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        pid = -1;
        // Make a cautious attempt at getting the pid - we dont want things to
        // blow up if this doesn't work though
        try {
            pid = SigarHelper.getPid();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public static void replay(File file, Destination<com.logginghub.logging.LogEvent> destination) {

        FileChannel channel = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024);

            int read;
            channel = fis.getChannel();
            while ((read = channel.read(buffer)) != -1) {

                buffer.flip();
                try {
                    while (buffer.hasRemaining()) {
                        buffer.mark();
                        com.logginghub.logging.LogEvent decode = LogEventCodex.decode(buffer);
                        destination.send(decode);
                    }
                } catch (PartialMessageException e) {
                    buffer.reset();
                }

                buffer.compact();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.closeQuietly(channel);
        }
    }

    public static void replayEventContexts(File file, Destination<EventContext> destination) {

        FileChannel channel = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

            LoggerPerformanceInterface lpi = new LoggerPerformanceInterface(Logger.root());
            EventContext eventContext = lpi.new EventContext();

            int read;
            channel = fis.getChannel();
            while ((read = channel.read(buffer)) != -1) {

                buffer.flip();
                try {
                    while (buffer.hasRemaining()) {
                        buffer.mark();
                        LogEventCodex.decode(buffer, eventContext);
                        destination.send(eventContext);
                    }
                } catch (PartialMessageException e) {
                    buffer.reset();
                }

                buffer.compact();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.closeQuietly(channel);
        }
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    public void send(LogEvent event) {
        onNewLogEvent(event);
    }

    public void onNewLogEvent(LogEvent event) {
        if (event.getLevel() >= levelFilter && !ignoreOldStyle) {

            VLLogEvent vlevent = new VLLogEvent(event, pid, sourceApplication, localHost.getHostAddress(), localHost.getHostName());

            synchronized (this) {
                ensureOpen();

                try {

                    LogEventCodex.encode(writeBuffer, vlevent);
                    writeBuffer.flip();
                    channel.write(writeBuffer.getBuffer());
                    writeBuffer.clear();

                    if (autoFlush) {
                        channel.force(true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }
    }

    @Override
    public void onNewLogEvent(EventContext event) {

        if (event.getLevel() >= levelFilter) {

            synchronized (this) {
                ensureOpen();

                try {

                    LogEventCodex.encodeEventContext(writeBuffer, event);
                    writeBuffer.flip();
                    channel.write(writeBuffer.getBuffer());
                    writeBuffer.clear();

                    if (autoFlush) {
                        channel.force(true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }

    }

    private void ensureOpen() {

        if (channel == null) {

            try {
                folder.mkdirs();
                FileOutputStream fos = new FileOutputStream(new File(folder, name));
                channel = fos.getChannel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() {
        if (channel != null) {
            FileUtils.closeQuietly(channel);
            channel = null;
        }

    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    public void setIgnoreOldStyle(boolean ignoreOldStyle) {
        this.ignoreOldStyle = ignoreOldStyle;
    }
}
