package com.logginghub.utils.data;

public interface SerialisationReader {
    String readUTF8(Object field);
    int readInt(Object field);
    long readLong(Object field);
    String[] readUTF8Array(Object field);
    byte[] readByteArray(int i);
}
