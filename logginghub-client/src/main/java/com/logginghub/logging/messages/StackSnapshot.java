package com.logginghub.logging.messages;

import java.util.Arrays;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class StackSnapshot implements SerialisableObject {

    private String environment;
    private String host;
    private String instanceType;
    private int instanceNumber;
    private long timestamp;
    private StackTrace[] traces;

    public StackSnapshot() {}

    public StackSnapshot(String environment, String host, String instanceType, int instanceNumber, long timestamp, StackTrace[] traces) {
        super();
        this.environment = environment;
        this.host = host;
        this.instanceType = instanceType;
        this.instanceNumber = instanceNumber;
        this.timestamp = timestamp;
        this.traces = traces;
    }

    public String buildKey() {
        return StringUtils.format("{}.{}.{}.{}", environment, host, instanceType, instanceNumber);
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTraces(StackTrace[] traces) {
        this.traces = traces;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getHost() {
        return host;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public StackTrace[] getTraces() {
        return traces;
    }

    public void read(SofReader reader) throws SofException {
        this.environment = reader.readString(1);
        this.host = reader.readString(2);
        this.instanceType = reader.readString(3);
        this.instanceNumber = reader.readInt(4);
        this.timestamp = reader.readLong(5);
        this.traces = (StackTrace[]) reader.readObjectArray(6, StackTrace.class);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, environment);
        writer.write(2, host);
        writer.write(3, instanceType);
        writer.write(4, instanceNumber);
        writer.write(5, timestamp);
        writer.write(6, traces, StackTrace.class);
    }

    @Override public String toString() {
        return "StackSnapshot [environment=" +
               environment +
               ", host=" +
               host +
               ", instanceType=" +
               instanceType +
               ", instanceNumber=" +
               instanceNumber +
               ", timestamp=" +
               timestamp +
               ", traces=" +
               Arrays.toString(traces) +
               "]";
    }

}
