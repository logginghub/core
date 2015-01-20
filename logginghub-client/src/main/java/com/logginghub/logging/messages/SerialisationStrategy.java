package com.logginghub.logging.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.utils.sof.SerialisableObject;

public interface SerialisationStrategy {
    void serialise(ByteBuffer buffer, SerialisableObject t) throws IOException;
    SerialisableObject deserialise(ByteBuffer buffer) throws IOException;

}
