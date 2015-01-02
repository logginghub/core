package com.logginghub.utils.data;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferSerialisationReader implements SerialisationReader {

    private ByteBuffer buffer;
    private SerialisedObject object;
    private boolean hasNext;

    public BufferSerialisationReader(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public boolean readNext() {

        hasNext = false;
        object = new SerialisedObject();

        try {
            byte field;

            while ((field = (byte) buffer.get()) != StreamSerialisationWriter.endOfObject) {
                hasNext = true;
                int type = buffer.get();

                Object value;

                switch (type) {
                    case StreamSerialisationWriter.TYPE_NULL: {
                        value = null;
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_INT: {
                        value = buffer.getInt();
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_UTF8: {
                        int length = buffer.getInt();
                        byte[] data = new byte[length];
                        buffer.get(data);
                        value = new String(data, StreamSerialisationWriter.charset);
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_LONG: {
                        value = buffer.getLong();
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_BYTE_ARRAY: {
                        int elements = buffer.getInt();
                        byte[] data = new byte[elements];
                        buffer.get(data);
                        value = data;
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_UTF8_ARRAY: {
                        int elements = buffer.getInt();
                        String[] array = new String[elements];
                        for (int i = 0; i < array.length; i++) {
                            int length = buffer.getInt();
                            byte[] data = new byte[length];
                            buffer.get(data);
                            array[i] = new String(data, StreamSerialisationWriter.charset);
                        }
                        value = array;
                        break;
                    }
                    default: {
                        throw new RuntimeException("Unsupported type " + type + " - is your stream corrupt?");
                    }
                }

                object.addField(field, type, value);
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return hasNext;
    }

    public String readUTF8(Object field) {
        return object.getField(field).asString();
    }

    public int readInt(Object field) {
        return object.getField(field).asInt();
    }

    public long readLong(Object field) {
        return object.getField(field).asLong();
    }

    public String[] readUTF8Array(Object field) {
        return object.getField(field).asStringArray();
    }

    public boolean hasNext() {
        return hasNext;
    }

    public byte[] readByteArray(int field) {
        return object.getField(field).asByteArray();
    }
}
