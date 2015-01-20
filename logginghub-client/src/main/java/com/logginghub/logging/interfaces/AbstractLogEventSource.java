package com.logginghub.logging.interfaces;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Source;
import com.logginghub.utils.ExceptionPolicy.Policy;

/**
 * Abstract base class for log event sources; implements the basics of adding
 * and removing LogEventListeners. Uses copy-on-write to keep the listeners
 * thread safe, so adding and removing listeners may be expensive for large
 * numbers of listeners, but there is no overhead to firing new log event
 * notifications.
 * 
 * @author admin
 */
public class AbstractLogEventSource implements LogEventSource, Source<LogEvent> {

//    public enum ExceptionPolicy {
//        RethrowOnAny,
//        SendToHandler,
//        Ignore,
//        SystemOut
//    }

    private Multiplexer<LogEvent> logEventMultiplexer = new Multiplexer<LogEvent>();
    private List<LogEventListener> m_logEventListeners = new CopyOnWriteArrayList<LogEventListener>();
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.RethrowOnAny);

    public void addLogEventListener(LogEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener was null");
        }
        m_logEventListeners.add(listener);
    }

    public void removeLogEventListener(LogEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener was null");
        }
        m_logEventListeners.remove(listener);
    }

    protected void fireNewLogEvent(LogEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event was null");
        }

        for (LogEventListener listener : m_logEventListeners) {
            try {
                listener.onNewLogEvent(event);
            }
            catch (RuntimeException e) {
                exceptionPolicy.handle(e);
            }
        }
        
        logEventMultiplexer.send(event);
    }

    public void addDestination(Destination<LogEvent> listener) {
        logEventMultiplexer.addDestination(listener);        
    }

    public void removeDestination(Destination<LogEvent> listener) {
        logEventMultiplexer.removeDestination(listener);
    }
}
