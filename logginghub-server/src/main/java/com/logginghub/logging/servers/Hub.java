package com.logginghub.logging.servers;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;

/**
 * A log event hub that deals with dispatching log events to listeners. It doesn't deal with
 * any connection management or any of that, so it doesn't do much by itself.
 * @author admin
 *
 */
public class Hub implements LogEventListener, LogEventSource
{
    private List<LogEventListener> m_listeners = new ArrayList<LogEventListener>();
    
    private List<LogEventListener> m_listenersToAdd = new ArrayList<LogEventListener>();
    private List<LogEventListener> m_listenersToRemove = new ArrayList<LogEventListener>();

    // //////////////////////////////////////////////////////////////////
    // LogEventListener implementation
    // //////////////////////////////////////////////////////////////////

    public void onNewLogEvent(LogEvent event)
    {
        synchronized (m_listeners)        
        {
            m_listeners.addAll(m_listenersToAdd);
            m_listenersToAdd.clear();
        
            for(LogEventListener listener : m_listeners)
            {
                listener.onNewLogEvent(event);
            }
            
            m_listeners.removeAll(m_listenersToRemove);
            m_listenersToRemove.clear();
        }
    }

    // //////////////////////////////////////////////////////////////////
    // LogEventSource implementation
    // //////////////////////////////////////////////////////////////////

    public void addLogEventListener(LogEventListener listener)
    {
        // m_listeners.add(listener);
        m_listenersToAdd.add(listener);
    }

    public void removeLogEventListener(LogEventListener listener)
    {
        //m_listeners.remove(listener);
        m_listenersToRemove.add(listener);
    }
}
