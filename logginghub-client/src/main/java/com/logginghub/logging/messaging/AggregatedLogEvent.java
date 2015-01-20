package com.logginghub.logging.messaging;

import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class AggregatedLogEvent implements SerialisableObject, TimeProvider {

    private int aggregationID;
    private long time;
    private double value;
    private String seriesKey;

    @Override public void read(SofReader reader) throws SofException {
        this.aggregationID = reader.readInt(0);
        this.time = reader.readLong(1);
        this.value = reader.readDouble(2);
        this.seriesKey = reader.readString(3);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, aggregationID);
        writer.write(1, time);
        writer.write(2, value);
        writer.write(3, seriesKey);
    }

    public int getAggregationID() {
        return aggregationID;
    }

    public void setAggregationID(int aggregationID) {
        this.aggregationID = aggregationID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getSeriesKey() {
        return seriesKey;
    }

    public void setSeriesKey(String seriesKey) {
        this.seriesKey = seriesKey;
    }

    @Override public String toString() {
        return "AggregatedLogEvent [aggregationID=" +
               aggregationID +
               ", time=" +
               Logger.toDateString(time) +
               ", value=" +
               value +
               ", seriesKey=" +
               seriesKey +
               "]";
    }

}
