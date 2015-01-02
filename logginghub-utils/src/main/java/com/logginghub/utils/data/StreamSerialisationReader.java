package com.logginghub.utils.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.logginghub.utils.FileUtils;

public class StreamSerialisationReader implements SerialisationReader {

    private DataInputStream dataInputStream;
    private SerialisedObject object;
    private boolean hasNext;

    public StreamSerialisationReader(InputStream inputStream) {
        dataInputStream = new DataInputStream(inputStream);
    }

    public boolean readNext() {

        hasNext = false;
        object = new SerialisedObject();

        try {
            byte field;

            while ((field = (byte) dataInputStream.read()) != StreamSerialisationWriter.endOfObject) {
                hasNext = true;
                int type = dataInputStream.read();

                Object value;

                switch (type) {
                    case StreamSerialisationWriter.TYPE_NULL: {
                        value = null;
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_INT: {
                        value = dataInputStream.readInt();
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_UTF8: {
                        int length = dataInputStream.readInt();
                        byte[] data = new byte[length];
                        FileUtils.readFully(data, dataInputStream);
                        value = new String(data, StreamSerialisationWriter.charset);
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_LONG:{
                        value = dataInputStream.readLong();
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_BYTE_ARRAY: {
                        int elements = dataInputStream.readInt();
                        byte[] data = new byte[elements];
                        dataInputStream.read(data);
                        value = data;
                        break;
                    }
                    case StreamSerialisationWriter.TYPE_UTF8_ARRAY:{
                        int elements = dataInputStream.readInt();
                        String[] array = new String[elements];
                        for (int i = 0; i < array.length; i++) {
                            int length = dataInputStream.readInt();
                            byte[] data = new byte[length];
                            FileUtils.readFully(data, dataInputStream);
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
