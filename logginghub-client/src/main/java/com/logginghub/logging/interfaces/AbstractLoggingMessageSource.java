package com.logginghub.logging.interfaces;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventCollection;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventCollectionMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;

/**
 * Abstract helper that deals with the listener management for combined logging message and log
 * event sources.
 * 
 * @author admin
 * 
 */
public class AbstractLoggingMessageSource extends AbstractLogEventSource implements LoggingMessageSource, LogEventSource {
   
    private List<LoggingMessageListener> messageListeners = new CopyOnWriteArrayList<LoggingMessageListener>();

    public void addLoggingMessageListener(LoggingMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("You aren't allowed to add a null listener");
        }

        messageListeners.add(listener);
    }

    public void removeLoggingMessageListener(LoggingMessageListener listener) {
        messageListeners.remove(listener);
    }

    protected void fireNewMessage(LoggingMessage message) {
        for (LoggingMessageListener listener : messageListeners) {
            listener.onNewLoggingMessage(message);
        }

        if (message instanceof LogEventMessage) {
            LogEvent event = ((LogEventMessage) message).getLogEvent();
            fireNewLogEvent(event);
        }
        else if (message instanceof LogEventCollectionMessage) {
            LogEventCollection collection = ((LogEventCollectionMessage) message).getLogEventCollection();
            for (LogEvent event : collection) {
                fireNewLogEvent(event);
            }
        }
    }
}
