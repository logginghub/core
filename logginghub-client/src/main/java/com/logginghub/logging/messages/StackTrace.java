package com.logginghub.logging.messages;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class StackTrace implements SerialisableObject {

    private StackTraceItem[] items;
    
    private String threadName;
    private long threadID;
    private String threadState;
    
    public StackTrace() {}
    
    public StackTrace(String threadName, String threadState, long threadID, StackTraceItem[] items) {
        this.threadName = threadName;
        this.threadState = threadState;
        this.threadID = threadID;
        this.items = items;
    }

    public StackTraceItem[] getItems() {
        return items;
    }
    
    public long getThreadID() {
        return threadID;
    }
    
    public String getThreadState() {
        return threadState;
    }
    
    public String getThreadName() {
        return threadName;
    }

    public void read(SofReader reader) throws SofException {
        this.threadName = reader.readString(1);
        this.threadState = reader.readString(2);
        this.threadID = reader.readLong(3);
        this.items = (StackTraceItem[]) reader.readObjectArray(4, StackTraceItem.class);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, threadName);
        writer.write(2, threadState);
        writer.write(3, threadID);
        writer.write(4, items, StackTraceItem.class);
    }

    @Override public String toString() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("Thread '{}' ({}) - state {}", threadName, threadID, threadState);    
        return builder.toString();
    }
    
    public String toStringFull() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("Thread '{}' ({}) - state {}", threadName, threadID, threadState);
        for (StackTraceItem stackTraceItem : items) {
            builder.appendLine(stackTraceItem);
        }
        return builder.toString();
    }

    public String formatStack() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        for (StackTraceItem stackTraceItem : items) {
            builder.appendLine(stackTraceItem);
        }
        return builder.toString();
    }

    
}
