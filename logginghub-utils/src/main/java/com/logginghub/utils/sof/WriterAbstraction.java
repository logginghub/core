package com.logginghub.utils.sof;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface WriterAbstraction {

    void writeShort(short value) throws IOException;

    void writeByte(byte value) throws IOException;

    void writeUnsignedByte(int value) throws IOException;

    void writeInt(int value) throws IOException;

    void writeLong(long value) throws IOException;

    void writeDouble(double value) throws IOException;

    void writeFloat(float value) throws IOException;

    void write(byte[] value, int position, int length) throws IOException;

    void write(byte[] encoded) throws IOException;

    void writeBoolean(boolean value) throws IOException;

    void writeChar(char value) throws IOException;

    void writeUnsignedShort(int i) throws IOException;

    int getPosition();

    void writeBuffer(ByteBuffer tempBuffer) throws IOException;

}
