package com.logginghub.logging.api.patterns;

import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class Aggregation implements SerialisableObject {

    private int aggregationID = -1;
    private int patternID = -1;
    private int captureLabelIndex = -1;
    private AggregationType type = AggregationType.Count;
    private long interval = TimeUtils.seconds(1);
    private String groupBy;

    @Override public void read(SofReader reader) throws SofException {
        aggregationID = reader.readInt(0);
        patternID = reader.readInt(1);
        captureLabelIndex = reader.readInt(2);
        type = AggregationType.valueOf(reader.readString(3));
        interval = reader.readLong(4);
        groupBy = reader.readString(5);        
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, aggregationID);
        writer.write(1, patternID);
        writer.write(2, captureLabelIndex);
        writer.write(3, type.name());
        writer.write(4, interval);
        writer.write(5, groupBy);
    }

    public int getAggregationID() {
        return aggregationID;
    }

    public void setAggregationID(int aggregationID) {
        this.aggregationID = aggregationID;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getCaptureLabelIndex() {
        return captureLabelIndex;
    }

    public void setCaptureLabelIndex(int captureLabelIndex) {
        this.captureLabelIndex = captureLabelIndex;
    }

    public AggregationType getType() {
        return type;
    }

    public void setType(AggregationType type) {
        this.type = type;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    @Override public String toString() {
        return "Aggregation [aggregationID=" +
               aggregationID +
               ", patternID=" +
               patternID +
               ", captureLabelIndex=" +
               captureLabelIndex +
               ", type=" +
               type +
               ", interval=" +
               interval +
               ", groupBy=" +
               groupBy +
               "]";
    }

    
}
