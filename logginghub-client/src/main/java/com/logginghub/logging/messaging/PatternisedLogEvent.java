package com.logginghub.logging.messaging;

import java.util.Arrays;

import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PatternisedLogEvent implements SerialisableObject, TimeProvider {

    private int patternID;
    private int level;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String[] variables;
    private String threadName;
    private long time;
    private String loggerName;
    private String sourceHost;
    private String sourceAddress;
    private String sourceApplication;
    private String channel;

    private int pid;

    public PatternisedLogEvent(int level, long time, long sequence, int patternID, String sourceApplication) {
        this.level = level;
        this.time = time;
        this.sequenceNumber = sequence;
        this.patternID = patternID;
        this.sourceApplication = sourceApplication;
    }

    public PatternisedLogEvent() {}

    public void read(SofReader reader) throws SofException {
        patternID = reader.readInt(0);
        time = reader.readLong(1);
        level = reader.readInt(2);
        variables = reader.readStringArray(3);
        sourceHost = reader.readString(4);
        sourceApplication = reader.readString(5);
        pid = reader.readInt(6);
        threadName = reader.readString(7);
        loggerName = reader.readString(8);
        sourceAddress = reader.readString(9);
        channel = reader.readString(10);
        sourceClassName = reader.readString(11);
        sourceMethodName = reader.readString(12);
        sequenceNumber = reader.readLong(13);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, patternID);
        writer.write(1, time);
        writer.write(2, level);
        writer.write(3, variables);
        writer.write(4, sourceHost);
        writer.write(5, sourceApplication);
        writer.write(6, pid);
        writer.write(7, threadName);
        writer.write(8, loggerName);
        writer.write(9, sourceAddress);
        writer.write(10, channel);
        writer.write(11, sourceClassName);
        writer.write(12, sourceMethodName);
        writer.write(13, sequenceNumber);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String[] getVariables() {
        return variables;
    }

    public void setVariables(String[] variables) {
        this.variables = variables;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    // TODO : remove all this sizeof stuff, its daft and inaccurate
    @Deprecated public int estimateSizeOf() {

        // time + sequence + level + pid + (12 * reference)
        int sizeof = 62;

        int nonNullStrings = 0;
        int lengths = 0;

        if (sourceHost != null) {
            nonNullStrings++;
            lengths += sourceHost.length();
        }

        if (sourceApplication != null) {
            nonNullStrings++;
            lengths += sourceApplication.length();
        }

        if (threadName != null) {
            nonNullStrings++;
            lengths += threadName.length();
        }

        if (loggerName != null) {
            nonNullStrings++;
            lengths += loggerName.length();
        }

        if (sourceAddress != null) {
            nonNullStrings++;
            lengths += sourceAddress.length();
        }

        if (channel != null) {
            nonNullStrings++;
            lengths += channel.length();
        }

        if (sourceClassName != null) {
            nonNullStrings++;
            lengths += sourceClassName.length();
        }

        if (sourceMethodName != null) {
            nonNullStrings++;
            lengths += sourceMethodName.length();
        }

        sizeof += (nonNullStrings * 36) + (lengths * 2);

        return sizeof;

    }

    /**
     * Gets the variable at this labelIndex, or null if the index is wrong
     * 
     * @param labelIndex
     * @return
     */
    public String getVariable(int labelIndex) {
        String variable;
        if (variables != null && labelIndex >= 0 && labelIndex < variables.length) {
            variable = variables[labelIndex];
        }
        else {
            variable = null;
        }
        return variable;

    }

    @Override public String toString() {
        return "PatternisedLogEvent [time=" +
               Logger.toDateString(time) +
               ", patternID=" +
               patternID +
               ", sourceHost=" +
               sourceHost +
               ", sourceApplication=" +
               sourceApplication +
               ", pid=" +
               pid +
               ", level=" +
               level +
               ", variables=" +
               Arrays.toString(variables) +
               "]";
    }

}
