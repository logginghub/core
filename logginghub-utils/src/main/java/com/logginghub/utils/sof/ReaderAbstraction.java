package com.logginghub.utils.sof;

import java.io.IOException;

public interface ReaderAbstraction {

    byte readByte() throws IOException;

    int readInt() throws IOException;

    short readShort() throws IOException;

    void skip(long extra) throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    void read(byte[] contents) throws IOException;

    boolean readBoolean() throws IOException;

    char readChar() throws IOException;

    double readDouble() throws IOException;

    int readUnsignedShort() throws IOException;

    short readUnsignedByte() throws IOException;

    boolean hasMore();

    long getPosition();

    void read(byte[] array, int position, int length) throws IOException;

    void setPosition(long position);

}
