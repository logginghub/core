package com.logginghub.logging.repository;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import com.logginghub.logging.messages.CompressionStrategy;
import com.logginghub.logging.messages.CompressionStrategyFactory;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.ByteBufferReaderAbstraction;
import com.logginghub.utils.sof.ByteBufferWriterAbstraction;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.SofWriter;
import com.logginghub.utils.sof.WriterAbstraction;

public class InMemorySofBlock implements SerialisableObject {

    public final static int FIELD_START_TIME = 0;
    public final static int FIELD_END_TIME = 1;
    public final static int FIELD_COMPRESSED_LENGTH = 2;
    public final static int FIELD_UNCOMPRESSED_LENGTH = 3;
    public final static int FIELD_COMPRESSED_DATA = 4;

    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;
    private long compressLength;
    private volatile long uncompressedLength;

    private static final Logger logger = Logger.getLoggerFor(InMemorySofBlock.class);

    private TimeProvider timeProvider = null;

    private ByteBuffer buffer;
    private WriterAbstraction writer;

    private byte[] compressedData;
    private SofConfiguration sofConfiguration;

    public InMemorySofBlock() {}

    public InMemorySofBlock(SofConfiguration configuration) {
        this(configuration, (int) ByteUtils.megabytes(10));
    }

    public InMemorySofBlock(SofConfiguration configuration, int bufferSize) {
        this.sofConfiguration = configuration;
        buffer = ByteBuffer.allocate(bufferSize);
        writer = new ByteBufferWriterAbstraction(buffer);
    }

    public void setSofConfiguration(SofConfiguration sofConfiguration) {
        this.sofConfiguration = sofConfiguration;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getCompressLength() {
        return compressLength;
    }

    public long getUncompressedLength() {
        return uncompressedLength;
    }

    public synchronized void write(SerialisableObject object) throws SofException {

        buffer.mark();
        try {
            SofSerialiser.write(writer, object, sofConfiguration);
        }
        catch (BufferOverflowException boe) {
            buffer.reset();
            throw boe;
        }

        long time = 0;
        if (timeProvider != null) {
            time = timeProvider.getTime();
        }
        else if (object instanceof TimeProvider) {
            TimeProvider timeProvider = (TimeProvider) object;
            time = timeProvider.getTime();
        }

//        if (time == 0) {
//            Debug.out("Rase");
//        }

        startTime = Math.min(startTime, time);
        endTime = Math.max(endTime, time);

        uncompressedLength = buffer.position();
    }

    public SofConfiguration getSofConfiguration() {
        return sofConfiguration;
    }

    public synchronized ByteBuffer compress() {
        buffer.flip();
        CompressionStrategy compressionStrategy = CompressionStrategyFactory.createStrategy(CompressionStrategyFactory.compression_lz4);
        ByteBuffer compressed = compressionStrategy.compress(buffer);
        compressLength = compressed.remaining();
        return compressed;
    }

    @Override public void read(SofReader reader) throws SofException {
        startTime = reader.readLong(FIELD_START_TIME);
        endTime = reader.readLong(FIELD_END_TIME);
        compressLength = reader.readLong(FIELD_COMPRESSED_LENGTH);
        uncompressedLength = reader.readLong(FIELD_UNCOMPRESSED_LENGTH);
        compressedData = reader.readByteArray(FIELD_COMPRESSED_DATA);

    }

    @Override public synchronized void write(SofWriter writer) throws SofException {

        if (compressedData == null) {
            compressedData = compress().array();
        }

//        Debug.out("Writing block start {} end {}", startTime, endTime);
//        if (startTime == 0) {
//            Debug.out("Arse");
//        }

        writer.write(FIELD_START_TIME, startTime);
        writer.write(FIELD_END_TIME, endTime);
        writer.write(FIELD_COMPRESSED_LENGTH, compressLength);
        writer.write(FIELD_UNCOMPRESSED_LENGTH, uncompressedLength);
        writer.write(FIELD_COMPRESSED_DATA, compressedData, 0, (int) compressLength);
    }

    public int remaining() {
        return buffer.remaining();
    }

    public int position() {
        return buffer.position();
    }

    public void visit(Destination<SerialisableObject> destination) throws EOFException, SofException {
        CompressionStrategy compressionStrategy = CompressionStrategyFactory.createStrategy(CompressionStrategyFactory.compression_lz4);
        Stopwatch sw = Stopwatch.start("Block compression");
        buffer = compressionStrategy.decompress(ByteBuffer.wrap(compressedData));
        logger.fine(sw);

        sw = Stopwatch.start("Block decode {}", buffer);
        SofSerialiser.readAll(new ByteBufferReaderAbstraction(buffer), sofConfiguration, destination);
        logger.fine(sw);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public synchronized int writeTo(OutputStream outputStream) throws SofException, IOException {
        // TODO : should be able to write directly to the stream
        byte[] bytes = SofSerialiser.toBytes(this);
        outputStream.write(bytes);
        return bytes.length;
    }

    public synchronized void reset() {
        buffer.clear();
        startTime = Long.MAX_VALUE;
        endTime = Long.MIN_VALUE;
        compressLength = 0;
        uncompressedLength = 0;
        compressedData = null;
    }

    public void visitLatest(long start, long end, Destination<SerialisableObject> destination) throws EOFException, SofException {

        logger.fine("Visiting the in memory block with compressed length {} and uncompressed length {}", compressLength, uncompressedLength);

        // jshaw - this isn't thread safe, but I dont want to hurt write performance by locking, so
        // we use the uncompressed length (which is updated after writing) as the read limit
        if (uncompressedLength > 0) {
            ByteBuffer readBuffer = ByteBuffer.wrap(buffer.array());
            readBuffer.limit((int) uncompressedLength);

            logger.fine("Deserialising {} MB of data", ByteUtils.formatMB((double) uncompressedLength));

            TimeFilterDestination filter = new TimeFilterDestination(destination, start, end);
            SofSerialiser.readAll(new ByteBufferReaderAbstraction(readBuffer), sofConfiguration, filter);
        }
    }

    @Override public String toString() {
        return "InMemorySofBlock " + buffer != null ? buffer.toString() : "<empty>";

    }
}
