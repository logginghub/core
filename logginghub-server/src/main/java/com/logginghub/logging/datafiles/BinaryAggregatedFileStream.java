package com.logginghub.logging.datafiles;

import com.logginghub.logging.datafiles.aggregation.Aggregation;
import com.logginghub.logging.datafiles.aggregation.PatternAggregation;
import com.logginghub.logging.datafiles.aggregation.TimeAggregation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision.StatisticsSnapshot;
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
 * Created by james on 17/09/15.
 */
public class BinaryAggregatedFileStream implements Destination<Aggregation> {
    private final static SofConfiguration sofConfiguration;

    static {
        sofConfiguration = new SofConfiguration();

        sofConfiguration.registerType(Aggregation.class, 0);
        sofConfiguration.registerType(PatternAggregation.class, 1);
        sofConfiguration.registerType(TimeAggregation.class, 2);
        sofConfiguration.registerType(StatisticsSnapshot.class, 3);
        sofConfiguration.registerType(IntegerFrequencyCount.class, 4);
    }

    private final File folder;
    private final String name;
    private int levelFilter = Logger.info;
    private boolean autoFlush = true;
    private ExpandingByteBuffer writeBuffer = new ExpandingByteBuffer(10 * 1024);
    private FileChannel channel;

    public BinaryAggregatedFileStream(File folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    public static void replay(File file, Destination<Aggregation> destination) {

        FileChannel channel = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            // TODO : the single object in this file is huge, so we need a massive buffer to read it
            ByteBuffer buffer = ByteBuffer.allocateDirect(100 * 1024 * 1024);

            int read;
            channel = fis.getChannel();
            while ((read = channel.read(buffer)) != -1) {

                buffer.flip();
                try {
                    while (buffer.hasRemaining()) {
                        buffer.mark();
                        Aggregation decoded = SofExpandingBufferSerialiser.read(buffer, sofConfiguration);
                        destination.send(decoded);
                    }
                } catch (SofPartialDecodeException e) {
                    buffer.reset();
                } catch (BufferUnderflowException e) {
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

    public void send(Aggregation event) {

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
