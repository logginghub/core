package com.logginghub.logging.utils;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofExpandingBufferSerialiser;
import com.logginghub.utils.sof.SofPartialDecodeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 */
public class BinaryPatternisedFileStream implements Destination<PatternisedLogEvent> {
    private final File folder;
    private final String name;
    private final static SofConfiguration sofConfiguration;
    private int levelFilter = Logger.info;
    private boolean autoFlush = true;
    private ExpandingByteBuffer writeBuffer = new ExpandingByteBuffer(10 * 1024);
    private FileChannel channel;

    static {
        sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(PatternisedLogEvent.class, 0);
    }

    public BinaryPatternisedFileStream(File folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    public static void replay(File file, Destination<PatternisedLogEvent> destination) {

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
                        PatternisedLogEvent decoded = SofExpandingBufferSerialiser.read(buffer, sofConfiguration);
                        destination.send(decoded);
                    }
                } catch (SofPartialDecodeException e) {
                    buffer.reset();
                }catch (BufferUnderflowException e) {
                    buffer.reset();
                } catch (SofException e) {
                    e.printStackTrace();
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

    public void send(PatternisedLogEvent event) {
        if (event.getLevel() >= levelFilter) {

            synchronized (this) {
                ensureOpen();

                try {
                    SofExpandingBufferSerialiser.write(writeBuffer, event, sofConfiguration);
                    writeBuffer.flip();
                    channel.write(writeBuffer.getBuffer());
                    writeBuffer.clear();

                    if (autoFlush) {
                        channel.force(true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                } catch (SofException e) {
                    e.printStackTrace();
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
}
