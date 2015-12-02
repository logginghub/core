package com.logginghub.utils.logging;

import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;

public interface LoggerStream {
    void onNewLogEvent(LogEvent event);
    void onNewLogEvent(EventContext eventContext);
}
