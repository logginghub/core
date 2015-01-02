package com.logginghub.utils.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StreamSerialisationWriter implements SerialisationWriter {

    public final static byte TYPE_UTF8 = 0;
    public final static byte TYPE_INT = 1;
    public final static byte TYPE_LONG = 2;
    public final static byte TYPE_UTF8_ARRAY = 3;
    public final static byte TYPE_NULL = 4;
    public final static byte TYPE_BYTE_ARRAY = 5;

    public final static String charset = Charset.forName("UTF-8").name();
    public final static byte endOfObject = (byte) 0xff;

    private DataOutputStream dataOutputStream;

    public StreamSerialisationWriter(OutputStream outputStream) {
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public void startObject() {}

    public void endObject() {
        try {
            dataOutputStream.write(endOfObject);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Object field, String value) {
        try {
            if (value == null) {
                writeHeader(field, TYPE_NULL);
            }
            else {
                writeHeader(field, TYPE_UTF8);
                dataOutputStream.writeInt(value.getBytes(charset).length);
                dataOutputStream.write(value.getBytes());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Object field, int value) {
        try {
            writeHeader(field, TYPE_INT);
            dataOutputStream.writeInt(value);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Object field, long value) {
        try {
            writeHeader(field, TYPE_LONG);
            dataOutputStream.writeLong(value);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Object field, String[] values) {
        try {
            if (values == null) {
                writeHeader(field, TYPE_NULL);
            }
            else {
                writeHeader(field, TYPE_UTF8_ARRAY);
                dataOutputStream.writeInt(values.length);
                for (String string : values) {
                    byte[] bytes = string.getBytes(charset);
                    dataOutputStream.writeInt(bytes.length);
                    dataOutputStream.write(bytes);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHeader(Object field, int type) throws IOException {
        Integer fieldValue = (Integer) field;
        byte byteValue = fieldValue.byteValue();
        dataOutputStream.write(byteValue);
        dataOutputStream.write(type);
    }

    public void write(Object field, byte[] value) {
        try {
            if (value == null) {
                writeHeader(field, TYPE_NULL);
            }
            else {
                writeHeader(field, TYPE_BYTE_ARRAY);
                dataOutputStream.writeInt(value.length);
                dataOutputStream.write(value);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }        
    }

}
