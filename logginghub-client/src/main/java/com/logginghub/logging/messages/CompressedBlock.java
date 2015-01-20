package com.logginghub.logging.messages;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofRuntimeException;
import com.logginghub.utils.sof.SofWriter;
import com.logginghub.utils.sof.ThreadLocalBuffers;

public class CompressedBlock<T extends SerialisableObject> implements SerialisableObject {

    private List<T> objects = new ArrayList<T>();

    private static ThreadLocalBuffers buffers = new ThreadLocalBuffers();

    private byte[] byteArray;

    private byte serialisationStrategy;
    private byte compressionStrategy;

    private CompressionStrategy compression;

    private SerialisationStrategy serialisation;

    private int items;

//    private Class<? extends SerialisableObject> clazz;

    public CompressedBlock() {
//        Class<? extends SerialisableObject> clazz
//        this.clazz = clazz;
        setSerialisationStrategy(SerialisationStrategyFactory.serialisation_sof_headers_and_no_fields);
        setCompressionStrategy(CompressionStrategyFactory.compression_lz4);
    }

    public void addObject(T object) {
        objects.add(object);
    }

    public T[] decodeAll(Class<T> clazz) {

        final T[] array = (T[]) Array.newInstance(clazz, items);
        decodeObjects(clazz, new Visitor<T>() {
            int index = 0;

            public void visit(T t) {
                array[index++] = t;
            }

        });

        return array;

    }

    public void decodeObjects(Class<T> clazz, Visitor<T> visitor) {

        ByteBuffer compressed = ByteBuffer.wrap(byteArray);
        ByteBuffer decompressed = compression.decompress(compressed);

        // Fudge the sof serialisation configuration to map to the type of class we'll be expecting to pluck out
        if(serialisation instanceof SofSerialisationStrategy) {
            SofSerialisationStrategy sofSerialisationStrategy = (SofSerialisationStrategy) serialisation;
            sofSerialisationStrategy.getConfiguration().registerType(clazz, 0);
        }
        
        while (decompressed.hasRemaining()) {
            try {
                T object = (T) serialisation.deserialise(decompressed);
                visitor.visit(object);
            }
            catch (IOException e) {
                throw new SofRuntimeException(e);
            }
        }

    }

    public void read(SofReader reader) throws SofException {
        setCompressionStrategy(reader.readByte(1));
        setSerialisationStrategy(reader.readByte(2));
        this.items = reader.readInt(3);
        this.byteArray = reader.readByteArray(4);
    }

    public void write(SofWriter writer) throws SofException {

        writer.write(1, (byte) compressionStrategy);
        writer.write(2, serialisationStrategy);
        int size = objects.size();
        writer.write(3, size);

        ByteBuffer byteBuffer = buffers.get((int) ByteUtils.megabytes(1));

        for (T object : objects) {
            try {
                serialisation.serialise(byteBuffer, object);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        byteBuffer.flip();

        // TODO : handle buffer overflow

        ByteBuffer compressed = compression.compress(byteBuffer);
        writer.write(4, compressed.array(), 0, compressed.remaining());

    }

    public void setCompressionStrategy(byte compression) {
        this.compressionStrategy = compression;
        this.compression = CompressionStrategyFactory.createStrategy(compression);
    }

    public void setSerialisationStrategy(byte serialisationStrategy) {
        this.serialisationStrategy = serialisationStrategy;
        this.serialisation = SerialisationStrategyFactory.createStrategy(serialisationStrategy);
    }

    public void addAll(T[] items) {
        Collections.addAll(objects, items);
    }

    public void clear() {
        objects.clear();
    }


}
