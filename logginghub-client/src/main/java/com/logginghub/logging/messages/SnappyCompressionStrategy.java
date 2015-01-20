package com.logginghub.logging.messages;

import java.nio.ByteBuffer;

import com.logginghub.logging.kanzi.ByteFunction;
import com.logginghub.logging.kanzi.IndexedByteArray;
import com.logginghub.logging.kanzi.SnappyCodec;

public class SnappyCompressionStrategy implements CompressionStrategy {

    public ByteBuffer compress(ByteBuffer sourceBuffer) {

        sourceBuffer.flip();

        ByteFunction codec = new SnappyCodec(0);
        byte[] input = new byte[sourceBuffer.remaining()];
        sourceBuffer.get(input);
        
        int decompressedLength = input.length;
        byte[] output = new byte[codec.getMaxEncodedLength(decompressedLength)];
                        
        IndexedByteArray source = new IndexedByteArray(input,0);
        IndexedByteArray destination = new IndexedByteArray(output, 0);
        codec.forward(source, destination);

        ByteBuffer destinationBuffer = ByteBuffer.allocate(destination.index + 4);
        destinationBuffer.putInt(decompressedLength);
        destinationBuffer.put(output, 0, destination.index);
        
        // Flip the buffer so people can read the compressed data
        destinationBuffer.flip();

        return destinationBuffer;

    }

    public ByteBuffer decompress(ByteBuffer sourceBuffer) {
        
        int decompressedSize = sourceBuffer.getInt();
        byte[] destinationBytes = new byte[decompressedSize];
        
        ByteBuffer destinationByteBuffer = ByteBuffer.wrap(destinationBytes);

        ByteFunction codec = new SnappyCodec(sourceBuffer.remaining());
        
        IndexedByteArray source = new IndexedByteArray(sourceBuffer.array(), sourceBuffer.position());
        IndexedByteArray destination = new IndexedByteArray(destinationBytes, 0);
        codec.inverse(source, destination);

        return destinationByteBuffer;
    }

}
