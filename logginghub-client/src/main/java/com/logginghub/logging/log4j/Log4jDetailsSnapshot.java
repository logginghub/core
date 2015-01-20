package com.logginghub.logging.log4j;

import java.util.Hashtable;

import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import com.logginghub.utils.TimeProvider;

/**
 * Captures some aspects of the log events so we can dispatch them correctly later on.
 * 
 * @author James
 * 
 */
public class Log4jDetailsSnapshot {
    private String className;
    private String fileName;
    private String lineNumber;
    private String methodName;
    private String threadName;
    private LoggingEvent loggingEvent;

    // Diagnostic contexts from log4j. Should we generalise them here?
    private String ndc;
    private Hashtable<String, Object> mdc;
    private long timestamp;

    @SuppressWarnings("unchecked") public static Log4jDetailsSnapshot fromLoggingEvent(LoggingEvent loggingEvent,
                                                                                       TimeProvider timeProvider,
                                                                                       boolean captureLocationInformation) {
        Log4jDetailsSnapshot snapshot = new Log4jDetailsSnapshot();

        if (captureLocationInformation) {
            LocationInfo locationInformation = loggingEvent.getLocationInformation();
            snapshot.className = locationInformation.getClassName();
            snapshot.fileName = locationInformation.getFileName();
            snapshot.lineNumber = locationInformation.getLineNumber();
            snapshot.methodName = locationInformation.getMethodName();
        }
        
        snapshot.threadName = loggingEvent.getThreadName();
        snapshot.loggingEvent = loggingEvent;
        snapshot.mdc = MDC.getContext();
        snapshot.ndc = NDC.get();

        if (timeProvider != null) {
            snapshot.timestamp = timeProvider.getTime();
        }
        else {
            snapshot.timestamp = loggingEvent.timeStamp;
        }

        return snapshot;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Hashtable<String, Object> getMdc() {
        return mdc;
    }

    public String getNdc() {
        return ndc;
    }

    public LoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

    public String getClassName() {
        return className;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public String getThreadName() {
        return threadName;
    }

}
