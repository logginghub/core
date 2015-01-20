package com.logginghub.logging.modules.history;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;

public interface SerialisationStrategy {
    void serialise(ByteBuffer buffer, DefaultLogEvent t) throws IOException;
    LogEvent deserialise(ByteBuffer buffer) throws IOException;

}
