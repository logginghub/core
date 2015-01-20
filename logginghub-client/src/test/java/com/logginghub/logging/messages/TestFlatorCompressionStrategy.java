package com.logginghub.logging.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.logging.messages.FlatorCompressionStrategy;

public class TestFlatorCompressionStrategy {
    @Test public void test() throws Exception {
        FlatorCompressionStrategy compressionStrategy = new FlatorCompressionStrategy();
        
        ByteBuffer input = ByteBuffer.wrap("Hello world".getBytes());
        input.compact();
        
        ByteBuffer compressed = compressionStrategy.compress(input);
        
        ByteBuffer output = compressionStrategy.decompress(compressed);
        byte[] decompressedArray = output.array();
        assertThat(new String(decompressedArray), is("Hello world"));
    }
}
