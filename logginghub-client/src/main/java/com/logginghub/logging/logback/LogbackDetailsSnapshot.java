package com.logginghub.logging.logback;

import java.util.Map;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.ILoggingEvent;

import com.logginghub.utils.TimeProvider;

/**
 * Captures some aspects of the log events so we can dispatch them correctly
 * later on.
 * 
 * @author James
 * 
 */
public class LogbackDetailsSnapshot {
    private String className;
    // private String fileName;
    // private String lineNumber;
    private String methodName;
    private String threadName;
    private ILoggingEvent loggingEvent;

    // Logback diagnostic content
    private Map mdc;
    private long timestamp;

    @SuppressWarnings("unchecked") public static LogbackDetailsSnapshot fromLoggingEvent(ILoggingEvent record, TimeProvider timeProvider) {
        LogbackDetailsSnapshot snapshot = new LogbackDetailsSnapshot();

        StackTraceElement[] callerData = record.getCallerData();

        // LocationInfo locationInformation = record.getLocationInformation();
        snapshot.className = callerData[0].getClassName();
        // snapshot.fileName = locationInformation.getFileName();
        // snapshot.lineNumber = locationInformation.getLineNumber();
        snapshot.methodName = callerData[0].getMethodName();
        snapshot.threadName = record.getThreadName();
        snapshot.loggingEvent = record;
        snapshot.mdc = MDC.getCopyOfContextMap();
        
        // Logback doesn't support NDC
        // snapshot.ndc = NDC.get();

        if (timeProvider != null) {
            snapshot.timestamp = timeProvider.getTime();
        }
        else {
            snapshot.timestamp = record.getTimeStamp();
        }

        return snapshot;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map getMdc() {
        return mdc;
    }

    public ILoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

    public String getClassName() {
        return className;
    }

    //
    // public String getFileName() {
    // return fileName;
    // }
    //
    public String getMethodName() {
        return methodName;
    }

    //
    // public String getLineNumber() {
    // return lineNumber;
    // }

    public String getThreadName() {
        return threadName;
    }

}
