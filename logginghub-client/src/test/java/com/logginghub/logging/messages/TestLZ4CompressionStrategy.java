package com.logginghub.logging.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.logging.messages.LZ4CompressionStrategy;

public class TestLZ4CompressionStrategy {
    @Test public void test() throws Exception {
        LZ4CompressionStrategy compressionStrategy = new LZ4CompressionStrategy();
        
        ByteBuffer input = ByteBuffer.wrap("Hello world".getBytes());
        
        ByteBuffer compressed = compressionStrategy.compress(input);
        
        ByteBuffer output = compressionStrategy.decompress(compressed);
        byte[] decompressedArray = output.array();
        assertThat(new String(decompressedArray), is("Hello world"));
    }
}
