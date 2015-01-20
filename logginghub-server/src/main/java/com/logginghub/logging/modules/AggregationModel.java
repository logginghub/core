package com.logginghub.logging.modules;

import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.TimeUtils;

public class AggregationModel {

    private int aggregationID;
    private int patternID;
    private String interval;
    private String type;
    private int labelIndex;
    private String eventParts;

    public int getAggregationID() {
        return aggregationID;
    }
    
    public void setAggregationID(int aggregationID) {
        this.aggregationID = aggregationID;
    }

    public int getPatternID() {
        return patternID;
    }
    
    public int getLabelIndex() {
        return labelIndex;
    }
    
    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }
    
    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AggregationModel(int id, int pattern, int label, String interval, String type) {
        super();
        this.aggregationID = id;
        this.patternID = pattern;
        this.labelIndex = label;
        this.interval = interval;
        this.type = type;
    }

    public AggregationModel() {}

    @Override public String toString() {
        return "AggregationModel [aggregationID=" + aggregationID + ", pattern=" + patternID + ", interval=" + interval + ", type=" + type + "]";
    }

    public String getEventParts() {
        return eventParts;         
    }
    
    public void setEventParts(String eventParts) {
        this.eventParts = eventParts;
    }

    public long getIntervalMilliseconds() {
        return TimeUtils.parseInterval(interval);
         
    }

    public AggregationType getTypeEnum() {
        return AggregationType.valueOf(type);
         
    }

}
