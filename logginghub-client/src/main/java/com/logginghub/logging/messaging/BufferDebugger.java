package com.logginghub.logging.messaging;

import java.nio.ByteBuffer;

import com.logginghub.utils.HexDump;

public class BufferDebugger {

    private ByteBuffer buffer;

    public BufferDebugger(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override public String toString() {
        return HexDump.format(buffer);
    }
    
}
