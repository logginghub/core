package com.logginghub.logging.encoding;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;

public interface LogEventWriter {
    void write(LogEvent event);
    void close();
    byte[] encode(DefaultLogEvent event);
}
