package com.logginghub.utils;

public class OffsetableSystemTimeProvider implements TimeProvider {

    private long offset;

    public void setOffset(long offset) {
        this.offset = offset;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public void incrementOffset(long increment) {
        offset += increment;
    }
    
    public long getTime() {
        return System.currentTimeMillis() + offset;
    }
}
