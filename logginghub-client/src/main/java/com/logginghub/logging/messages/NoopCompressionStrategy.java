package com.logginghub.logging.messages;

import java.nio.ByteBuffer;

import com.logginghub.logging.kanzi.ByteFunction;
import com.logginghub.logging.kanzi.IndexedByteArray;
import com.logginghub.logging.kanzi.LZ4Codec;

public class NoopCompressionStrategy implements CompressionStrategy {

    public ByteBuffer compress(ByteBuffer sourceBuffer) {
        return sourceBuffer;
    }

    public ByteBuffer decompress(ByteBuffer sourceBuffer) {
        return sourceBuffer;
    }

}
