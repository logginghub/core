package com.logginghub.logging.utils;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logginghub.utils.Decoder;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Encoder;
import com.logginghub.utils.Factory;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.filter.Filter;

public class OffHeapEventStore<T> {

    private ByteBuffer dataBuffer;
    private Encoder<T> encoder;
    private Decoder<T> decoder;
    private Factory<T> factory;

    static final class DataPointer {
        public int start;
        public int length;

        public DataPointer(int start, int length) {
            super();
            this.start = start;
            this.length = length;
        }

        @Override public String toString() {
            return "DataPointer [start=" + start + ", length=" + length + "]";
        }

    }

    private List<DataPointer> visible = new ArrayList<DataPointer>();
    private List<DataPointer> pointersx = new ArrayList<DataPointer>();
    private Filter<T> filter;

    public OffHeapEventStore(int size, int lookupBufferSize, Encoder<T> encoder, Decoder<T> decoder, Factory<T> factory) {

        this.encoder = encoder;
        this.decoder = decoder;
        this.factory = factory;
        dataBuffer = ByteBuffer.allocateDirect(size);

        // TODO : need to have some sort of system for calculating this!
        // lookupBuffer = ByteBuffer.allocateDirect(lookupBufferSize);

    }

    public void add(T t) {

        boolean ok = false;
        int retries = 0;

        while (!ok && retries < 2) {

            try {
                int position = dataBuffer.position();
                encoder.encode(t, dataBuffer);

                int endPosition = dataBuffer.position();
                Out.out("Wrote event between {} and {}", position, endPosition);

                DataPointer pointer = new DataPointer(position, endPosition - position);
                invalidateOverwrittenPointers(pointer);

                if (filter == null || filter.passes(t)) {
                    visible.add(pointer);
                }

                pointersx.add(pointer);

                ok = true;
            }
            catch (BufferOverflowException e) {
                dataBuffer.position(0);
                retries++;
            }
        }

        if (!ok) {
            throw new RuntimeException("Failed to write event to buffer - was the event bigger than the buffer size?");
        }

    }

    private void invalidateOverwrittenPointers(DataPointer pointer) {
        for (Iterator<DataPointer> iterator = pointersx.iterator(); iterator.hasNext();) {
            DataPointer dataPointer = iterator.next();

            if (TimeUtils.overlapsExclusive(dataPointer.start, dataPointer.start + dataPointer.length, pointer.start, pointer.start + pointer.length)) {
                iterator.remove();
                visible.remove(dataPointer);
            }
        }               
    }

    public void decodeVisible(int index, ByteBuffer dataBuffer, ByteBuffer lookupBuffer, T event) {
        DataPointer dataPointer = visible.get(index);
        ByteBuffer duplicate = dataBuffer.duplicate();
        duplicate.position(dataPointer.start);
        decoder.decode(event, duplicate);
    }
    
    public void decode(int index, ByteBuffer dataBuffer, ByteBuffer lookupBuffer, T event) {
        DataPointer dataPointer = pointersx.get(index);
        ByteBuffer duplicate = dataBuffer.duplicate();
        duplicate.position(dataPointer.start);
        decoder.decode(event, duplicate);
    }

    private int getIndexPosition(int index, ByteBuffer lookupBuffer) {
        DataPointer dataPointer = visible.get(index);
        return dataPointer.start;
    }

    public void decode(ByteBuffer buffer, T event) {
        decoder.decode(event, buffer);
    }

    public void stream(Destination<T> destination) {

        ByteBuffer readDataBuffer = dataBuffer.duplicate();

        int count = visible.size();
        for (int i = 0; i < count; i++) {
            T event = factory.create();
            decodeVisible(i, readDataBuffer, null, event);
            destination.send(event);
        }

    }

    public T get(int index) {
        ByteBuffer readDataBuffer = dataBuffer.duplicate();

        T event = factory.create();
        decodeVisible(index, readDataBuffer, null, event);
        return event;
    }

    public List<T> decodeAll() {
        final List<T> all = new ArrayList<T>();
        stream(new Destination<T>() {
            @Override public void send(T t) {
                all.add(t);
            }
        });
        return all;
    }

    public int getUsed() {
        return dataBuffer.position();

    }

    public int getCount() {
        int count = visible.size();
        return count;

    }

    ByteBuffer getDataBuffer() {
        return dataBuffer;
    }

    // ByteBuffer getLookupBuffer() {
    // return lookupBuffer;
    // }

    // public void dumpLookups() {
    //
    // Out.out("----------------------------------");
    // for (int i = 0; i < 100; i += 4) {
    // Out.out("{} = {}", i, lookupBuffer.getInt(i));
    // }
    // Out.out("----------------------------------");
    // }

    public String toString() {
        StringUtilsBuilder builder = new StringUtils.StringUtilsBuilder();

        builder.appendLine("-------------------------------------");
        builder.appendLine("      OffHeapEventStorage state");
        builder.appendLine("-------------------------------------");
        builder.appendLine("data buffer: {}", dataBuffer);
        builder.appendLine("pointers : {}", pointersx.toString());
        builder.appendLine("visible : {}", visible.toString());

        builder.appendLine("count: {}", getCount());

        for (int i = 0; i < getCount(); i++) {
            T item = factory.create();
            decodeVisible(i, dataBuffer, null, item);
            builder.appendLine("  item {} is at position {} = '{}'", i, getIndexPosition(i, null), item.toString());
        }

        // builder.appendLine("-------------------------------------");
        // builder.appendLine("data buffer: ");
        // HexDump.dump(builder, dataBuffer, 0, dataBuffer.capacity());

        return builder.toString();
    }

    public void dumpState() {
        Out.out(toString());
    }

    public void applyFilter(Filter<T> filter) {
        this.filter = filter;
        refilter();
    }

    public void clearFilter() {
        this.filter = null;
        refilter();
    }

    private void refilter() {

        ByteBuffer readDataBuffer = dataBuffer.duplicate();

        visible.clear();

        int count = pointersx.size();
        for (int i = 0; i < count; i++) {
            T event = factory.create();
            decode(i, readDataBuffer, null, event);

            if (filter == null || filter.passes(event)) {
                // Visible
                visible.add(pointersx.get(i));
            }
            else {
                // Not visible
            }

        }

    }

}
