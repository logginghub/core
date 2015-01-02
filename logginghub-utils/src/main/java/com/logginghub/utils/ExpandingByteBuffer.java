package com.logginghub.utils;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpandingByteBuffer {
    private static Logger logger = Logger.getLogger(ExpandingByteBuffer.class.getName());
    private final static int startingSize = 4096;

    private static final int sizeofByte = 1;
    private static final int sizeofChar = 2;
    private static final int sizeofBoolean = 1;
    private static final int sizeofShort = 2;
    private static final int sizeofInt = 4;
    private static final int sizeofFloat = 4;
    private static final int sizeofLong = 8;
    private static final int sizeofDouble = 8;

    private ByteBuffer buffer;

    public ExpandingByteBuffer(int initialCapacity) {
        buffer = allocate(initialCapacity);
    }

    public ExpandingByteBuffer() {
        this(startingSize);
    }

    /**
     * Returns the byte buffer with whatever was in there last time, and at least
     * <code>capacity</code> bytes of remaining space to write to
     */
    public ByteBuffer getBuffer(int extraCapacity) {
        while (buffer.remaining() < extraCapacity) {
            doubleSize();
        }
        // int newCapacity = m_buffer.capacity() - m_buffer.remaining() + extraCapacity;
        //
        // m_buffer = resize(newCapacity);
        // }

        return buffer;
    }

    private ByteBuffer resize(int newCapacity) {
        if (newCapacity > 800000) {
            System.out.println(newCapacity);
        }

        if (logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("Resizing buffer, current state '%s' newCapacity will be %d bytes", buffer, newCapacity));
        }
        ByteBuffer newBuffer = allocate(newCapacity);

        // Flip to read from it
        buffer.flip();

        // Dump the whole lot into the new buffer
        newBuffer.put(buffer);

        return newBuffer;
    }

    public ByteBuffer doubleSize() {
        int capacity = buffer.capacity();
        int newCapacity = capacity * 2;

        if (newCapacity == 0) {
            newCapacity = 1;
        }

        buffer = resize(newCapacity);
        return buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Insert a byte at the index specified, moving the rest of the data.
     * 
     * @param index
     * @param value
     */
    public void insertByte(int index, byte value) {
        int currentPosition = buffer.position();

        int freeSpace = buffer.capacity() - currentPosition;
        if (freeSpace < 2) {
            doubleSize();
        }

        int lengthOfDataToMove = currentPosition - index;
        byte[] data = new byte[lengthOfDataToMove];
        buffer.position(index);

        buffer.get(data, 0, lengthOfDataToMove);

        buffer.position(index);
        buffer.put(value);
        buffer.put(data);
    }

    public void flip() {
        buffer.flip();
    }

    @Override public String toString() {
        return buffer.toString();
    }

    // //////////////////////////////////////////////////////////////////
    // Decorated put methods
    // //////////////////////////////////////////////////////////////////

    public void setInt(int index, int length) {
        // Assumes the index is correctly set!
        buffer.putInt(index, length);
    }

    public void putInt(int value) {
        if (buffer.remaining() < sizeofInt) {
            getBuffer(sizeofInt).putInt(value);
        }
        else {
            buffer.putInt(value);
        }
    }

    public void put(byte value) {
        if (buffer.remaining() < sizeofByte) {
            getBuffer(sizeofByte).put(value);
        }
        else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(String.format("Before byte written, buffer state is '%s'", buffer));
            }

            synchronized (buffer) {
                buffer.put(value);
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(String.format("Byte written, buffer state is now '%s'", buffer));
            }
        }
    }

    public void putDouble(double value) {
        getBuffer(sizeofDouble).putDouble(value);
    }
    
    public void putFloat(float value) {
        getBuffer(sizeofFloat).putFloat(value);
    }

    public void putBoolean(boolean b) {
        getBuffer(sizeofBoolean).put((byte) (b ? 1 : 0));
    }
    
    public void putChar(char  c) {
        getBuffer(sizeofChar).putChar(c);
    }

    public static String getUTF8(ByteBuffer buffer) {
        int size = buffer.getInt();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return new String(bytes);
    }

    public void putUTF8(String series) {
        byte[] bytes = series.getBytes();
        int size = 4 + bytes.length;
        ByteBuffer buffer = getBuffer(size);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    public void putShort(short lengthPlaceHolder) {
        getBuffer(sizeofShort).putShort(lengthPlaceHolder);
    }

    public void putLong(long value) {
        if (buffer.remaining() < sizeofLong) {
            getBuffer(sizeofLong).putLong(value);
        }
        else {
            buffer.putLong(value);
        }
    }

    public void put(byte[] value) {
        if (buffer.remaining() < value.length) {
            getBuffer(value.length).put(value);
        }
        else {
            buffer.put(value);
        }
    }

    /**
     * @return a copy of the contents of the underlying buffer, from the current position to the
     *         data limit.
     */
    public byte[] getContents() {
        // int position = m_buffer.position();
        byte[] contents = new byte[buffer.remaining()];
        buffer.get(contents);
        // m_buffer.position(position);

        return contents;
    }

    public int position() {
        return buffer.position();
    }

    public void position(int position) {
        buffer.position(position);
    }

    public void dump() {
        byte[] contents = getContents();
        HexDump.dump(contents);
    }

    public byte[] getContents(int startPosition, int endPosition) {
        int savedPosition = buffer.position();
        int savedLimit = buffer.limit();

        buffer.position(startPosition);
        buffer.limit(endPosition);

        byte[] contents = getContents();

        buffer.position(savedPosition);
        buffer.limit(savedLimit);

        return contents;
    }

    /**
     * Rip this section out of the bytebuffer.
     * 
     * @param startPosition
     * @param endPosition
     * @return
     */
    public byte[] extract(int startPosition, int endPosition) {
        int savedPosition = buffer.position();
        int savedLimit = buffer.limit();

        buffer.position(startPosition);
        buffer.limit(endPosition);

        byte[] contents = getContents();

        buffer.position(savedPosition);
        buffer.limit(savedLimit);

        return contents;
    }

    public int limit() {
        return buffer.limit();
    }

    public void put(byte[] bytes, int offset, int length) {
        getBuffer(length - offset).put(bytes, offset, length);
    }

    public void compact() {
        buffer.compact();
    }

    public void clear() {
        buffer.clear();
    }

    public int remaining() {
        return buffer.remaining();
    }

    public void limit(int friggedSize) {
        buffer.limit(friggedSize);
    }

    private ByteBuffer allocate(int capacity) {
        return ByteBuffer.allocateDirect(capacity);
    }

    public void put(ExpandingByteBuffer src) {
        getBuffer(src.remaining()).put(src.buffer);
    }

    public void put(ByteBuffer tempBuffer) {
        getBuffer(tempBuffer.remaining()).put(tempBuffer);
    }

}
