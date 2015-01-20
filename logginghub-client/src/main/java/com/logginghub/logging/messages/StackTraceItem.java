package com.logginghub.logging.messages;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class StackTraceItem implements SerialisableObject {

    private String className;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public StackTraceItem() {}
    
    public StackTraceItem(String className, String methodName, String fileName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void read(SofReader reader) throws SofException {
        this.className = reader.readString(1);
        this.methodName = reader.readString(2);
        this.fileName = reader.readString(3);
        this.lineNumber = reader.readInt(4);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, className);
        writer.write(2, methodName);
        writer.write(3, fileName);
        writer.write(4, lineNumber);
    }

    @Override public String toString() {
        return StringUtils.format("{}.{}({}:{})", className, methodName, fileName, lineNumber);
    }

    
}
