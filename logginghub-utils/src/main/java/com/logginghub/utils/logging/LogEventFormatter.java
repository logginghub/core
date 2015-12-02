package com.logginghub.utils.logging;

import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;

public interface LogEventFormatter {
    String format(LogEvent event);
    String format(EventContext event);
}
