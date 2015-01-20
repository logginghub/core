package com.logginghub.logging.utils;

import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.Bucket;

/**
 * A convenient helper that listens to new logging messages and drops them into a bucket. It helps
 * bridging between asynchronous and synchronous models.
 * 
 * @author admin
 * 
 */
public class LoggingMessageBucket extends Bucket<LoggingMessage> implements LoggingMessageListener {
    public void onNewLoggingMessage(LoggingMessage event) {
        add(event);
    }

}
