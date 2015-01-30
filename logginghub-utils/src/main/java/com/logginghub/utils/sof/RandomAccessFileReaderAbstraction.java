package com.logginghub.utils.sof;

import com.logginghub.utils.SizeOf;
import com.logginghub.utils.StringUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileReaderAbstraction implements ReaderAbstraction {

    private final RandomAccessFile randomAccessFile;
    private final long limit;

    private long count = 0;

    public RandomAccessFileReaderAbstraction(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
        try {
            this.limit = randomAccessFile.length();
        } catch (IOException e) {
            throw new SofRuntimeException(e);
        }
    }

    public void skip(long i) throws IOException {

        long skipTracker = 0;

        while (skipTracker < i) {
            skipTracker += randomAccessFile.skipBytes((int) (i - skipTracker));
        }

        count += i;
    }

    public short readShort() throws IOException {
        count += SizeOf.shortSize;
        return randomAccessFile.readShort();

    }

    public long readLong() throws IOException {
        count += SizeOf.longSize;
        return randomAccessFile.readLong();
    }

    public int readInt() throws IOException {
        count += SizeOf.intSize;
        return randomAccessFile.readInt();
    }

    public float readFloat() throws IOException {
        count += SizeOf.floatSize;
        return randomAccessFile.readFloat();
    }

    public double readDouble() throws IOException {
        count += SizeOf.doubleSize;
        return randomAccessFile.readDouble();
    }

    public char readChar() throws IOException {
        count += SizeOf.charSize;
        return randomAccessFile.readChar();
    }

    public byte readByte() throws IOException {
        count += SizeOf.byteSize;
        int read = randomAccessFile.read();
        if (read == -1) {
            throw new EOFException(StringUtils.format("You've reached the end of the stream"));
        } else {
            return (byte) read;
        }
    }

    public boolean readBoolean() throws IOException {
        count += SizeOf.booleanSize;
        return randomAccessFile.read() == 1 ? true : false;
    }

    public void read(byte[] array, int position, int length) throws IOException {
        // TESTME
        int read = 0;
        while (read < length) {
            read += randomAccessFile.read(array, position + read, length);
        }
        count += read;
    }

    @Override public void setPosition(long position) {
        try {
            randomAccessFile.seek(position);
        } catch (IOException e) {
            throw new SofRuntimeException(e);
        }
    }

    public void read(byte[] contents) throws IOException {
        int read = 0;
        while (read < contents.length) {
            read += randomAccessFile.read(contents);
        }
        count += contents.length;
    }

    public int readUnsignedShort() throws IOException {
        count += SizeOf.shortSize;
        int value = (int) (0x0000ffff & randomAccessFile.readShort());
        return value;
    }

    public short readUnsignedByte() throws IOException {
        count += SizeOf.byteSize;
        int read = randomAccessFile.read();
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
        return StringUtils.format("[RandomAccessFileReaderAbstraction position='{}' limit='{}']", count, limit);
    }

}