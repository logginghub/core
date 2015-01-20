package com.logginghub.logging.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.logginghub.utils.ExpandingByteBuffer;

public class CompressingCodex
{

    public ByteBuffer uncompress(ByteBuffer buffer)
    {
        // Create the decompressor and give it the data to compress
        Inflater decompressor = new Inflater();

        int size = buffer.getInt();
        byte[] compressedData = new byte[size];
        buffer.get(compressedData);

        decompressor.setInput(compressedData);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);

        // Decompress the data
        byte[] buf = new byte[1024];
        while(!decompressor.finished())
        {
            try
            {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            catch (DataFormatException e)
            {
            }
        }
        try
        {
            bos.close();
        }
        catch (IOException e)
        {
        }

        byte[] decompressedData = bos.toByteArray();
        return ByteBuffer.wrap(decompressedData);
    }

    public void compress(ExpandingByteBuffer expandingBuffer, int startingPosition)
    {
        int length = expandingBuffer.getBuffer().position() - startingPosition;

        byte[] dataToCompress = new byte[length];

        expandingBuffer.getBuffer().position(startingPosition);
        expandingBuffer.getBuffer().get(dataToCompress, 0, length);

        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(dataToCompress);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // You cannot use an array that's the same size as the orginal because
        // there is no guarantee that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(dataToCompress.length);

        // Compress the data
        byte[] buf = new byte[1024];
        while(!compressor.finished())
        {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        try
        {
            bos.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to close compressing output stream", e);
        }

        // Get the compressed data
        byte[] compressedData = bos.toByteArray();

        expandingBuffer.getBuffer().position(startingPosition);

        expandingBuffer.putInt(compressedData.length);
        expandingBuffer.put(compressedData);
    }

}
