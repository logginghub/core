package com.logginghub.logging.messages;

import java.nio.ByteBuffer;

public interface CompressionStrategy {

    ByteBuffer compress(ByteBuffer currentBlock);
    ByteBuffer decompress(ByteBuffer currentBlock);

}
