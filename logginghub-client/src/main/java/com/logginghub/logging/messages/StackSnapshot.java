package com.logginghub.logging.messages;

import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.Arrays;

public class StackSnapshot implements SerialisableObject, TimeProvider {

    private InstanceKey instanceKey;

    private long timestamp;
    private StackTrace[] traces;

    public StackSnapshot() {}

    public StackSnapshot(InstanceKey instanceKey, long timestamp, StackTrace[] traces) {
        super();
        this.instanceKey = instanceKey;
        this.timestamp = timestamp;
        this.traces = traces;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTraces(StackTrace[] traces) {
        this.traces = traces;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public StackTrace[] getTraces() {
        return traces;
    }

    public void read(SofReader reader) throws SofException {
        this.instanceKey = (InstanceKey) reader.readObject(1);
        this.timestamp = reader.readLong(2);
        this.traces = (StackTrace[]) reader.readObjectArray(3, StackTrace.class);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, instanceKey);
        writer.write(2, timestamp);
        writer.write(3, traces, StackTrace.class);
    }

    @Override public String toString() {
        return "StackSnapshot [instanceKey=" +
               instanceKey +
               ", timestamp=" +
               timestamp +
               ", traces=" +
               Arrays.toString(traces) +
               "]";
    }

    @Override public long getTime() {
        return timestamp;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey;
    }
}
