package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SerialisationStrategy {
    void serialise(ByteBuffer buffer, SerialisableObject t) throws IOException;
    SerialisableObject deserialise(ByteBuffer buffer) throws IOException;

}
