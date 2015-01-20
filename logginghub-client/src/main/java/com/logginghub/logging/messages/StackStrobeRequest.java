package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class StackStrobeRequest implements SerialisableObject {

    private String instanceSelector = "*";
    private int snapshotCount = 10;
    private long intervalLength = 10000;

    public StackStrobeRequest() {}
    
    public StackStrobeRequest(String instanceSelector, int snapshotCount, long intervalLength) {
        super();
        this.instanceSelector = instanceSelector;
        this.snapshotCount = snapshotCount;
        this.intervalLength = intervalLength;
    }

    public void read(SofReader reader) throws SofException {
        this.snapshotCount = reader.readInt(1);
        this.intervalLength = reader.readLong(2);
        this.instanceSelector = reader.readString(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, snapshotCount);
        writer.write(2, intervalLength);
        writer.write(3, instanceSelector);
    }

    public String getInstanceSelector() {
        return instanceSelector;
    }

    public void setInstanceSelector(String instanceSelector) {
        this.instanceSelector = instanceSelector;
    }

    public int getSnapshotCount() {
        return snapshotCount;
    }

    public void setSnapshotCount(int snapshotCount) {
        this.snapshotCount = snapshotCount;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(long intervalLength) {
        this.intervalLength = intervalLength;
    }

    @Override public String toString() {
        return "StackStrobeRequest [instanceSelector=" +
               instanceSelector +
               ", snapshotCount=" +
               snapshotCount +
               ", intervalLength=" +
               intervalLength +
               "]";
    }

    
}
