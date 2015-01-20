package com.logginghub.logging.modules;

import com.logginghub.logging.messages.AggregationType;

public class OpenIntervalKey {

    private String seriesKey;
    private int patternID;
    private int labelIndex;
    private AggregationType type;
    private long interval;

    @Override public String toString() {
        return "OpenIntervalKey [seriesKey=" +
               seriesKey +
               ", patternID=" +
               patternID +
               ", labelIndex=" +
               labelIndex +
               ", type=" +
               type +
               ", interval=" +
               interval +
               "]";
    }

    public String getSeriesKey() {
        return seriesKey;
    }

    public void setSeriesKey(String seriesKey) {
        this.seriesKey = seriesKey;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
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

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (interval ^ (interval >>> 32));
        result = prime * result + labelIndex;
        result = prime * result + patternID;
        result = prime * result + ((seriesKey == null) ? 0 : seriesKey.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OpenIntervalKey other = (OpenIntervalKey) obj;
        if (interval != other.interval) {
            return false;
        }
        if (labelIndex != other.labelIndex) {
            return false;
        }
        if (patternID != other.patternID) {
            return false;
        }
        if (seriesKey == null) {
            if (other.seriesKey != null) {
                return false;
            }
        }
        else if (!seriesKey.equals(other.seriesKey)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

}
