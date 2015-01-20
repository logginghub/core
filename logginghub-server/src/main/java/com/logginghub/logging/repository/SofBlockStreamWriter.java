package com.logginghub.logging.repository;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;

import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class SofBlockStreamWriter {

    private OutputStream outputStream;
    private InMemorySofBlock block;
    private long bytesWritten = 0;

    private static final Logger logger = Logger.getLoggerFor(SofBlockStreamWriter.class);

    public SofBlockStreamWriter(OutputStream outputStream, SofConfiguration configuration, int blockSize) {
        this.outputStream = outputStream;
        block = new InMemorySofBlock(configuration, blockSize);
    }

    public synchronized void write(SerialisableObject object) throws SofException, IOException {

        boolean written = false;
        int retry = 0;

        while (!written && retry < 2) {
            try {
                block.write(object);
                written = true;
            }
            catch (BufferOverflowException boe) {
                String blockInfo = block.toString();
                writeCurrentBlock();
                logger.fine("Writing current block after overflow '{}' - now written {} MB", blockInfo, ByteUtils.formatMB((double) bytesWritten));
                retry++;
            }
        }

        if (!written) {
            throw new SofException("It looks like we couldn't fit this object into a block, you might need to increase the block size");
        }
    }

    public synchronized void writeCurrentBlock() throws SofException, IOException {
        bytesWritten += block.writeTo(outputStream);

        long compressLength = block.getCompressLength();
        long uncompressedLength = block.getUncompressedLength();
        logger.fine("Compressed length was {} vs uncompressed length {} - compression % {}",
                    compressLength,
                    uncompressedLength,
                    100d * compressLength / (double) uncompressedLength);

        block.reset();
    }

    public synchronized void flush() throws SofException, IOException {
        writeCurrentBlock();
        outputStream.flush();
    }

    public synchronized void periodicFlush() throws SofException, IOException {
        if (block.position() > 0) {
            logger.fine("Block position is '{}' so the periodic flush is writing to disk", block.position());
            flush();
        }
        else {
            logger.fine("Block position is '{}' so the periodic flush is skipping writing to disk", block.position());
        }
    }

    public synchronized void close() throws SofException, IOException {
        if (block.position() > 0) {
            writeCurrentBlock();
        }

    }

    public long getBytesWritten() {
        return bytesWritten + block.position();
    }

    public void visitLatest(long start, long end, Destination<SerialisableObject> destination) throws EOFException, SofException {        
        block.visitLatest(start, end, destination);
    }

}
