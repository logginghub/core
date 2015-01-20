package com.logginghub.logging.juli;

import java.util.logging.LogRecord;

import org.apache.log4j.spi.LoggingEvent;

import com.logginghub.utils.TimeProvider;

/**
 * Captures some aspects of the log events so we can dispatch them correctly later on.
 * 
 * @author James
 * 
 */
public class JULDetailsSnapshot {
    private String threadName;
    private LoggingEvent loggingEvent;

    public static JULDetailsSnapshot fromLoggingEvent(LogRecord record, TimeProvider timeProvider) {
        JULDetailsSnapshot snapshot = new JULDetailsSnapshot();
        snapshot.threadName = Thread.currentThread().getName();
        return snapshot;

    }

    public String getThreadName() {
        return threadName;
    }

    public LoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

}
