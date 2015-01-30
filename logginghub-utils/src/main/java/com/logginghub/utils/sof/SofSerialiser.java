package com.logginghub.utils.sof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.logginghub.utils.Destination;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.Logger;

public class SofSerialiser {

    public static final int Version_One_Byte = 1;
    public static final int Version_Two_Bytes = 2;
    public static final int Version_Four_Bytes = 4;

    private static final Logger logger = Logger.getLoggerFor(SofSerialiser.class);

    public static void write(WriterAbstraction writer, SerialisableObject serialisableObject, SofConfiguration configuration) throws SofException {

        Integer typeID = configuration.resolve(serialisableObject.getClass());
        if (typeID == null) {
            throw new SofException("Object class '{}' has not been registered", serialisableObject.getClass().getName());
        }

        CountingWriterAbstraction counter = new CountingWriterAbstraction();
        DefaultSofWriter sofWriter = new DefaultSofWriter(counter, configuration);
        serialisableObject.write(sofWriter);

        int flags = 0;
        if (configuration.isCompressed()) {
            flags |= 0x0001;
        }

        // TODO : deal with uber massive objects?
        int encodedLength = (int) counter.getLength();
        int fieldCount = sofWriter.getFieldCount();

        // jshaw - we know at this point that encoded length contains:
        // * field types
        // * field indexes
        // * encoded fields
        // * sub objects
        // * sub object lengths
        // * sub object counts
        // So there is absolutely no way of telling what the length would be with a different
        // encoding scheme
        // This is pointless.

        // Take into account the size of the field headers for each version approach
        // int lengthWithVersion1 = encodedLength;

        // Add on the extra bytes for the 2 byte encoding
        // int lengthWithVersion2 = encodedLength + (fieldCount * 2 * 1);

        // Add on the extra bytes for the 4 byte encoding
        // int lengthWithVersion4 = encodedLength + (fieldCount * 2 * 3);

        int version = 1;
        // if (lengthWithVersion1 < 255 && fieldCount < 255 && typeID < 255) {
        // version = SofSerialiser.Version_One_Byte;
        // }
        // else if (lengthWithVersion2 < 65535 && fieldCount < 65535 && typeID < 65535) {
        // version = SofSerialiser.Version_Two_Bytes;
        // encodedLength = lengthWithVersion2;
        // }
        // else {
        // version = SofSerialiser.Version_Four_Bytes;
        // encodedLength = lengthWithVersion4;
        // }

        byte[] encoded = null;
        if (configuration.isCompressed()) {
            // Need to encode the payload into a temporary byte array
            try {
                ByteArrayOutputStream temporary = new ByteArrayOutputStream();
                WriterAbstraction tempWriter = SofStreamSerialiser.createWriterAbstraction(temporary);

                DefaultSofWriter realWriter = new DefaultSofWriter(tempWriter, configuration);
                serialisableObject.write(realWriter);

                encoded = temporary.toByteArray();
                encoded = compress(encoded);

                // Update the encoded length with the compressed version
                encodedLength = encoded.length;
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }

        try {
            writer.writeByte((byte) version);
            writer.writeByte((byte) flags);
            SofSerialiser.writeInt(writer, typeID);
            SofSerialiser.writeInt(writer, fieldCount);
            SofSerialiser.writeInt(writer, encodedLength);

            // Did we pre-encode for comrpession or encryption?
            if (encoded != null) {
                // Just write the pre-encoded bytes
                writer.write(encoded);
            }
            else {
                // Write out the object
                DefaultSofWriter realWriter = new DefaultSofWriter(writer, configuration);

                long start = writer.getPosition();
                serialisableObject.write(realWriter);
                long end = writer.getPosition();
                long size = end - start;

                if (encodedLength != size) {
                    throw new SofException("Actual encoded length was {}, but the pre-calcuated payload length was {} - there must have been a problem with the two-pass encoding?",
                                           size,
                                           encodedLength);
                }
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public static byte[] uncompress(byte[] input) {
        // Create the decompressor and give it the data to compress
        Inflater decompressor = new Inflater();

        decompressor.setInput(input);

        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        // Decompress the data
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
            catch (DataFormatException e) {}
        }
        try {
            bos.close();
        }
        catch (IOException e) {}

        byte[] decompressedData = bos.toByteArray();
        return decompressedData;
    }

    public static byte[] compress(byte[] input) throws IOException {
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }

        bos.close();

        byte[] compressedData = bos.toByteArray();
        return compressedData;
    }

    public static SofHeader readHeader(ReaderAbstraction reader) throws IOException, SofException {
        long start = reader.getPosition();
        
        SofHeader header = new SofHeader();
        header.version = reader.readByte();
        if(header.version != 1) {
            throw new SofException("Illegal version number in header : {}", header.version);
        }
        
        header.flags = reader.readByte();
        header.type = SofSerialiser.readInt(reader);
        header.fieldCount = SofSerialiser.readInt(reader);
        header.length = SofSerialiser.readInt(reader);
 
        long end = reader.getPosition();
        header.headerLength = (int) (end - start);
        
        return header;
    }

    @SuppressWarnings("unchecked") public static <T extends SerialisableObject> T read(ReaderAbstraction reader, SofConfiguration configuration)
                    throws SofException, EOFException {

        try {
            SofHeader header = readHeader(reader);

            // TODO : fail fast if we know we've not got enough bytes?

            Class<? extends SerialisableObject> clazz = configuration.resolve(header.type);
            if (clazz == null) {
                // Righty ho. We've got an object we have not been setup to handle. Its important we
                // DO THE RIGHT THING and leave the stream/buffer in a sensible place. The end of
                // this message seems like a good bet.
                reader.skip(header.length);

                throw new SofUnknownTypeException(header.version, header.length, header.flags, header.type, header.fieldCount);
            }

            SerialisableObject serialisableObject = ReflectionUtils.instantiate(clazz);

            DefaultSofReader streamReader;

            boolean compressed = (header.flags & 0x0001) != 0;
            if (compressed) {
                byte[] payload = new byte[header.length];
                reader.read(payload);
                byte[] uncompressed = uncompress(payload);
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

            long extra = header.length - decoded;

            if (extra < 0 && !compressed) {
                throw new SofException("We decoded {} bytes, but the header payload length was {} - this object looks badly encoded",
                                       decoded,
                                       header.length);
            }
            else if (extra > 0) {
                // Need to make sure we keep reading until the end of the object - there might have
                // been
                // additional fields that this version of the object just didn't know about
                streamReader.skip(extra);
            }

            return (T) serialisableObject;
        }
        catch (EOFException eof) {
            throw eof;
        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public static void writeInt(WriterAbstraction out, int n) throws IOException {
        // //logger.fine("Writing varint '{}' at position {}", n, out.getPosition());
        int bytes = 1;

        int b = 0;
        if (n < 0) {
            b = 0x40;
            n = ~n;
        }
        b |= (byte) (n & 0x3F);
        n >>>= 6;
        while (n != 0) {
            b |= 0x80;
            out.writeByte((byte) b);
            b = (n & 0x7F);
            n >>>= 7;
            bytes++;
        }
        out.writeByte((byte) b);
        // //logger.fine("Wrote var int '{}' in {} bytes", n, bytes);

    }

    public static void writeLong(WriterAbstraction out, long value) throws IOException {
        // logger.fine("Writing varint '{}' at position {}", value, out.getPosition());
        int bytes = 1;

        int b = 0;
        if (value < 0) {
            b = 0x40; /* set the negative (2nd left bit) flag */
            value = ~value; /* make the value positive for the rest of this process */
        }
        b |= (byte) (value & 0x3F); /* get the right 6 bits left in the first byte */
        value >>>= 6; /* shift the value 6 bits so value now contains the remainder */
        while (value != 0) {
            b |= 0x80; /* set the continuation bit on the next byte */
            out.writeByte((byte) b); /* write out what we have already */
            b = (int) (value & 0x007F); /*
                                         * load the next portion of the long into the current byte,
                                         * but only the right 7 bits
                                         */
            value >>>= 7; /* shift the remainder over by those 7 bits */
            bytes++;
        }
        out.writeByte((byte) b);
        // logger.fine("Wrote varint '{}' in {} bytes", value, bytes);
    }

    // More from : http://docs.oracle.com/cd/E14526_01/coh.350/e14509/apppifpof.htm
    public static void writeChar(WriterAbstraction out, int ch) throws IOException {
        if (ch >= 0x0001 && ch <= 0x007F) {
            // 1-byte format: 0xxx xxxx
            out.writeByte((byte) ch);
        }
        else if (ch <= 0x07FF) {
            // 2-byte format: 110x xxxx, 10xx xxxx
            out.writeByte((byte) (0xC0 | ((ch >>> 6) & 0x1F)));
            out.writeByte((byte) (0x80 | ((ch) & 0x3F)));
        }
        else {
            // 3-byte format: 1110 xxxx, 10xx xxxx, 10xx xxxx
            out.writeByte((byte) (0xE0 | ((ch >>> 12) & 0x0F)));
            out.writeByte((byte) (0x80 | ((ch >>> 6) & 0x3F)));
            out.writeByte((byte) (0x80 | ((ch) & 0x3F)));
        }
    }

    public static void skipChar(ReaderAbstraction in) throws IOException {

        int b = in.readUnsignedByte();
        switch ((b & 0xF0) >>> 4) {
            case 0x0:
            case 0x1:
            case 0x2:
            case 0x3:
            case 0x4:
            case 0x5:
            case 0x6:
            case 0x7:
                break;

            case 0xC:
            case 0xD: {
                in.readUnsignedByte();
                break;
            }

            case 0xE: {
                in.readUnsignedShort();
                break;
            }

            default:
                throw new IOException("illegal leading UTF byte: " + b);
        }
    }

    public static char readChar(ReaderAbstraction in) throws IOException {
        char ch;

        int b = in.readUnsignedByte();
        switch ((b & 0xF0) >>> 4) {
            case 0x0:
            case 0x1:
            case 0x2:
            case 0x3:
            case 0x4:
            case 0x5:
            case 0x6:
            case 0x7:
                // 1-byte format: 0xxx xxxx
                ch = (char) b;
                break;

            case 0xC:
            case 0xD: {
                // 2-byte format: 110x xxxx, 10xx xxxx
                int b2 = in.readUnsignedByte();
                if ((b2 & 0xC0) != 0x80) {
                    throw new IOException();
                }
                ch = (char) (((b & 0x1F) << 6) | b2 & 0x3F);
                break;
            }

            case 0xE: {
                // 3-byte format: 1110 xxxx, 10xx xxxx, 10xx xxxx
                int n = in.readUnsignedShort();
                int b2 = n >>> 8;
                int b3 = n & 0xFF;
                if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                    throw new IOException();
                }
                ch = (char) (((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | b3 & 0x3F);
                break;
            }

            default:
                throw new IOException("illegal leading UTF byte: " + b);
        }

        return ch;
    }

    public static void skipVarInt(ReaderAbstraction in) throws IOException {
        int b = in.readUnsignedByte();
        while ((b & 0x80) != 0) {
            b = in.readUnsignedByte();
        }
    }

    public static long readLong(ReaderAbstraction in) throws IOException {
        long start = in.getPosition();
        int b = in.readUnsignedByte();
        long result = b & 0x3F; /*
                                 * load the first bit of data, masking out the sign and continuation
                                 * bits
                                 */
        int cBits = 6;
        boolean fNeg = (b & 0x40) != 0; /* check for the sign bit */
        while ((b & 0x80) != 0) { /* while the current byte has the continuation flag set */
            b = in.readUnsignedByte();
            long portion = b & 0x7F;
            portion = portion << cBits;
            result |= portion; /*
                                * set the portion of hte result with the curren byte masked to
                                * remove the continuation bit. First shift is 6 as that includes the
                                * sign. Future shifts increment by 7 bits
                                */
            cBits += 7;
        }
        if (fNeg) {
            result = ~result;
        }

        long length = in.getPosition() - start;
        // logger.fine("Read varint '{}' from {} bytes | position {}", result, length, start);
        return result;
    }

    public static int readInt(ReaderAbstraction in) throws IOException {
        long start = in.getPosition();
        int b = in.readUnsignedByte();
        int n = b & 0x3F;
        int cBits = 6;
        boolean fNeg = (b & 0x40) != 0;
        while ((b & 0x80) != 0) {
            b = in.readUnsignedByte();
            n |= ((b & 0x7F) << cBits);
            cBits += 7;
        }
        if (fNeg) {
            n = ~n;
        }

        long length = in.getPosition() - start;
        // logger.fine("Read varint '{}' from {} bytes | position {}", n, length, start);
        return n;
    }

    public static Object extract(byte[] bytes, int field, SofConfiguration sofConfiguration) throws SofException {
        Object extract = null;
        boolean found = false;

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bais, bytes.length);
        try {
            int version = reader.readByte();
            int flags = reader.readByte();
            int type = SofSerialiser.readInt(reader);
            int fieldCount = SofSerialiser.readInt(reader);
            int length = SofSerialiser.readInt(reader);

            // logger.fine("Decoded header : version='{}' flags='{}' type='{}' fieldCount='{}' length='{}'",
            // version, flags, type, fieldCount, length);

            while (!found && reader.hasMore()) {
                int fieldIndex = SofSerialiser.readInt(reader);
                int fieldType = SofSerialiser.readInt(reader);

                // logger.fine("Decoded field : fieldIndex='{}' fieldType='{}'", fieldIndex,
                // sofConfiguration.resolveField(fieldType));

                if (fieldIndex == field) {
                    extract = decodeField(fieldType, reader, sofConfiguration);
                    found = true;
                }
                else {
                    skipField(fieldType, reader);
                }

                // logger.fine("Reader is now : {}", reader);

            }

        }
        catch (IOException e) {
            throw new SofException(e);
        }

        if (!found) {
            throw new SofException("Field index '{}' wasn't found in the object", field);
        }

        return extract;

    }

    private static void skipField(int fieldType, StreamReaderAbstraction reader) throws IOException, SofException {
        switch (fieldType) {
            case DefaultSofWriter.TYPE_INT:
                TypeCodex.skipInt(reader);
                break;
            case DefaultSofWriter.TYPE_LONG:
                TypeCodex.skipLong(reader);
                break;
            case DefaultSofWriter.TYPE_UTF8_ARRAY:
                TypeCodex.skipStringArray(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE_ARRAY:
                TypeCodex.skipByteArray(reader);
                break;
            case DefaultSofWriter.TYPE_DOUBLE:
                TypeCodex.skipDouble(reader);
                break;
            case DefaultSofWriter.TYPE_UTF8:
                TypeCodex.skipString(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE:
                TypeCodex.skipByte(reader);
                break;
            case DefaultSofWriter.TYPE_SHORT:
                TypeCodex.skipShort(reader);
                break;
            case DefaultSofWriter.TYPE_FLOAT:
                TypeCodex.skipFloat(reader);
                break;
            case DefaultSofWriter.TYPE_BOOLEAN:
                TypeCodex.skipBoolean(reader);
                break;
            case DefaultSofWriter.TYPE_CHAR:
                TypeCodex.skipChar(reader);
                break;
            case DefaultSofWriter.TYPE_NULL_USER_TYPE:
                TypeCodex.skipNullType(reader);
                break;
            case DefaultSofWriter.TYPE_INT_OBJECT:
                TypeCodex.skipIntObject(reader);
                break;
            case DefaultSofWriter.TYPE_LONG_OBJECT:
                TypeCodex.skipLongObject(reader);
                break;
            case DefaultSofWriter.TYPE_DOUBLE_OBJECT:
                TypeCodex.skipDoubleObject(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE_OBJECT:
                TypeCodex.skipByteObject(reader);
                break;
            case DefaultSofWriter.TYPE_SHORT_OBJECT:
                TypeCodex.skipShortObject(reader);
                break;
            case DefaultSofWriter.TYPE_FLOAT_OBJECT:
                TypeCodex.skipFloatObject(reader);
                break;
            case DefaultSofWriter.TYPE_BOOLEAN_OBJECT:
                TypeCodex.skipBooleanObject(reader);
                break;
            case DefaultSofWriter.TYPE_CHARACTER_OBJECT:
                TypeCodex.skipCharacterObject(reader);
                break;
            case DefaultSofWriter.TYPE_DATE_OBJECT:
                TypeCodex.skipDateObject(reader);
                break;
            case DefaultSofWriter.TYPE_BIGDECIMAL_OBJECT:
                TypeCodex.skipBigDecimalObject(reader);
                break;
            case DefaultSofWriter.TYPE_UNIFORM_OBJECT_ARRAY:
                TypeCodex.skipUniformObjectArray(reader);
                break;
            case DefaultSofWriter.TYPE_NON_UNIFORM_OBJECT_ARRAY:
                TypeCodex.skipNonUniformObjectArray(reader);
                break;
            default: {
                // Do the user types skip
                TypeCodex.skipObject(fieldType, reader);
            }
        }
    }

    private static Object decodeField(int fieldType, StreamReaderAbstraction reader, SofConfiguration configuration) throws IOException, SofException {
        Object decoded;

        switch (fieldType) {
            case DefaultSofWriter.TYPE_INT:
                decoded = TypeCodex.readInt(reader);
                break;
            case DefaultSofWriter.TYPE_LONG:
                decoded = TypeCodex.readLong(reader);
                break;
            case DefaultSofWriter.TYPE_UTF8_ARRAY:
                decoded = TypeCodex.readStringArray(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE_ARRAY:
                decoded = TypeCodex.readByteArray(reader);
                break;
            case DefaultSofWriter.TYPE_DOUBLE:
                decoded = TypeCodex.readDouble(reader);
                break;
            case DefaultSofWriter.TYPE_UTF8:
                decoded = TypeCodex.readString(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE:
                decoded = TypeCodex.readByte(reader);
                break;
            case DefaultSofWriter.TYPE_SHORT:
                decoded = TypeCodex.readShort(reader);
                break;
            case DefaultSofWriter.TYPE_FLOAT:
                decoded = TypeCodex.readFloat(reader);
                break;
            case DefaultSofWriter.TYPE_BOOLEAN:
                decoded = TypeCodex.readBoolean(reader);
                break;
            case DefaultSofWriter.TYPE_CHAR:
                decoded = TypeCodex.readChar(reader);
                break;
            case DefaultSofWriter.TYPE_NULL_USER_TYPE:
                decoded = TypeCodex.readNullType(reader);
                break;
            case DefaultSofWriter.TYPE_INT_OBJECT:
                decoded = TypeCodex.readIntObject(reader);
                break;
            case DefaultSofWriter.TYPE_LONG_OBJECT:
                decoded = TypeCodex.readLongObject(reader);
                break;
            case DefaultSofWriter.TYPE_DOUBLE_OBJECT:
                decoded = TypeCodex.readDoubleObject(reader);
                break;
            case DefaultSofWriter.TYPE_BYTE_OBJECT:
                decoded = TypeCodex.readByteObject(reader);
                break;
            case DefaultSofWriter.TYPE_SHORT_OBJECT:
                decoded = TypeCodex.readShortObject(reader);
                break;
            case DefaultSofWriter.TYPE_FLOAT_OBJECT:
                decoded = TypeCodex.readFloatObject(reader);
                break;
            case DefaultSofWriter.TYPE_BOOLEAN_OBJECT:
                decoded = TypeCodex.readBooleanObject(reader);
                break;
            case DefaultSofWriter.TYPE_CHARACTER_OBJECT:
                decoded = TypeCodex.readCharacterObject(reader);
                break;
            case DefaultSofWriter.TYPE_DATE_OBJECT:
                decoded = TypeCodex.readDateObject(reader);
                break;
            case DefaultSofWriter.TYPE_BIGDECIMAL_OBJECT:
                decoded = TypeCodex.readBigDecimalObject(reader);
                break;
            case DefaultSofWriter.TYPE_UNIFORM_OBJECT_ARRAY:
                decoded = TypeCodex.readUniformObjectArray(reader, null, configuration);
                break;
            case DefaultSofWriter.TYPE_NON_UNIFORM_OBJECT_ARRAY:
                decoded = TypeCodex.readNonUniformObjectArray(reader, configuration);
                break;
            default: {
                // Do the user types decode
                decoded = TypeCodex.readObject(fieldType, reader, configuration);
            }
        }

        return decoded;
    }

    public static byte[] toBytes(SerialisableObject object, SofConfiguration configuration) throws SofException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(new StreamWriterAbstraction(baos), object, configuration);
        return baos.toByteArray();
    }

    public static byte[] toBytes(SerialisableObject object) throws SofException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(object.getClass(), 0);
        return toBytes(object, configuration);
    }

    public static void readAll(ReaderAbstraction readerAbstraction, SofConfiguration configuration, Destination<SerialisableObject> destination)
                    throws EOFException, SofException {
        while (readerAbstraction.hasMore()) {
            SerialisableObject object = read(readerAbstraction, configuration);
            destination.send(object);
        }
    }

    public static void readAllWithBackwardsPointers(ReaderAbstraction readerAbstraction, SofConfiguration configuration, Destination<SerialisableObject> destination) throws
                                                                                                                                                                      EOFException,
                                                                                                                                                                      SofException {
        while (readerAbstraction.hasMore()) {
            SerialisableObject object = read(readerAbstraction, configuration);
            destination.send(object);
            try {
                readerAbstraction.readInt();
            }catch(IOException e) {
                throw new SofException(e);
            }
        }
    }

    public static <T extends SerialisableObject> void readAll(InputStream bis, Class<T> clazz, Destination<T> destination) {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(clazz, 0);
        StreamReaderAbstraction streamReaderAbstraction = new StreamReaderAbstraction(bis, Integer.MAX_VALUE);
        try {

            while (true) {
                T t = read(streamReaderAbstraction, configuration);
                destination.send(t);
            }
        }
        catch (EOFException e) {
            // Fine
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    public static <T extends SerialisableObject> void readAll(InputStream bis,
                                                              Class<T> clazz,
                                                              Destination<T> destination,
                                                              SofConfiguration configuration) {
        StreamReaderAbstraction streamReaderAbstraction = new StreamReaderAbstraction(bis, Integer.MAX_VALUE);
        try {

            while (true) {
                T t = read(streamReaderAbstraction, configuration);
                destination.send(t);
            }
        }
        catch (EOFException eof) {
            // Fine
        }
        catch (SofException e) {
            throw new SofRuntimeException(e);
        }
    }

    public static <T extends SerialisableObject> T read(StreamReaderAbstraction readerAbstraction, Class<T> clazz) throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(clazz, 0);
        return read(readerAbstraction, configuration);
    }

    public static <T extends SerialisableObject> T fromBytes(byte[] bytes, Class<T> clazz) throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(clazz, 0);
        return fromBytes(bytes, configuration);
    }

    public static <T extends SerialisableObject> T fromBytes(byte[] bytes, SofConfiguration configuration) throws SofException, EOFException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        T t = read(new ByteBufferReaderAbstraction(buffer), configuration);
        return t;
    }

    public static void writeUTF(WriterAbstraction writer, String string) throws IOException {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char charAt = string.charAt(i);
            writeChar(writer, charAt);
        }
    }

}
