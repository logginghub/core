package com.logginghub.logging.repository;

import java.io.IOException;

import com.logginghub.logging.LogEvent;

public interface LogEventWriter {
    void open() throws IOException;
    void write(LogEvent event) throws IOException;
    void close() throws IOException;    
}
