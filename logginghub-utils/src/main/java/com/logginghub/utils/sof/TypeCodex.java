package com.logginghub.utils.sof;

import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.SizeOf;
import com.logginghub.utils.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class TypeCodex {

    static private final CharsetEncoder encoder;
    static private final CharsetDecoder decoder;
    static private final int maxBytesPerChar;
    private static final Logger logger = Logger.getLoggerFor(TypeCodex.class);
    private static ThreadLocalBuffers temporaryBuffers = new ThreadLocalBuffers();
    private static ThreadLocalCharArrays temporaryCharArrays = new ThreadLocalCharArrays();

    static {
        Charset charset = Charset.forName("UTF-8");
        encoder = charset.newEncoder();
        maxBytesPerChar = (int) Math.ceil(encoder.maxBytesPerChar());
        decoder = charset.newDecoder();
    }

    public static BigDecimal readBigDecimalObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading BigDecimal from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        BigDecimal value;
        if (nullHint == DefaultSofWriter.NULL) {
            value = null;
        } else {
            long unscaledValue = reader.readLong();
            int scale = reader.readInt();
            value = BigDecimal.valueOf(unscaledValue, scale);
        }

        return value;
    }

    public static boolean readBoolean(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading boolean from {}", reader.getPosition());
        return reader.readBoolean();
    }

    public static Boolean readBooleanObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Boolean from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Boolean booleanValue;
        if (nullHint == DefaultSofWriter.NULL) {
            booleanValue = null;
        } else {
            booleanValue = reader.readBoolean();
        }
        return booleanValue;
    }

    public static byte readByte(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading byte from {}", reader.getPosition());
        return reader.readByte();
    }

    // public static SerialisableObject[] readObjectArray(ReaderAbstraction reader, Class<?> clazz,
    // SofConfiguration sofConfiguration)
    // throws SofException, IOException {
    // int count = reader.readInt();
    // SerialisableObject[] array = (SerialisableObject[]) Array.newInstance(clazz, count);
    //
    // for (int i = 0; i < count; i++) {
    //
    // int nullHint = reader.readByte();
    // if (nullHint == DefaultSofWriter.NULL) {
    // array[i] = null;
    // }
    // else {
    //
    // int objectType = SofSerialiser.readInt(reader);
    // int length = SofSerialiser.readInt(reader);
    //
    // Class<? extends SerialisableObject> subclazz = sofConfiguration.resolve(objectType);
    //
    // SerialisableObject value;
    // if (subclazz == null) {
    // if (sofConfiguration.isAllowUnknownNestedTypes()) {
    // // TODO : maybe we should be logging this or writing it to a
    // // listener at least?
    // reader.skip(length);
    // value = null;
    // }
    // else {
    // throw new SofException("Decode failed, sub-object type '{}' has not been registered",
    // objectType);
    // }
    // }
    // else {
    // value = ReflectionUtils.instantiate(subclazz);
    // DefaultSofReader sofReader = new DefaultSofReader(reader, sofConfiguration);
    // value.read(sofReader);
    // }
    //
    // array[i] = value;
    // }
    // }
    //
    // return array;
    // }

    public static byte[] readByteArray(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading byte[] from {}", reader.getPosition());
        // TODO : varint
        int length = reader.readInt();
        byte[] value;
        if (length == -1) {
            value = null;
        } else {
            value = new byte[length];
            reader.read(value);
        }
        return value;
    }

    public static Byte readByteObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Byte from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Byte byteValue;
        if (nullHint == DefaultSofWriter.NULL) {
            byteValue = null;
        } else {
            byteValue = reader.readByte();
        }
        return byteValue;
    }

    public static char readChar(ReaderAbstraction reader) throws IOException, SofException {
        //logger.fine("Reading char from {}", reader.getPosition());
        return SofSerialiser.readChar(reader);
    }

    public static Character readCharacterObject(ReaderAbstraction reader) throws IOException, SofException {
        //logger.fine("Reading Character from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Character characterValue;
        if (nullHint == DefaultSofWriter.NULL) {
            characterValue = null;
        } else {
            characterValue = SofSerialiser.readChar(reader);
        }
        return characterValue;
    }

    public static Date readDateObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Date from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Date value;
        if (nullHint == DefaultSofWriter.NULL) {
            value = null;
        } else {
            value = new Date(reader.readLong());
        }
        return value;

    }

    public static double readDouble(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading double from {}", reader.getPosition());
        return reader.readDouble();
    }

    public static Double readDoubleObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Double from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Double doubleValue;
        if (nullHint == DefaultSofWriter.NULL) {
            doubleValue = null;
        } else {
            doubleValue = reader.readDouble();
        }
        return doubleValue;

    }

    public static float readFloat(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading float from {}", reader.getPosition());
        return reader.readFloat();
    }

    public static Float readFloatObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Float from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Float value;
        if (nullHint == DefaultSofWriter.NULL) {
            value = null;
        } else {
            value = reader.readFloat();
        }
        return value;
    }

    public static int readInt(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading int from {}", reader.getPosition());
        int value = SofSerialiser.readInt(reader);
        return value;
    }

    public static Integer readIntObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Integer from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Integer integer;
        if (nullHint == DefaultSofWriter.NULL) {
            integer = null;
        } else {
            integer = SofSerialiser.readInt(reader);
        }
        return integer;

    }

    public static long readLong(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading long from {}", reader.getPosition());
        long value = SofSerialiser.readLong(reader);
        return value;
    }

    public static Long readLongObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Long from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Long longValue;
        if (nullHint == DefaultSofWriter.NULL) {
            longValue = null;
        } else {
            longValue = SofSerialiser.readLong(reader);
        }

        return longValue;
    }

    public static SerialisableObject[] readNonUniformObjectArray(ReaderAbstraction reader,
                                                                 SofConfiguration sofConfiguration) throws IOException, SofException {
        //logger.fine("Reading non-uniform array from {}", reader.getPosition());

        int count = SofSerialiser.readInt(reader);
        SerialisableObject[] array = new SerialisableObject[count];

        for (int i = 0; i < count; i++) {

            int nullHint = reader.readByte();
            if (nullHint == DefaultSofWriter.NULL) {
                array[i] = null;
            } else {

                int objectType = SofSerialiser.readInt(reader);
                int length = SofSerialiser.readInt(reader);

                Class<? extends SerialisableObject> subclazz = sofConfiguration.resolve(objectType);

                SerialisableObject value;
                if (subclazz == null) {
                    if (sofConfiguration.isAllowUnknownNestedTypes()) {
                        // TODO : maybe we should be logging this or writing it to a
                        // listener at least?
                        reader.skip(length);
                        value = null;
                    } else {
                        throw new SofException("Decode failed, sub-object type '{}' has not been registered", objectType);
                    }
                } else {
                    value = ReflectionUtils.instantiate(subclazz);
                    DefaultSofReader sofReader = new DefaultSofReader(reader, sofConfiguration);
                    value.read(sofReader);
                }

                array[i] = value;
            }
        }

        return array;

    }

    public static Object readNullType(ReaderAbstraction reader) {
        // This looks dodgy, but its kind of required to keep the symetry of the other methods. We
        // return null as there simply isn't anything to decode - the null value is intrinsic in the
        // type.
        return null;
    }

    public static SerialisableObject readObject(int type,
                                                ReaderAbstraction reader,
                                                SofConfiguration sofConfiguration) throws SofException, IOException {

        //logger.fine("Reading SerialisableObject from {}", reader.getPosition());

        int length = SofSerialiser.readInt(reader);

        Class<? extends SerialisableObject> clazz = sofConfiguration.resolve(type);

        SerialisableObject value;
        if (clazz == null) {
            if (sofConfiguration.isAllowUnknownNestedTypes()) {
                value = null;
                reader.skip(length);
            } else {
                throw new SofException("Decode failed, sub-object type '{}' has not been registered", type);
            }
        } else {
            value = ReflectionUtils.instantiate(clazz);
            DefaultSofReader sofReader = new DefaultSofReader(reader, sofConfiguration);
            value.read(sofReader);
        }

        return value;
    }

    public static short readShort(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading short from {}", reader.getPosition());
        return (short) SofSerialiser.readInt(reader);
    }

    public static Short readShortObject(ReaderAbstraction reader) throws IOException {
        //logger.fine("Reading Short from {}", reader.getPosition());
        byte nullHint = reader.readByte();
        Short shortValue;
        if (nullHint == DefaultSofWriter.NULL) {
            shortValue = null;
        } else {
            shortValue = (short) SofSerialiser.readInt(reader);
        }

        return shortValue;
    }

    public static String[] readStringArray(ReaderAbstraction reader) throws IOException, SofException {
        //logger.fine("Reading String[] from {}", reader.getPosition());
        String[] value;

        int count = SofSerialiser.readInt(reader);
        if (count == -1) {
            value = null;
        } else {
            value = new String[count];
            for (int i = 0; i < count; i++) {
                value[i] = readString(reader);
            }

        }

        return value;
    }

    public static String readString(ReaderAbstraction reader) throws IOException, SofException {
        //logger.fine("Reading String from {}", reader.getPosition());
        int length = SofSerialiser.readInt(reader);
        String value;
        if (length == -1) {
            value = null;
        } else {

            char[] data = new char[length];
            for (int i = 0; i < length; i++) {
                data[i] = SofSerialiser.readChar(reader);
            }

            CharSequence seq = java.nio.CharBuffer.wrap(data);
            value = seq.toString();
        }
        return value;
    }

    public static SerialisableObject[] readUniformObjectArray(ReaderAbstraction reader,
                                                              Class<? extends SerialisableObject> clazz,
                                                              SofConfiguration sofConfiguration) throws SofException, IOException {

        //logger.fine("Reading uniform object array from {}", reader.getPosition());

        int count = SofSerialiser.readInt(reader);
        int type = SofSerialiser.readInt(reader);

        Class<? extends SerialisableObject> encodedClass = sofConfiguration.resolve(type);

        if (encodedClass == null) {
            if (sofConfiguration.isAllowUnknownNestedTypes()) {
                for (int i = 0; i < count; i++) {
                    int nullHint = reader.readByte();
                    if (nullHint == DefaultSofWriter.NULL) {
                    } else {
                        int objectLength = SofSerialiser.readInt(reader);
                        reader.skip(objectLength);
                    }
                }

                // Ok some wacky philosophy needs to happen here. We are in a situation where the
                // configuration doesn't know about this type, but we know the class MUST exist in
                // this JVM or the caller can't be called the uniform array methods as they HAVE to
                // provide the concrete class in the read call. So we can safely instantiate an
                // array of that type even though it has not be registered - which just looks like
                // an oversight in configuration. If this class genuinely didn't exist in the JVM
                // then the calling class would have failed to load long before we do this.

                // There is another edge case - if the object is decoded by someone just walking the
                // object, they wont know the type, and quite frankly they dont care. They will just
                // have to deal with the fact we can't produce an array of the right type in the
                // situation where they class hasn't been registered. In this case there is the very
                // real chance the concrete type doesn't exist in this JVM. So all we can do is
                // return a SerialisalbeObject[] and hope people dont do silly casts on it later.

                // Another thing to think about is should we both returning arrays with anything in
                // them? Seems a bit pointless to return a fully sized array of nulls? Maybe it will
                // give the receiver some clue their decode failed, but its their fault for turning
                // on relaxed mode?

                if (clazz != null) {
                    // Fudge a return value
                    SerialisableObject[] array = (SerialisableObject[]) Array.newInstance(clazz, count);
                    return array;
                } else {
                    SerialisableObject[] array = new SerialisableObject[count];
                    return array;
                }

            } else {
                throw new SofException("Decode failed, uniform array sub-type '{}' has not been registered", type);
            }
        } else {

            // TODO : if we are forcing to people to provide the concrete class, why are we also
            // encoding its type?
            if (clazz != null && !encodedClass.equals(clazz)) {
                throw new SofException("Decode failed, uniform array encoded class '{}' was different from the suggested type passed in '{}'",
                                       encodedClass.getName(),
                                       clazz.getName());
            }

            SerialisableObject[] array = (SerialisableObject[]) Array.newInstance(encodedClass, count);

            for (int i = 0; i < count; i++) {

                // TODO : shouldn't we just be calling a readObject method at this point?
                SerialisableObject object;

                int nullHint = reader.readByte();
                if (nullHint == DefaultSofWriter.NULL) {
                    object = null;
                } else {

                    // This isn't actually used in the decode, but is vital if we need to skip it!
                    int objectLength = SofSerialiser.readInt(reader);

                    object = ReflectionUtils.instantiate(encodedClass);
                    DefaultSofReader sofReader = new DefaultSofReader(reader, sofConfiguration);
                    object.read(sofReader);
                }

                array[i] = object;
            }

            return array;
        }

    }

    public static <T extends SerialisableObject> Collection<T> readUniformObjectCollection(ReaderAbstraction reader,
                                                                                       Class<T> clazz,
                                                                                       SofConfiguration sofConfiguration) throws SofException, IOException {

        //logger.fine("Reading uniform object array from {}", reader.getPosition());

        int count = SofSerialiser.readInt(reader);
        int type = SofSerialiser.readInt(reader);

        Class<? extends SerialisableObject> encodedClass = sofConfiguration.resolve(type);

        if (encodedClass == null) {
            if (sofConfiguration.isAllowUnknownNestedTypes()) {
                for (int i = 0; i < count; i++) {
                    int nullHint = reader.readByte();
                    if (nullHint == DefaultSofWriter.NULL) {
                    } else {
                        int objectLength = SofSerialiser.readInt(reader);
                        reader.skip(objectLength);
                    }
                }

                Collection<SerialisableObject> array = new ArrayList<SerialisableObject>(count);
                return (Collection<T>) array;

            } else {
                throw new SofException("Decode failed, uniform array sub-type '{}' has not been registered", type);
            }
        } else {

            // TODO : if we are forcing to people to provide the concrete class, why are we also
            // encoding its type?
            if (clazz != null && !encodedClass.equals(clazz)) {
                throw new SofException("Decode failed, uniform array encoded class '{}' was different from the suggested type passed in '{}'",
                                       encodedClass.getName(),
                                       clazz.getName());
            }

            Collection<SerialisableObject> array = new ArrayList<SerialisableObject>(count);

            for (int i = 0; i < count; i++) {

                // TODO : shouldn't we just be calling a readObject method at this point?
                SerialisableObject object;

                int nullHint = reader.readByte();
                if (nullHint == DefaultSofWriter.NULL) {
                    object = null;
                } else {

                    // This isn't actually used in the decode, but is vital if we need to skip it!
                    int objectLength = SofSerialiser.readInt(reader);

                    object = ReflectionUtils.instantiate(encodedClass);
                    DefaultSofReader sofReader = new DefaultSofReader(reader, sofConfiguration);
                    object.read(sofReader);
                }

                array.add(object);
            }

            return (Collection<T>) array;
        }

    }

    public static void skipBigDecimalObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint == DefaultSofWriter.NULL) {
        } else {
            reader.skip((int) SizeOf.longSize + (int) SizeOf.intSize);
        }
    }

    public static void skipBoolean(ReaderAbstraction reader) throws IOException {
        reader.skip((int) SizeOf.booleanSize);
    }

    public static void skipBooleanObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            reader.skip((int) SizeOf.booleanSize);
        }
    }

    public static void skipByte(ReaderAbstraction reader) throws IOException {
        reader.skip((int) SizeOf.byteSize);
    }

    public static void skipByteArray(ReaderAbstraction reader) throws IOException {
        // TODO : varint
        int length = reader.readInt();
        if (length == -1) {
        } else {
            reader.skip(length);
        }
    }

    public static void skipByteObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            reader.skip((int) SizeOf.byteSize);
        }
    }

    public static void skipCharacterObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            skipChar(reader);
        }
    }

    public static void skipChar(ReaderAbstraction reader) throws IOException {
        SofSerialiser.skipChar(reader);
    }

    public static void skipDateObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            reader.skip((int) SizeOf.longSize);
        }
    }

    public static void skipDouble(ReaderAbstraction reader) throws IOException {
        reader.skip((int) SizeOf.doubleSize);
    }

    public static void skipDoubleObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            reader.skip((int) SizeOf.doubleSize);
        }
    }

    public static void skipFloat(ReaderAbstraction reader) throws IOException {
        reader.skip((int) SizeOf.floatSize);
    }

    public static void skipFloatObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            reader.skip((int) SizeOf.floatSize);
        }
    }

    public static void skipInt(ReaderAbstraction reader) throws IOException {
        SofSerialiser.skipVarInt(reader);
    }

    public static void skipIntObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            SofSerialiser.skipVarInt(reader);
        }
    }

    public static void skipLong(ReaderAbstraction reader) throws IOException {
        SofSerialiser.skipVarInt(reader);
    }

    public static void skipLongObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            SofSerialiser.skipVarInt(reader);
        }
    }

    public static void skipNonUniformObjectArray(StreamReaderAbstraction reader) throws IOException {
        int count = SofSerialiser.readInt(reader);
        for (int i = 0; i < count; i++) {
            int nullHint = reader.readByte();
            if (nullHint == DefaultSofWriter.NULL) {
            } else {
                int objectType = SofSerialiser.readInt(reader);
                int length = SofSerialiser.readInt(reader);
                reader.skip(length);
            }
        }
    }

    public static void skipNullType(ReaderAbstraction reader) {
        // Nothing to do, there is no encoding for null object type
    }

    public static void skipObject(int fieldType, StreamReaderAbstraction reader) throws IOException {
        int length = SofSerialiser.readInt(reader);
        reader.skip(length);
    }

    public static void skipShort(ReaderAbstraction reader) throws IOException {
        SofSerialiser.skipVarInt(reader);
    }

    public static void skipShortObject(ReaderAbstraction reader) throws IOException {
        byte nullHint = reader.readByte();
        if (nullHint != DefaultSofWriter.NULL) {
            SofSerialiser.skipVarInt(reader);
        }
    }

    public static void skipString(ReaderAbstraction reader) throws IOException {
        int length = SofSerialiser.readInt(reader);
        if (length == -1) {
            // Nothing more to skip
        } else {
            reader.skip(length);
        }
    }

    public static void skipStringArray(ReaderAbstraction reader) throws IOException {
        int count = SofSerialiser.readInt(reader);
        if (count == -1) {
        } else {
            for (int i = 0; i < count; i++) {
                int length = SofSerialiser.readInt(reader);
                if (length == -1) {
                } else {
                    reader.skip(length);
                }
            }
        }
    }

    public static void skipUniformObjectArray(ReaderAbstraction reader) throws IOException {

        int count = SofSerialiser.readInt(reader);
        int type = SofSerialiser.readInt(reader);

        for (int i = 0; i < count; i++) {

            int nullHint = reader.readByte();
            if (nullHint == DefaultSofWriter.NULL) {
            } else {
                int objectLength = SofSerialiser.readInt(reader);
                reader.skip(objectLength);
            }
        }
    }

    public static void writeNonUniformObjectArray(WriterAbstraction writer,
                                                  SerialisableObject[] serialisableObjectArray,
                                                  SofConfiguration configuration) throws IOException, SofException {
        // Write the length of the array
        int length = serialisableObjectArray.length;
        SofSerialiser.writeInt(writer, length);

        if (length > 0) {
            // Write each object
            for (SerialisableObject serialisableObject : serialisableObjectArray) {

                if (serialisableObject == null) {
                    writer.writeByte(DefaultSofWriter.NULL);
                } else {
                    writer.writeByte(DefaultSofWriter.NOT_NULL);

                    // Assertain the uniform array type
                    Integer userType = configuration.resolve(serialisableObject.getClass());
                    if (userType == null) {
                        throw new SofException("Sub-object class '{}' has not been registered", serialisableObject.getClass().getName());
                    }

                    // Write the type ID
                    SofSerialiser.writeInt(writer, userType);

                    // Work out the encoded length
                    CountingWriterAbstraction countingWriter = new CountingWriterAbstraction();
                    DefaultSofWriter countingSofWriter = new DefaultSofWriter(countingWriter, configuration);

                    // First pass
                    serialisableObject.write(countingSofWriter);

                    // TODO : at this stage shouldn't we be going back to the top most entry
                    // point and
                    // writing out a full object header again? That way we could
                    // compress/encrypt object
                    // internals?

                    // Write the object length
                    int objectLength = countingWriter.getLength();
                    SofSerialiser.writeInt(writer, objectLength);

                    // Write the actual object
                    serialisableObject.write(new DefaultSofWriter(writer, configuration));
                }
            }
        }
    }

    public static void writeString(WriterAbstraction writer, String string) throws IOException {
        if (string == null) {
            SofSerialiser.writeInt(writer, -1);
        } else {
            SofSerialiser.writeInt(writer, string.length());
            SofSerialiser.writeUTF(writer, string);
        }
    }

    public static void writeStringArray(WriterAbstraction writer, String[] array) throws IOException {
        if (array != null) {
            int length = array.length;
            SofSerialiser.writeInt(writer, length);

            for (int i = 0; i < length; i++) {
                String string = array[i];
                if (string == null) {
                    SofSerialiser.writeInt(writer, -1);
                } else {
                    byte[] bytes = string.getBytes();
                    SofSerialiser.writeInt(writer, bytes.length);
                    writer.write(bytes);
                }
            }
        } else {
            SofSerialiser.writeInt(writer, -1);
        }
    }

    public static void writeUniformObjectCollection(WriterAbstraction writer,
                                                    Class<? extends SerialisableObject> clazz,
                                                    SerialisableObject[] serialisableObjectArray,
                                                    SofConfiguration configuration) throws SofException, IOException {

        // Write the length of the array
        int length = serialisableObjectArray.length;
        SofSerialiser.writeInt(writer, length);

        // Assertain the uniform array type
        Integer userType = configuration.resolve(clazz);
        if (userType == null) {
            throw new SofException("Sub-object class '{}' has not been registered", clazz.getName());
        }

        // Write the type ID
        SofSerialiser.writeInt(writer, userType);

        if (length > 0) {
            // Write each object
            for (SerialisableObject serialisableObject : serialisableObjectArray) {

                if (serialisableObject == null) {
                    writer.writeByte(DefaultSofWriter.NULL);
                } else {
                    writer.writeByte(DefaultSofWriter.NOT_NULL);

                    // Work out the encoded length
                    CountingWriterAbstraction countingWriter = new CountingWriterAbstraction();
                    DefaultSofWriter countingSofWriter = new DefaultSofWriter(countingWriter, configuration);

                    // First pass
                    serialisableObject.write(countingSofWriter);

                    // TODO : at this stage shouldn't we be going back to the top most entry
                    // point and
                    // writing out a full object header again? That way we could
                    // compress/encrypt object
                    // internals?

                    // Write the object length
                    int objectLength = countingWriter.getLength();
                    SofSerialiser.writeInt(writer, objectLength);

                    // Write the actual object
                    serialisableObject.write(new DefaultSofWriter(writer, configuration));
                }
            }
        }
    }

    public static void writeUniformObjectCollection(WriterAbstraction writer,
                                                    Class<? extends SerialisableObject> clazz,
                                                    Collection<? extends SerialisableObject> serialisableObjectArray,
                                                    SofConfiguration configuration) throws SofException, IOException {

        // Write the length of the array
        int length = serialisableObjectArray.size();
        SofSerialiser.writeInt(writer, length);

        // Assertain the uniform array type
        Integer userType = configuration.resolve(clazz);
        if (userType == null) {
            throw new SofException("Sub-object class '{}' has not been registered", clazz.getName());
        }

        // Write the type ID
        SofSerialiser.writeInt(writer, userType);

        if (length > 0) {
            // Write each object
            for (SerialisableObject serialisableObject : serialisableObjectArray) {

                if (serialisableObject == null) {
                    writer.writeByte(DefaultSofWriter.NULL);
                } else {
                    writer.writeByte(DefaultSofWriter.NOT_NULL);

                    // Work out the encoded length
                    CountingWriterAbstraction countingWriter = new CountingWriterAbstraction();
                    DefaultSofWriter countingSofWriter = new DefaultSofWriter(countingWriter, configuration);

                    // First pass
                    serialisableObject.write(countingSofWriter);

                    // TODO : at this stage shouldn't we be going back to the top most entry
                    // point and
                    // writing out a full object header again? That way we could
                    // compress/encrypt object
                    // internals?

                    // Write the object length
                    int objectLength = countingWriter.getLength();
                    SofSerialiser.writeInt(writer, objectLength);

                    // Write the actual object
                    serialisableObject.write(new DefaultSofWriter(writer, configuration));
                }
            }
        }
    }

}