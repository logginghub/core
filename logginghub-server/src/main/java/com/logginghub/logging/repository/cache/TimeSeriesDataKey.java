package com.logginghub.logging.repository.cache;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TimeSeriesDataKey /*implements DataSerializable*/ {
    private static final long serialVersionUID = 1L;
    
    private long startTime;
    private long endTime;
    private String seriesName;

    public TimeSeriesDataKey() {}

    public TimeSeriesDataKey(String seriesName, long startTime, long endTime) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.seriesName = seriesName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public void readData(DataInput in) throws IOException {
        startTime = in.readLong();
        endTime = in.readLong();
        seriesName = in.readUTF();
    }

    public void writeData(DataOutput out) throws IOException {
        out.writeLong(startTime);
        out.writeLong(endTime);
        out.writeUTF(seriesName);
    }
    
    @Override public String toString() {
        return "TimeSeriesDataKey [seriesName=" + seriesName + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    } 
}
