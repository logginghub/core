package com.logginghub.logging.encoding;

import java.io.InputStream;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;

public interface LogEventReader {

    void readAll(InputStream stream, Destination<LogEvent> destination);
    
}
