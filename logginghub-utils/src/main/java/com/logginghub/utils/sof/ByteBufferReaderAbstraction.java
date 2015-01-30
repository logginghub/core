package com.logginghub.utils.sof;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.logginghub.utils.logging.Logger;

public class ByteBufferReaderAbstraction implements ReaderAbstraction {

    private static final Logger logger = Logger.getLoggerFor(ByteBufferReaderAbstraction.class);
    private ByteBuffer buffer;

    public ByteBufferReaderAbstraction(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void skip(long i) {
        buffer.position((int) (buffer.position() + i));
        logger.fine("Skipped {} bytes to position '{}", i, buffer.position());
    }

    public short readShort() {
        logger.fine("Reading short", buffer.position());
        return buffer.getShort();
    }

    public long readLong() {
        logger.fine("Reading long", buffer.position());
        return buffer.getLong();
    }

    public int readInt() {
        logger.fine("Reading int", buffer.position());
        return buffer.getInt();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    public char readChar() {
        return buffer.getChar();
    }

    public byte readByte() {
        return buffer.get();
    }

    public boolean readBoolean() {
        return buffer.get() == 1 ? true : false;
    }

    public void read(byte[] contents) {
        buffer.get(contents);
    }

    public int readUnsignedShort() throws IOException {
        int value = (int) (0x0000ffff & buffer.getShort());
        return value;
    }

    public short readUnsignedByte() throws IOException {
        short value = (short) (0x00ff & buffer.get());
        return value;
    }

    public boolean hasMore() {
        // TODO : this just doesn't look right - there might be loads of extra stuff on the end of
        // the buffer?!
        return buffer.remaining() > 0;
    }

    public long getPosition() {
        return buffer.position();
    }

    public void read(byte[] array, int position, int length) throws IOException {
        buffer.put(array, position, length);
    }

    @Override public void setPosition(long position) {
        buffer.position((int)position);
    }

    @Override public String toString() {
        return buffer.toString();         
    }
};