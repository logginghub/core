package com.logginghub.utils.sof;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.utils.SizeOf;
import com.logginghub.utils.logging.Logger;

public class CountingWriterAbstraction implements WriterAbstraction {

    private static final Logger logger = Logger.getLoggerFor(CountingWriterAbstraction.class);
    private int length = 0;

    public void reset() {
        length = 0;
    }

    public void writeShort(short value) throws IOException {
        length += SizeOf.shortSize;
    }

    public void writeByte(byte value) throws IOException {
        // logger.fine("Writing byte at position '{}'", length);
        length += SizeOf.byteSize;
    }

    public void writeUnsignedByte(int value) throws IOException {
        length += SizeOf.byteSize;
    }

    public void writeInt(int value) throws IOException {
        length += SizeOf.intSize;
    }

    public void writeLong(long value) throws IOException {
        length += SizeOf.longSize;
    }

    public void writeDouble(double value) throws IOException {
        length += SizeOf.doubleSize;
    }

    public void writeFloat(float value) throws IOException {
        length += SizeOf.floatSize;
    }

    public void write(byte[] value) throws IOException {
        length += value.length;
    }

    public void writeBoolean(boolean value) throws IOException {
        length += SizeOf.booleanSize;
    }

    public void writeChar(char value) throws IOException {
        length += SizeOf.charSize;
    }

    public void writeUnsignedShort(int i) throws IOException {
        length += SizeOf.shortSize;
    }

    public int getLength() {
        return length;
    }

    public int getPosition() {
        return length;
    }

    public void writeBuffer(ByteBuffer tempBuffer) {
        length += tempBuffer.remaining();
    }

    public void write(byte[] value, int position, int length) throws IOException {
        this.length += length;
    }

}
