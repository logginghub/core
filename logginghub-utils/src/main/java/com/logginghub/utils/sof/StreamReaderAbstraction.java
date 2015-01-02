package com.logginghub.utils.sof;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.logginghub.utils.SizeOf;
import com.logginghub.utils.StringUtils;

public class StreamReaderAbstraction implements ReaderAbstraction {

    private DataInputStream dataStream;

    private long limit;
    private long count = 0;

    public StreamReaderAbstraction(InputStream stream, long limit) {
        this.limit = limit;
        this.dataStream = new DataInputStream(stream);
    }

    public void skip(long i) throws IOException {

        long skipTracker = 0;

        while (skipTracker < i) {
            skipTracker += dataStream.skip(i - skipTracker);
        }

        count += i;
    }

    public short readShort() throws IOException {
        count += SizeOf.shortSize;
        return dataStream.readShort();

    }

    public long readLong() throws IOException {
        count += SizeOf.longSize;
        return dataStream.readLong();
    }

    public int readInt() throws IOException {
        count += SizeOf.intSize;
        return dataStream.readInt();
    }

    public float readFloat() throws IOException {
        count += SizeOf.floatSize;
        return dataStream.readFloat();
    }

    public double readDouble() throws IOException {
        count += SizeOf.doubleSize;
        return dataStream.readDouble();
    }

    public char readChar() throws IOException {
        count += SizeOf.charSize;
        return dataStream.readChar();
    }

    public byte readByte() throws IOException {
        count += SizeOf.byteSize;
        int read = dataStream.read();
        if (read == -1) {
            throw new EOFException(StringUtils.format("You've reached the end of the stream"));
        }
        else {
            return (byte) read;
        }
    }

    public boolean readBoolean() throws IOException {
        count += SizeOf.booleanSize;
        return dataStream.read() == 1 ? true : false;
    }

    public void read(byte[] array, int position, int length) throws IOException {
        // TESTME
        int read = 0;
        while (read < length) {
            read += dataStream.read(array, position + read, length);
        }
        count += read;
    }

    public void read(byte[] contents) throws IOException {
        int read = 0;
        while (read < contents.length) {
            read += dataStream.read(contents);
        }
        count += contents.length;
    }

    public int readUnsignedShort() throws IOException {
        count += SizeOf.shortSize;
        int value = (int) (0x0000ffff & dataStream.readShort());
        return value;
    }

    public short readUnsignedByte() throws IOException {
        count += SizeOf.byteSize;
        int read = dataStream.read();
        if (read == -1) {
            throw new EOFException(StringUtils.format("You've reached the end of the stream"));
        }
        short value = (short) (0x00ff & read);
        return value;
    }

    public boolean hasMore() {
        return count < limit;
    }

    public long getPosition() {
        return count;
    }

    public String toString() {
        return StringUtils.format("[StreamReaderAbstraction position='{}' limit='{}']", count, limit);
    }

}