package com.logginghub.utils.sof;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StreamWriterAbstraction implements WriterAbstraction, Closeable {
    private DataOutputStream dos;

    public StreamWriterAbstraction(OutputStream stream) {
        dos = new DataOutputStream(stream);
    }

    public void writeShort(short value) throws IOException {
        dos.writeShort(value);
    }

    public void writeLong(long l) throws IOException {        
        dos.writeLong(l);
    }

    public void writeFloat(float f) throws IOException {
        dos.writeFloat(f);
    }

    public void writeDouble(double d) throws IOException {
        dos.writeDouble(d);
    }

    public void writeChar(char c) throws IOException {
        dos.writeChar(c);
    }

    public void writeByte(byte type) throws IOException {
        dos.write(type);
    }

    public void writeBoolean(boolean b) throws IOException {
        dos.writeBoolean(b);
    }

    public void write(byte[] bytes) throws IOException {
        dos.write(bytes);
    }

    public void writeInt(int value) throws IOException {
        dos.writeInt(value);
    }

    public void writeUnsignedByte(int value) throws IOException {
        dos.write((byte) (0x000000ff & value));
    }

    public void writeUnsignedShort(int value) throws IOException {
        dos.writeShort((short) (0x0000ffff & value));
    }

    @Override public String toString() {
        return "StreamWriterAbstraction position=" + dos.size();
    }

    public int getPosition() {
        return dos.size();
    }

    public void writeBuffer(ByteBuffer tempBuffer) throws IOException {
        dos.write(tempBuffer.array(), tempBuffer.position(), tempBuffer.remaining());
    }

    public void write(byte[] value, int position, int length) throws IOException {
        dos.write(value, position, length);
    }

    public void close() throws IOException {
        dos.close();
    }

    public void flush() throws IOException {
        dos.flush();
    }
}
