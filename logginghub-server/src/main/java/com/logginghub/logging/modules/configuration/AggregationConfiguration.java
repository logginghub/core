package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class AggregationConfiguration {

    @XmlAttribute private int aggregationID = -1;
    @XmlAttribute private int patternID = -1;
    @XmlAttribute private int labelIndex = -1;
    @XmlAttribute private String type = "Mean";
    @XmlAttribute private String interval = "1 second";
    @XmlAttribute private String eventParts = "";

    public AggregationConfiguration() {}

    public AggregationConfiguration(int aggregationID, int patternID, int labelIndex, String interval, String type, String eventParts) {
        this.aggregationID = aggregationID;
        this.patternID = patternID;
        this.labelIndex = labelIndex;
        this.interval = interval;
        this.type = type;
        this.eventParts = eventParts;
    }
    
    public void setAggregationID(int aggregationID) {
        this.aggregationID = aggregationID;
    }
    
    public int getAggregationID() {
        return aggregationID;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public int getPatternID() {
        return patternID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getEventParts() {
        return eventParts;
    }

    public void setEventParts(String eventParts) {
        this.eventParts = eventParts;
    }

    @Override public String toString() {
        return "AggregationConfiguration [patternID=" +
               patternID +
               ", labelIndex=" +
               labelIndex +
               ", type=" +
               type +
               ", interval=" +
               interval +
               ", eventParts=" +
               eventParts +
               "]";
    }

}
