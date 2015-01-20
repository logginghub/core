package com.logginghub.logging.modules.history;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.SerialisationStrategy;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Pair;

public class SerialisedBuffer implements EventBuffer {

    private SerialisationStrategy serialisationStrategy;
    private int blockSize;
    private int highWaterMark;

    public SerialisedBuffer(SerialisationStrategy serialisationStrategy, int blockSize, final int highWaterMark) {
        this.serialisationStrategy = serialisationStrategy;
        this.blockSize = blockSize;
        this.highWaterMark = highWaterMark;
    }

    @Override public String toString() {
        return "[" + serialisationStrategy.toString() + ", blockSize=" + blockSize + "]";
    }

    private long watermark = 0;
    private int count = 0;
    private int currentCount = 0;

    private ByteBuffer currentBlock;
    private List<Pair<Integer, ByteBuffer>> historicalBlocks = new ArrayList<Pair<Integer, ByteBuffer>>();

    @Override public int countEvents() {
        return count;
    }

    @Override public int countBetween(long start, long end) {

        int count = 0;
        for (Pair<Integer, ByteBuffer> pair : historicalBlocks) {

            ByteBuffer data = pair.getB();
            while (data.hasRemaining()) {
                LogEvent deserialise;
                try {
                    deserialise = (LogEvent) serialisationStrategy.deserialise(data);
                    long time = deserialise.getOriginTime();
                    if (time >= start && time < end) {
                        count++;
                    }
                }
                catch (IOException e) {
                    throw new FormattedRuntimeException(e);
                }
            }
        }

        // TODO : have a look in the current buffer!

        return count;
    }

    @Override public void addEvent(DefaultLogEvent t) {

        if (currentBlock == null) {
            currentBlock = createNewBuffer();
        }

        boolean encoded = false;
        while (!encoded) {
            currentBlock.mark();
            try {
                int start = currentBlock.position();
                serialisationStrategy.serialise(currentBlock, t);
                encoded = true;
                int length = currentBlock.position() - start;
                watermark += length;
                count++;
                currentCount++;
            }
            catch (BufferOverflowException overflow) {
                currentBlock.reset();
                currentBlock.flip();
                historicalBlocks.add(new Pair<Integer, ByteBuffer>(currentCount, currentBlock));
                currentCount = 0;
                currentBlock = createNewBuffer();
            }
            catch (IOException e) {
                throw new FormattedRuntimeException(e, "Failed to encode");
            }
        }

        if (historicalBlocks.size() > 0) {
            while (watermark > highWaterMark) {
                Pair<Integer, ByteBuffer> pair = historicalBlocks.remove(0);
                int count = pair.getA();
                ByteBuffer oldestBuffer = pair.getB();
                watermark -= oldestBuffer.remaining();
                this.count -= count;
            }
        }
    }

    private ByteBuffer createNewBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(blockSize);
        return buffer;
    }

    @Override public long getWatermark() {
        return watermark;
    }

    @Override public int size() {
        return 0;
         
    }

    @Override public void extractIndexBetween(List<HistoricalIndexElement> index, long start, long end) {}

    @Override public void extractEventsBetween(Destination<LogEvent> visitor, long start, long end) {}

    @Override public void extractEventsBetween(List<LogEvent> matchingEvents, long start, long end) {}

    @Override public int getBlockSequence() {
        return 0;
         
    }

    @Override public void addIndexListener(Destination<HistoricalIndexElement> destination) {}
}
