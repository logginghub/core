package com.logginghub.utils.logging;

import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;

public class SystemErrStream implements LoggerStream {
    public static int gapThreshold = 250;
    private long lastLogTime = 0;
    private LogEventFormatter formatter = new SingleLineStreamFormatter();
    private int levelFilter = Logger.info;

    public void onNewLogEvent(LogEvent event) {
        if(event.getLevel() >= levelFilter) {
            long now = System.currentTimeMillis();
            if (now - lastLogTime > gapThreshold) {
                System.err.println();
            }
            System.err.print(formatter.format(event));
            System.err.flush();
            lastLogTime = now;
        }
    }

    @Override
    public void onNewLogEvent(EventContext event) {
        if(event.getLevel() >= levelFilter) {
            long now = System.currentTimeMillis();
            if (now - lastLogTime > gapThreshold) {
                System.err.println();
            }
            System.err.print(formatter.format(event));
            System.err.flush();
            lastLogTime = now;
        }
    }

    public void setFormatter(LogEventFormatter formatter) {
        this.formatter = formatter;
    }

    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    public int getLevelFilter() {
        return levelFilter;
    }
}
