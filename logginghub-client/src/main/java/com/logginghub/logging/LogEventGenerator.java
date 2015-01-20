package com.logginghub.logging;

import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;



/**
 * Interface for objects that receive log events and may generate new events as
 * result. The events are fired back through the listener added, and
 * maybe fired on any thread including the one that calls onNewLogEvent.
 * 
 * @author admin
 */
public interface LogEventGenerator extends LogEventListener, LogEventSource
{
    /*
    private LogEventListener m_listener;

    public LogEventGenerator(LogEventListener listener)
    {
        m_listener = listener;
    }
    
    protected void fireNewLogEvent(LogEvent event)
    {
        m_listener.onNewLogEvent(event);
    }
    */
    
//    public void onNewLogEvent(LogEvent event);
//    public void addLogEventListener(LogEventListener listener);
//    public void removeLogEventListener(LogEventListener listener);
}
