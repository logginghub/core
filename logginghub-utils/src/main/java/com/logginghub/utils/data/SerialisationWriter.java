package com.logginghub.utils.data;

public interface SerialisationWriter {
    void write(Object field, String value);
    void write(Object field, int value);
    void write(Object field, long value);
    void write(Object field, String[] values);
    void write(Object field, byte[] t);

}
