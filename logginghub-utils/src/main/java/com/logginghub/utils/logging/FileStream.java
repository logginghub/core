package com.logginghub.utils.logging;

import com.logginghub.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileStream implements LoggerStream {

    public static int gapThreshold = 250;
    private final File folder;
    private final String name;
    private long lastLogTime = 0;
    private LogEventFormatter formatter = new SingleLineStreamFormatter();
    private int levelFilter = Logger.info;
    private BufferedWriter writer = null;
    private boolean outputGaps = false;
    private boolean autoFlush = true;

    public FileStream(File folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }


    public void onNewLogEvent(LogEvent event) {
        if (event.getLevel() >= levelFilter) {

            synchronized (this) {
                ensureOpen();
                long now = System.currentTimeMillis();

                try {
                    if (outputGaps) {
                        if (now - lastLogTime > gapThreshold) {
                            writer.newLine();
                        }
                    }

                    writer.write(formatter.format(event));

                    if (autoFlush) {
                        writer.flush();
                    }

                    lastLogTime = now;
                }catch(IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }
    }

    public void close() {
        if(writer != null) {
            FileUtils.closeQuietly(writer);
            writer = null;
        }

    }

    private void ensureOpen() {

        if (writer == null) {

            try {
                folder.mkdirs();
                writer = new BufferedWriter(new FileWriter(new File(folder, name)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    public static void setGapThreshold(int gapThreshold) {
        FileStream.gapThreshold = gapThreshold;
    }

    public void setOutputGaps(boolean outputGaps) {
        this.outputGaps = outputGaps;
    }
}
