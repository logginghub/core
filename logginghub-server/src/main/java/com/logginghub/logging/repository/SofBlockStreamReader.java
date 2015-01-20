package com.logginghub.logging.repository;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.Destination;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.DefaultSofReader;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofHeader;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;

public class SofBlockStreamReader {

    private static final Logger logger = Logger.getLoggerFor(SofBlockStreamReader.class);
    private InMemorySofBlock block;
    private SofConfiguration configuration;

    public SofBlockStreamReader(SofConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<SofBlockPointer> loadPointers(File file) throws IOException, SofException {
        long length = file.length();

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        StreamReaderAbstraction reader = new StreamReaderAbstraction(bis, length);

        int count = 0;
        List<SofBlockPointer> pointers = new ArrayList<SofBlockPointer>();
        try {
            while (reader.hasMore()) {
                long objectStartPosition = reader.getPosition();
                SofHeader header = SofSerialiser.readHeader(reader);

                count++;
                long payloadStartPosition = reader.getPosition();

                DefaultSofReader sofReader = new DefaultSofReader(reader, configuration);
                long startTime = sofReader.readLong(InMemorySofBlock.FIELD_START_TIME);
                long endTime = sofReader.readLong(InMemorySofBlock.FIELD_END_TIME);
                
                long compressedLength = sofReader.readLong(InMemorySofBlock.FIELD_COMPRESSED_LENGTH);
                long uncompressedLength = sofReader.readLong(InMemorySofBlock.FIELD_UNCOMPRESSED_LENGTH);

                SofBlockPointer pointer = new SofBlockPointer(startTime, endTime, objectStartPosition, header.length, compressedLength, uncompressedLength);
                pointers.add(pointer);

                long nowPosition = reader.getPosition();
                long amountRead = nowPosition - payloadStartPosition;
                long skipLength = header.length - amountRead;
                reader.skip(skipLength);
            }
        }
        catch (EOFException e) {
            // Fair enough
        }

        return pointers;
    }

    public void visit(InputStream inputStream, Destination<SerialisableObject> destination, SofBlockPointer sofBlockPointer) throws SofException,
                    IOException {
        StreamReaderAbstraction readerAbstraction = new StreamReaderAbstraction(inputStream, Integer.MAX_VALUE);
        readerAbstraction.skip((int) sofBlockPointer.getPosition());

        SofConfiguration temporaryConfiguration = new SofConfiguration();
        temporaryConfiguration.registerType(InMemorySofBlock.class, 0);

        InMemorySofBlock block = SofSerialiser.read(readerAbstraction, temporaryConfiguration);
        block.setSofConfiguration(configuration);

        block.visit(destination);
    }

    public void visit(File file, List<SofBlockPointer> pointers, Destination<SerialisableObject> destination) throws IOException, SofException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        long length = file.length();
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bis, length);

        SofConfiguration temporaryConfiguration = new SofConfiguration();
        temporaryConfiguration.registerType(InMemorySofBlock.class, 0);

        for (SofBlockPointer sofBlockPointer : pointers) {

            logger.finer("Visiting pointer '{}'", sofBlockPointer);
            if (reader.getPosition() < sofBlockPointer.getPosition()) {
                reader.skip(sofBlockPointer.getPosition() - reader.getPosition());
            }

            InMemorySofBlock block = SofSerialiser.read(reader, temporaryConfiguration);
            block.setSofConfiguration(configuration);
            
            Stopwatch sw = Stopwatch.start("Visiting block");
            block.visit(destination);
            logger.finer(sw);
        }

    }

    public void visit(InputStream inputStream, Destination<SerialisableObject> destination) throws SofException {
        StreamReaderAbstraction readerAbstraction = new StreamReaderAbstraction(inputStream, Integer.MAX_VALUE);

        try {
            while (readerAbstraction.hasMore()) {
                InMemorySofBlock block = SofSerialiser.read(readerAbstraction, InMemorySofBlock.class);
                block.setSofConfiguration(configuration);
                block.visit(destination);
            }
        }
        catch (EOFException e) {
            // Fair enough
        }

    }

}
