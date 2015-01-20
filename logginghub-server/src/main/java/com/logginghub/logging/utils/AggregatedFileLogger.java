package com.logginghub.logging.utils;

import java.io.Closeable;

import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.interfaces.FilteredMessageSender;

public interface AggregatedFileLogger extends FilteredMessageSender, Closeable {
    void setFormatter(LogEventFormatter formatter);
    void setAutoNewline(boolean autoNewline);
    void setForceFlush(boolean forceFlush);
}
