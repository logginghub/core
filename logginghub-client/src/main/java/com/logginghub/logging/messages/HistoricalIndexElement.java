package com.logginghub.logging.messages;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalIndexElement implements SerialisableObject, TimeProvider {

    private long time;
    private long interval;

    private int infoCount;
    private int warningCount;
    private int severeCount;
    private int otherCount;

    public HistoricalIndexElement() {}

    public HistoricalIndexElement(long time, long interval, int infoCount, int warningCount, int severeCount, int otherCount) {
        super();
        this.time = time;
        this.interval = interval;
        this.infoCount = infoCount;
        this.warningCount = warningCount;
        this.severeCount = severeCount;
        this.otherCount = otherCount;
    }

    public HistoricalIndexElement(HistoricalIndexElement toCopy) {
        time = toCopy.time;
        interval = toCopy.interval;
        infoCount = toCopy.infoCount;
        warningCount = toCopy.warningCount;
        severeCount = toCopy.severeCount;
        otherCount = toCopy.otherCount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(int infoCount) {
        this.infoCount = infoCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public int getSevereCount() {
        return severeCount;
    }

    public void setSevereCount(int severeCount) {
        this.severeCount = severeCount;
    }

    public int getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(int otherCount) {
        this.otherCount = otherCount;
    }

    public void read(SofReader reader) throws SofException {
        time = reader.readLong(1);
        interval = reader.readLong(2);
        infoCount = reader.readInt(3);
        warningCount = reader.readInt(4);
        severeCount = reader.readInt(5);
        otherCount = reader.readInt(6);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, time);
        writer.write(2, interval);
        writer.write(3, infoCount);
        writer.write(4, warningCount);
        writer.write(5, severeCount);
        writer.write(6, otherCount);
    }

    public int getTotalCount() {
        return infoCount + warningCount + severeCount + otherCount;
    }

    public void remove(LogEvent event) {
        int level = event.getLevel();
        if (level == Logger.severe) {
            severeCount--;
        }
        else if (level == Logger.warning) {
            warningCount--;
        }
        else if (level == Logger.info) {
            infoCount--;
        }
        else {
            otherCount--;
        }
    }

    public void increment(HistoricalIndexElement value) {
        this.infoCount += value.infoCount;
        this.warningCount += value.warningCount;
        this.severeCount += value.severeCount;
        this.otherCount += value.otherCount;
    }
    
    public void increment(LogEvent event) {
        int level = event.getLevel();
        if (level == Logger.severe) {
            severeCount++;
        }
        else if (level == Logger.warning) {
            warningCount++;
        }
        else if (level == Logger.info) {
            infoCount++;
        }
        else {
            otherCount++;
        }
    }

    public void set(HistoricalIndexElement other) {
        severeCount = other.getSevereCount();
        infoCount = other.getInfoCount();
        warningCount = other.getWarningCount();
        otherCount = other.getOtherCount();
    }

    @Override public String toString() {
        return "HistoricalIndexElement [time=" +
               Logger.toDateString(time) +
               ", interval=" +
               interval +
               ", infoCount=" +
               infoCount +
               ", warningCount=" +
               warningCount +
               ", severeCount=" +
               severeCount +
               ", otherCount=" +
               otherCount +
               "]";
    }



    

}
