package com.logginghub.logging.modules.history;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.CompressionStrategy;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.SerialisationStrategy;
import com.logginghub.logging.modules.Indexifier;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class CompressedBlockEventBuffer implements EventBuffer {

    private static final Logger logger = Logger.getLoggerFor(CompressedBlockEventBuffer.class);
    private Multiplexer<HistoricalIndexElement> elementMultiplexer = new Multiplexer<HistoricalIndexElement>();
    private CompressionStrategy compressionStrategy;
    private SerialisationStrategy serialisationStrategy;
    private int blockSize;
    private long watermark = 0;
    private int count = 0;
    private int currentCount = 0;
    private int blockSequence = 0;

    // jshaw - used so I can get rid of the lock around the event write - readers wanting to iterate
    // through the current buffer will use this value instead, which maybe behind the /real/
    // position until the writer thread updates it once its done encoding the current event.
    private volatile int currentBufferPosition;

    //    private ReadWriteLock currentBufferLock = new ReentrantReadWriteLock();
    //    private ReadWriteLock historicalBuffersLockx = new ReentrantReadWriteLock();
    private ReentrantLock historicalBuffersLock = new ReentrantLock();

    // private ByteBuffer currentBlock;
    private Block currentBlock;
    // private List<Pair<Integer, ByteBuffer>> historicalBlocks = new ArrayList<Pair<Integer,
    // ByteBuffer>>();
    private List<Block> historicalBlocks = new ArrayList<Block>();
    private int highWaterMark;

    public CompressedBlockEventBuffer(CompressionStrategy compressionStrategy,
                                      SerialisationStrategy serialisationStrategy,
                                      int blockSize,
                                      int highWaterMark) {
        this.compressionStrategy = compressionStrategy;
        this.serialisationStrategy = serialisationStrategy;
        this.blockSize = blockSize;
        this.highWaterMark = highWaterMark;
    }

    @Override
    public synchronized void addEvent(DefaultLogEvent t) {

        if (currentBlock == null) {
            currentBlock = createNewBuffer(t.getOriginTime());
        }

        boolean encoded = false;
        while (!encoded) {
            //            currentBufferLock.writeLock().lock();
            currentBlock.buffer.mark();
            try {
                int start = currentBlock.buffer.position();
                serialisationStrategy.serialise(currentBlock.buffer, t);
                encoded = true;
                int length = currentBlock.buffer.position() - start;
                watermark += length;

                currentBlock.index.addEvent(t);
                currentBufferPosition = currentBlock.buffer.position();

                count++;
                currentCount++;
            } catch (BufferOverflowException overflow) {
                currentBlock.buffer.reset();

                int originalWatermarkUsed = currentBlock.buffer.position();
                if (originalWatermarkUsed == 0) {
                    // Hmm this is bad, it means this event couldn't be encoded into an empty block
                    // - ie our block size is too small
                    logger.warn(
                            "Serialised event was too large to fit into an empty block, so we are dropping it. You might want to consider increasing your block size (currently {}) if you have large events.",
                            ByteUtils.format(blockSize));
                    encoded = true;
                } else {
                    currentBlock.buffer.flip();
                    ByteBuffer compressed = compressionStrategy.compress(currentBlock.buffer);

                    Block oldBlock = new Block(compressed, compressed.remaining(), currentBlock.index, currentCount);
                    oldBlock.startTime = currentBlock.startTime;
                    oldBlock.endTime = t.getOriginTime();

                    historicalBuffersLock.lock();
                    try {
                        historicalBlocks.add(oldBlock);
                    } finally {
                        historicalBuffersLock.unlock();
                    }

                    currentCount = 0;
                    blockSequence++;

                    int newWatermarkUsed = compressed.remaining();

                    // Update the watermark to show we've compressed the block down
                    watermark -= originalWatermarkUsed;
                    watermark += newWatermarkUsed;

                    // Decouple the index listener
                    currentBlock.index.removeFinishedIntervalDestination(elementMultiplexer);

                    currentBlock = createNewBuffer(t.getOriginTime());
                }
            } catch (IOException e) {
                throw new FormattedRuntimeException(e, "Failed to encode");
            } finally {
                //                currentBufferLock.writeLock().unlock();
            }
        }

        historicalBuffersLock.lock();
        try {
            if (historicalBlocks.size() > 0) {
                while (watermark > highWaterMark) {
                    Block pair = historicalBlocks.remove(0);
                    logger.fine("Throwing away block containing data between '{}' and '{}'",
                                Logger.toDateString(pair.startTime),
                                Logger.toDateString(pair.endTime));
                    this.watermark -= pair.watermark;
                    this.count -= pair.count;
                }
            }
        } finally {
            historicalBuffersLock.unlock();
        }
    }

    @Override
    public void clear() {
        historicalBuffersLock.lock();
        try {
            historicalBlocks.clear();
            currentBlock = null;
            count = 0;
        } finally {
            historicalBuffersLock.unlock();
        }
    }

    public int sizeof(DefaultLogEvent t) {
        ByteBuffer tempBuffer = ByteBuffer.allocate((int) ByteUtils.megabytes(1));
        try {
            serialisationStrategy.serialise(tempBuffer, t);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempBuffer.position();
    }

    @Override
    public int countEvents() {
        return count;
    }

    @Override
    public int countBetween(long start, long end) {

        historicalBuffersLock.lock();
        int count = 0;
        try {

            for (Block pair : historicalBlocks) {

                ByteBuffer compressed = pair.buffer;

                ByteBuffer decompress = compressionStrategy.decompress(compressed);
                while (decompress.hasRemaining()) {
                    LogEvent deserialise;
                    try {
                        deserialise = (LogEvent) serialisationStrategy.deserialise(decompress);
                        long time = deserialise.getOriginTime();
                        if (time >= start && time < end) {
                            count++;
                        }
                    } catch (IOException e) {
                        throw new FormattedRuntimeException(e);
                    }
                }
            }
        } finally {
            historicalBuffersLock.unlock();
        }

        // TODO : have a look in the current buffer!

        return count;
    }

    @Override
    public long getWatermark() {
        return watermark;
    }

    @Override
    public int size() {
        return 0;

    }

    @Override
    public void extractEventsBetween(final List<LogEvent> matchingEvents, long start, long end) {

        extractEventsBetween(new Destination<LogEvent>() {
            @Override
            public void send(LogEvent t) {
                matchingEvents.add(t);
            }
        }, start, end);

    }

    private void extractEventsBetween(Destination<LogEvent> visitor, ByteBuffer buffer, long start, long end) {
        while (buffer.hasRemaining()) {
            DefaultLogEvent deserialise;
            try {
                deserialise = (DefaultLogEvent) serialisationStrategy.deserialise(buffer);
                long time = deserialise.getOriginTime();
                if (time >= start && time < end) {
                    visitor.send(deserialise);
                }
            } catch (IOException e) {
                throw new FormattedRuntimeException(e);
            }
        }
    }

    @Override
    public void extractIndexBetween(List<HistoricalIndexElement> index, long start, long end) {
        // Process the oldest blocks first
        historicalBuffersLock.lock();
        try {
            for (Block block : historicalBlocks) {
                HistoricalIndexElement[] elements = block.index.toSortedElements();
                for (HistoricalIndexElement historicalIndexElement : elements) {
                    long time = historicalIndexElement.getTime();
                    if (time >= start && time < end) {
                        index.add(historicalIndexElement);
                    }
                }
            }
        } finally {
            historicalBuffersLock.unlock();
        }

        if (currentBlock != null) {
            // Now the current block
            HistoricalIndexElement[] elements = currentBlock.index.toSortedElements();
            for (HistoricalIndexElement historicalIndexElement : elements) {
                long time = historicalIndexElement.getTime();
                if (time >= start && time < end) {
                    index.add(historicalIndexElement);
                }
            }
        }

    }

    @Override
    public void extractEventsBetween(Destination<LogEvent> visitor, long start, long end) {
        // Start with the oldest first
        historicalBuffersLock.lock();
        try {
            for (Block block : historicalBlocks) {

                if (TimeUtils.overlaps(block.startTime, block.endTime, start, end)) {

                    byte[] data = block.buffer.array();
                    ByteBuffer compressed = ByteBuffer.wrap(data);
                    compressed.limit(block.watermark);

                    logger.fine("Visiting block with data from '{}' to '{}' : search criteria from '{}' to '{}' : block is '{}'",
                                Logger.toDateString(block.startTime),
                                Logger.toDateString(block.endTime),
                                Logger.toDateString(start),
                                Logger.toDateString(end),
                                compressed);

                    ByteBuffer decompress = compressionStrategy.decompress(compressed);
                    extractEventsBetween(visitor, decompress, start, end);
                }
            }
        } finally {
            historicalBuffersLock.unlock();
        }

        if (currentBlock != null) {
            ByteBuffer readBuffer;
            //            currentBufferLock.writeLock().lock();
            try {
                // Now have a look in the current buffer
                //                int position = currentBlock.buffer.position();
                readBuffer = ByteBuffer.wrap(currentBlock.buffer.array());
                readBuffer.limit(currentBufferPosition);
            } finally {
                //                currentBufferLock.writeLock().unlock();
            }

            try {
                extractEventsBetween(visitor, readBuffer, start, end);
            } catch (BufferUnderflowException e) {
                logger.warn("Failed to read current buffer, buffer is '{}'", readBuffer);
                throw e;
            }
        }
    }

    @Override
    public int getBlockSequence() {
        return blockSequence;

    }

    @Override
    public void addIndexListener(Destination<HistoricalIndexElement> destination) {
        elementMultiplexer.addDestination(destination);
    }

    private Block createNewBuffer(long time) {
        Indexifier index = new Indexifier(1000);
        index.addFinishedIntervalDestination(elementMultiplexer);
        Block block = new Block(ByteBuffer.allocate(blockSize), 0, index, 0);
        block.startTime = time;
        return block;
    }

    private void extractEventsBetween(List<DefaultLogEvent> matchingEvents, ByteBuffer buffer, long start, long end) {
        while (buffer.hasRemaining()) {
            DefaultLogEvent deserialise;
            try {
                deserialise = (DefaultLogEvent) serialisationStrategy.deserialise(buffer);
                long time = deserialise.getOriginTime();
                if (time >= start && time < end) {
                    matchingEvents.add(deserialise);
                }
            } catch (IOException e) {
                throw new FormattedRuntimeException(e);
            }
        }
    }

    private class Block {
        public int count;
        long startTime;
        long endTime;
        Indexifier index;
        ByteBuffer buffer;
        int watermark;

        public Block(ByteBuffer buffer, int watermark, Indexifier index, int count) {
            this.buffer = buffer;
            this.watermark = watermark;
            this.index = index;
            this.count = count;
        }
    }

}
