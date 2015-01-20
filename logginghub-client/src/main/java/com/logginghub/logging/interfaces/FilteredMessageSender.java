package com.logginghub.logging.interfaces;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;

public interface FilteredMessageSender extends LoggingMessageSender, Destination<LogEvent> {
    int getLevelFilter();
    int getConnectionType();
}
