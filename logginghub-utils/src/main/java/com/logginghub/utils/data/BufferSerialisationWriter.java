package com.logginghub.utils.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class BufferSerialisationWriter implements SerialisationWriter {

    public final static byte TYPE_UTF8 = 0;
    public final static byte TYPE_INT = 1;
    public final static byte TYPE_LONG = 2;
    public final static byte TYPE_UTF8_ARRAY = 3;
    public final static byte TYPE_NULL = 4;
    public final static byte TYPE_BYTE_ARRAY = 5;

    public final static String charset = Charset.forName("UTF-8").name();
    public final static byte endOfObject = (byte) 0xff;

    private ByteBuffer buffer;

    public BufferSerialisationWriter(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void startObject() {}

    public void endObject() {
        buffer.put(endOfObject);
    }

    public void write(Object field, String value) {
        if (value == null) {
            writeHeader(field, TYPE_NULL);
        }
        else {
            writeHeader(field, TYPE_UTF8);
            try {
                buffer.putInt(value.getBytes(charset).length);
                buffer.put(value.getBytes());
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(Object field, int value) {
        writeHeader(field, TYPE_INT);
        buffer.putInt(value);
    }

    public void write(Object field, long value) {
        writeHeader(field, TYPE_LONG);
        buffer.putLong(value);
    }

    public void write(Object field, String[] values) {
        try {
            if (values == null) {
                writeHeader(field, TYPE_NULL);
            }
            else {
                writeHeader(field, TYPE_UTF8_ARRAY);
                buffer.putInt(values.length);
                for (String string : values) {
                    byte[] bytes = string.getBytes(charset);
                    buffer.putInt(bytes.length);
                    buffer.put(bytes);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeader(Object field, int type) {
        Integer fieldValue = (Integer) field;
        byte byteValue = fieldValue.byteValue();
        buffer.put(byteValue);
        buffer.put((byte) type);
    }

    public void write(Object field, byte[] value) {
        if (value == null) {
            writeHeader(field, TYPE_NULL);
        }
        else {
            writeHeader(field, TYPE_BYTE_ARRAY);
            buffer.putInt(value.length);
            buffer.put(value);
        }
    }

}
