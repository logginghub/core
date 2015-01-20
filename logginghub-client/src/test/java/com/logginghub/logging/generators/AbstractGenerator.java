package com.logginghub.logging.generators;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventGenerator;
import com.logginghub.logging.interfaces.AbstractLogEventSource;

public abstract class AbstractGenerator extends AbstractLogEventSource implements LogEventGenerator 
{
    public abstract void onNewLogEvent(LogEvent event);
}
