package com.logginghub.utils.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StreamSerialiser {

    private Map<Class<?>, Serialiser<?>> map = new HashMap<Class<?>, Serialiser<?>>();

    public <T> void registerSerialiser(Class<T> c, Serialiser<T> serialiser) {
        map.put(c, serialiser);
    }

    public <T> void serialise(T object, OutputStream outputStream) {
        @SuppressWarnings("unchecked") Serialiser<T> serialiser = (Serialiser<T>) map.get(object.getClass());
        StreamSerialisationWriter writer = new StreamSerialisationWriter(outputStream);
        serialiser.serialise(object, writer);
        writer.endObject();
    }

    public <T> byte[] serialise(T object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialise(object, baos);
        return baos.toByteArray();
    }

    public byte[] serialiseAll(Object... objects) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Object object : objects) {
            serialise(object, baos);
        } 
        return baos.toByteArray();
    }

    public <T> T deserialise(Class<T> c, InputStream inputStream) {
        @SuppressWarnings("unchecked") Serialiser<T> serialiser = (Serialiser<T>) map.get(c);
        StreamSerialisationReader reader = new StreamSerialisationReader(inputStream);
        reader.readNext();
        T object = serialiser.deserialise(reader);
        return object;
    }

    public <T> T deserialise(Class<T> c, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return deserialise(c, bais);
    }

    public <T> List<T> deserialiseAll(Class<T> c, InputStream inputStream) {
        @SuppressWarnings("unchecked") Serialiser<T> serialiser = (Serialiser<T>) map.get(c);
        StreamSerialisationReader reader = new StreamSerialisationReader(inputStream);

        List<T> list = new ArrayList<T>();
        while (reader.readNext()) {
            T object = serialiser.deserialise(reader);
            list.add(object);
        }
        return list;
    }

    public <T> List<T> deserialiseAll(Class<T> c, byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return deserialiseAll(c, bais);
    }

    public <T> void serialiseAll(Class<T> c, Collection<T> objects, OutputStream os) {
        @SuppressWarnings("unchecked") Serialiser<T> serialiser = (Serialiser<T>) map.get(c);
        StreamSerialisationWriter writer = new StreamSerialisationWriter(os);
        for (T t : objects) {
            serialiser.serialise(t, writer);
            writer.endObject();
        }

        writer.endObject();
    }

    public <T> byte[] serialiseAll(Class<T> c, Collection<T> objects) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialiseAll(c, objects, baos);
        return baos.toByteArray();
    }

    public <T> Iterator<T> getIterator(Class<T> c, InputStream inputStream) {

        @SuppressWarnings("unchecked") final Serialiser<T> serialiser = (Serialiser<T>) map.get(c);
        final StreamSerialisationReader reader = new StreamSerialisationReader(inputStream);
        Iterator<T> iterator = new Iterator<T>() {

            public boolean hasNext() {
                return reader.hasNext();

            }

            public T next() {
                T deserialised = serialiser.deserialise(reader);
                reader.readNext();
                return deserialised;
            }

            public void remove() {}
        };

        // Tee the reader up
        reader.readNext();
        
        return iterator;

    }

}
