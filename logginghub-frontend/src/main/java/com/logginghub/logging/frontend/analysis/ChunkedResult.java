package com.logginghub.logging.frontend.analysis;

import com.logginghub.utils.logging.Logger;

public class ChunkedResult {

    private final long startOfCurrentChunk;
    private final long chunkDuration;
    private final double value;
    private String source;

    public ChunkedResult(long startOfCurrentChunk, long chunkDuration, double value, String source) {
        this.startOfCurrentChunk = startOfCurrentChunk;
        this.chunkDuration = chunkDuration;
        this.value = value;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public long getChunkDuration() {
        return chunkDuration;
    }

    public long getStartOfCurrentChunk() {
        return startOfCurrentChunk;
    }

    public double getValue() {
        return value;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override public String toString() {
        return "ChunkedResult [startOfCurrentChunk=" + Logger.toDateString(startOfCurrentChunk) + ", chunkDuration=" + chunkDuration + ", value=" + value + ", source=" + source + "]";
    }
}
