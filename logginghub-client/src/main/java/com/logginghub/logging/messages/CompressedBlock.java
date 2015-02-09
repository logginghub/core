package com.logginghub.logging.messages;

import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofRuntimeException;
import com.logginghub.utils.sof.SofWriter;
import com.logginghub.utils.sof.ThreadLocalBuffers;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompressedBlock<T extends SerialisableObject> implements SerialisableObject {

    private List<T> objects = new ArrayList<T>();

    private static ThreadLocalBuffers buffers = new ThreadLocalBuffers();

    private byte[] byteArray;

    private byte serialisationStrategy;
    private byte compressionStrategy;

    private CompressionStrategy compression;

    private SerialisationStrategy serialisation;

    private int items;
    private int bufferSizeMB = 1;

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

        ByteBuffer byteBuffer = null;
        boolean done = false;
        while(!done) {
            try {
                byteBuffer = buffers.get((int) ByteUtils.megabytes(bufferSizeMB));

                if (serialisation instanceof SofSerialisationStrategy) {
                    SofSerialisationStrategy sofSerialisationStrategy = (SofSerialisationStrategy) serialisation;
                    sofSerialisationStrategy.setConfiguration(writer.getConfiguration());
                }

                for (T object : objects) {
                    try {
                        serialisation.serialise(byteBuffer, object);
                    } catch (IOException e) {
                        throw new SofException(e);
                    }
                }
                byteBuffer.flip();
                done = true;
            } catch (BufferOverflowException boe) {
                // Make the buffer bigger
                bufferSizeMB *= 2;
            }
        }

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


    public void setBufferSizeMB(int bufferSizeMB) {
        this.bufferSizeMB = bufferSizeMB;
    }

    public int getBufferSizeMB() {
        return bufferSizeMB;
    }


    public void setSofConfiguration(SofConfiguration configuration) {
        if (serialisation instanceof SofSerialisationStrategy) {
            SofSerialisationStrategy sofSerialisationStrategy = (SofSerialisationStrategy) serialisation;
            sofSerialisationStrategy.setConfiguration(configuration);
        }
    }
}
