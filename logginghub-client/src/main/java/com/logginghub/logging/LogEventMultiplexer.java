package com.logginghub.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.logging.Logger;

public class LogEventMultiplexer implements LogEventSource, LogEventListener, StreamListener<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(LogEventMultiplexer.class);
    private List<LogEventListener> listeners = new CopyOnWriteArrayList<LogEventListener>();
    private ExceptionHandler exceptionHandler = new ExceptionHandler() {
        public void handleException(String action, Throwable t) {
            t.printStackTrace();
        }
    };
    private boolean isDebug;

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void addLogEventListener(LogEventListener logEventListener) {
        listeners.add(logEventListener);
    }

    public void removeLogEventListener(LogEventListener logEventListener) {
        listeners.remove(logEventListener);
    }

    public void onNewLogEvent(LogEvent event) {
        if (isDebug) {
            logger.info(event.toString());
        }
        for (LogEventListener logEventListener : listeners) {
            try {
                logEventListener.onNewLogEvent(event);
            }
            catch (RuntimeException e) {
                exceptionHandler.handleException("Processing new log event", e);
            }
        }
    }

    public void clear() {
        listeners.clear();
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void onNewItem(LogEvent t) {
        onNewLogEvent(t);
    }

}
