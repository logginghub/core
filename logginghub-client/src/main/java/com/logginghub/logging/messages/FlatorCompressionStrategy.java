package com.logginghub.logging.messages;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.logginghub.utils.FormattedRuntimeException;

public class FlatorCompressionStrategy implements CompressionStrategy {

    public ByteBuffer compress(ByteBuffer sourceBuffer) {

        sourceBuffer.flip();
        
        Deflater deflater = new Deflater();
        int decompressedLength = sourceBuffer.limit();
        deflater.setInput(sourceBuffer.array(), 0, decompressedLength);
        deflater.finish();

        ByteBuffer destinationBuffer = ByteBuffer.allocate(sourceBuffer.capacity() * 2);
        
        byte[] array = destinationBuffer.array();
        int compressedLength = deflater.deflate(array, 4, destinationBuffer.capacity() - 4);
        
        // Write the decompressed length to the front of the buffer
        destinationBuffer.putInt(0, decompressedLength);

        // Move the position to the end of the compressed data
        destinationBuffer.position(compressedLength + 4);     
        
        // Flip the buffer so people can read the compressed data
        destinationBuffer.flip();
        
        return destinationBuffer;

    }

    public ByteBuffer decompress(ByteBuffer sourceBlock) {
        
        int decompressedSize = sourceBlock.getInt();
        
        ByteBuffer destinationBuffer = ByteBuffer.allocate(decompressedSize);
        
        Inflater inflater = new Inflater();
        byte[] inputArray = sourceBlock.array();
        inflater.setInput(inputArray, 4, inputArray.length - 4);
        
        byte[] array = destinationBuffer.array();

        try {
            inflater.inflate(array);            
        }
        catch (DataFormatException e) {
            throw new FormattedRuntimeException(e, "Decompression failed");
        }
        
        destinationBuffer.position(decompressedSize);
        destinationBuffer.flip();
        
        return destinationBuffer;
         
    }

}
