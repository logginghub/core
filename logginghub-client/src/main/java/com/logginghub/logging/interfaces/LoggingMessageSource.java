package com.logginghub.logging.interfaces;

import com.logginghub.logging.listeners.LoggingMessageListener;

public interface LoggingMessageSource
{
    public void addLoggingMessageListener(LoggingMessageListener listener);
    public void removeLoggingMessageListener(LoggingMessageListener listener);
}
