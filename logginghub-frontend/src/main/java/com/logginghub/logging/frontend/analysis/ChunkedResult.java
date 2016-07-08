package com.logginghub.logging.frontend.analysis;

import com.logginghub.utils.logging.Logger;

public class ChunkedResult {

    private final long startOfCurrentChunk;
    private final long chunkDuration;
    private final double value;
    private final String mode;
    private final String label;
    private final String groupBy;
    private String source;
    private String aggregationName;

    public ChunkedResult(long startOfCurrentChunk, long chunkDuration, double value, String mode, String label, String groupBy, String source) {
        this.startOfCurrentChunk = startOfCurrentChunk;
        this.chunkDuration = chunkDuration;
        this.value = value;
        this.mode = mode;
        this.label = label;
        this.groupBy = groupBy;
        this.source = source;
    }

    public void setAggregationName(String aggregationName) {
        this.aggregationName = aggregationName;
    }

    public String getAggregationName() {
        return aggregationName;
    }

    public String getMode() {
        return mode;
    }

    public long getChunkDuration() {
        return chunkDuration;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public String getLabel() {
        return label;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getStartOfCurrentChunk() {
        return startOfCurrentChunk;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ChunkedResult [startOfCurrentChunk=" +
               Logger.toDateString(startOfCurrentChunk) +
               ", chunkDuration=" +
               chunkDuration +
               ", value=" +
               value +
               ", source=" +
               source +
               ", aggregationName=" +
               aggregationName +
               ", label=" +
               label +
               ", groupBy=" +
               groupBy +
               ", mode=" +
               mode +
               "]";
    }
}
