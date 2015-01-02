package com.logginghub.utils.sof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.logginghub.utils.ReflectionUtils;

/**
 * Alternative to the SofSerialiser which has too many static methods to be performant!
 * 
 * @author James
 * 
 */
public class ByteBufferSofSerialiser {

    private ByteBufferWriterAbstraction writer;
    private SofConfiguration configuration;
    private DefaultSofWriter realWriter;

    public ByteBufferSofSerialiser(ByteBufferWriterAbstraction writer, SofConfiguration configuration) {
        this.configuration = configuration;
        this.writer = writer;
        this.realWriter = new DefaultSofWriter(writer, configuration);
    }

    public void write(SerialisableObject serialisableObject) throws SofException {
        Integer typeID = configuration.resolve(serialisableObject.getClass());
        if (typeID == null) {
            throw new SofException("Object class '{}' has not been registered", serialisableObject.getClass().getName());
        }

        write(typeID, serialisableObject);
    }

    public void write(int typeID, SerialisableObject serialisableObject) throws SofException {

        int flags = 0;
        if (configuration.isCompressed()) {
            flags |= 0x0001;
        }

        int version = 1;

        byte[] preEncoded = null;
        if (configuration.isCompressed()) {
            // Need to encode the payload into a temporary byte array
            try {
                ByteArrayOutputStream temporary = new ByteArrayOutputStream();
                WriterAbstraction tempWriter = SofStreamSerialiser.createWriterAbstraction(temporary);

                DefaultSofWriter realWriter = new DefaultSofWriter(tempWriter, configuration);
                serialisableObject.write(realWriter);

                preEncoded = temporary.toByteArray();
                preEncoded = SofSerialiser.compress(preEncoded);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }

        try {
            writer.writeByte((byte) version);
            writer.writeByte((byte) flags);
            SofSerialiser.writeInt(writer, typeID);

            // TODO : I really dont think we need the field count any more? Maybe it is if we add
            // new field types?
            int fieldCount = 0;
            SofSerialiser.writeInt(writer, fieldCount);

            // Record the length position so we can update it later
            int lengthPosition = writer.getPosition();

            // jshaw - dont use the varint encoding, we need to the length to be consisent in this
            // approach
            writer.writeInt(0);

            // Save the start of the payload position
            int payloadStart = writer.getPosition();

            // Did we pre-encode for comrpession or encryption?
            if (preEncoded != null) {
                // Just write the pre-encoded bytes
                writer.write(preEncoded);
            }
            else {
                // Write out the object
                serialisableObject.write(realWriter);
            }

            int payloadEnd = writer.getPosition();
            int payloadSize = payloadEnd - payloadStart;

            writer.setInt(lengthPosition, payloadSize);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    @SuppressWarnings("unchecked") public static <T extends SerialisableObject> T read(ReaderAbstraction reader, SofConfiguration configuration)
                    throws SofException {

        try {
            int version = reader.readByte();
            int flags = reader.readByte();
            int type = SofSerialiser.readInt(reader);
            int fieldCount = SofSerialiser.readInt(reader);
            
            // This variety has a fixed int size
            int length = reader.readInt();

            // TODO : fail fast if we know we've not got enough bytes?

            Class<? extends SerialisableObject> clazz = configuration.resolve(type);
            if (clazz == null) {
                // Righty ho. We've got an object we have not been setup to handle. Its important we
                // DO THE RIGHT THING and leave the stream/buffer in a sensible place. The end of
                // this message seems like a good bet.
                reader.skip(length);

                throw new SofUnknownTypeException(version, length, flags, type, fieldCount);
            }

            SerialisableObject serialisableObject = ReflectionUtils.instantiate(clazz);

            DefaultSofReader streamReader;

            boolean compressed = (flags & 0x0001) != 0;
            if (compressed) {
                byte[] payload = new byte[length];
                reader.read(payload);
                byte[] uncompressed = SofSerialiser.uncompress(payload);
                ByteArrayInputStream bais = new ByteArrayInputStream(uncompressed);
                ReaderAbstraction tempReader = new StreamReaderAbstraction(bais, uncompressed.length);
                streamReader = new DefaultSofReader(tempReader, configuration);
            }
            else {
                streamReader = new DefaultSofReader(reader, configuration);
            }

            long start = streamReader.getPosition();
            serialisableObject.read(streamReader);
            long end = streamReader.getPosition();

            long decoded = end - start;
            long extra = length - decoded;

            if (extra > 0) {
                // Need to make sure we keep reading until the end of the object - there might have
                // been
                // additional fields that this version of the object just didn't know about
                streamReader.skip(extra);
            }

            return (T) serialisableObject;
        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

}
