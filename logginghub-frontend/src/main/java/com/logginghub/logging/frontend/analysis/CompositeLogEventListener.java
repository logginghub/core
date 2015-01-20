package com.logginghub.logging.frontend.analysis;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;

public class CompositeLogEventListener implements LogEventListener
{

    private List<LogEventListener> listeners = new CopyOnWriteArrayList<LogEventListener>();

    public void onNewLogEvent(LogEvent event)
    {
        for (LogEventListener listener : listeners)
        {
            listener.onNewLogEvent(event);
        }
    }

    public void addLogEventListener(LogEventListener listener)
    {
        listeners.add(listener);
    }

    public void removeLogEventListener(LogEventListener listener)
    {
        listeners.remove(listener);
    }
}
