package com.logginghub.logging.utils;

import java.util.Comparator;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.logging.Logger;

public class LogEventBlockElement {

    private long startPosition;
    private long endPosition;

    private int count = 0;
    private long earliestTime = Long.MAX_VALUE;
    private long latestTime = Long.MIN_VALUE;

    // Level counts
    private int other;
    private int severe;
    private int info;
    private int warnings;

    private LogEventBlockDataProvider dataProvider;
    
    public static final Comparator<LogEventBlockElement> TimeComparator = new Comparator<LogEventBlockElement>() {
        @Override public int compare(LogEventBlockElement o1, LogEventBlockElement o2) {
            return CompareUtils.compareLongs(o1.getEarliestTime(), o1.getLatestTime());
        }
    };
    
    public LogEventBlockElement() {}

    public LogEventBlockElement(int count, long start, long end) {
        this.count = count;
        this.earliestTime = start;
        this.latestTime = end;
    }

    @Override public String toString() {
        return "LogEventBlockElement [count=" +
               count +
               ", earliestTime=" +
               Logger.toDateString(earliestTime) +
               ", latestTime=" +
               Logger.toDateString(latestTime) +
               "]";
    }

    public boolean overlaps(LogEventBlockElement nextIndex) {

        LogEventBlockElement a = this;
        LogEventBlockElement b = nextIndex;

        boolean overlap;

        if (a.earliestTime >= b.earliestTime && a.earliestTime < b.latestTime) {
            // a start inside b
            overlap = true;
        }
        else if (a.latestTime > b.earliestTime && a.latestTime <= b.latestTime) {
            // a end inside b
            overlap = true;
        }
        else if (b.earliestTime >= a.earliestTime && b.earliestTime < a.latestTime) {
            // b start inside a
            overlap = true;
        }
        else if (b.latestTime > a.earliestTime && b.latestTime <= a.latestTime) {
            // b end inside a
            overlap = true;
        }
        else if (a.earliestTime <= b.earliestTime && a.latestTime > b.latestTime) {
            // a contains b
            overlap = true;
            // (b contains a is covered by the first two checks)
        }
        else {
            overlap = false;
        }

        return overlap;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getSize() {
        return endPosition - startPosition;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getEarliestTime() {
        return earliestTime;
    }

    public void setEarliestTime(long earliestTime) {
        this.earliestTime = earliestTime;
    }

    public long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public int getSevere() {
        return severe;
    }

    public void setSevere(int severe) {
        this.severe = severe;
    }

    public int getInfo() {
        return info;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public void setEndPosition(long count) {
        this.endPosition = count;
    }

    public void update(LogEvent event) {
        long eventTime = event.getOriginTime();
        this.earliestTime = Math.min(earliestTime, eventTime);
        this.latestTime = Math.max(latestTime, eventTime);

        int level = event.getLevel();
        if (level == Logger.severe) {
            severe++;
        }
        else if (level == Logger.warning) {
            warnings++;
        }
        else if (level == Logger.info) {
            info++;
        }else{
            other++;
        }

        count++;
    }

    
    public void setDataProvider(LogEventBlockDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
    
    public LogEventBlockDataProvider getDataProvider() {
        return dataProvider;
    }
}
