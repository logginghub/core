package com.logginghub.logging.utils;

import java.util.Collection;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.Bucket;

/**
 * A convenient helper that listens to new events and drops them into a bucket.
 * It helps bridging between asynchronous and synchronous models.
 * 
 * @author admin
 * 
 */
public class LogEventBucket extends Bucket<LogEvent> implements LogEventListener {
    public LogEventBucket() {
        super();
    }

    public LogEventBucket(String name) {
        super(name);
    }

    public void onNewLogEvent(LogEvent event) {
        add(event);
    }

    public void addAll(Collection<LogEvent> logEvents) {
        addAll(logEvents);
    }
}
