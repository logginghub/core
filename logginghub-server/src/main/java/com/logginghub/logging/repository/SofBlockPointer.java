package com.logginghub.logging.repository;

import com.logginghub.utils.TimeUtils;

public class SofBlockPointer {

    private long startTime = -1;
    private long endTime = -1;
    private long position = -1;
    private long length = -1;
    private long compressedLength;
    private long uncompressedLength;

    public SofBlockPointer(long startTime, long endTime, long position, long length, long compressedLength, long uncompressedLength) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.position = position;
        this.length = length;
        this.compressedLength = compressedLength;
        this.uncompressedLength = uncompressedLength;
    }

    public long getUncompressedLength() {
        return uncompressedLength;
    }
    
    public long getCompressedLength() {
        return compressedLength;
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

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override public String toString() {
        return "SofBlockPointer [startTime=" +
               startTime +
               ", endTime=" +
               endTime +
               ", position=" +
               position +
               ", length=" +
               length +
               ", compressedLength=" +
               compressedLength +
               ", uncompressedLength=" +
               uncompressedLength +
               "]";
    }

    public boolean overlaps(long start, long end) {
        return TimeUtils.overlaps(startTime, endTime, start, end);
    }

}
