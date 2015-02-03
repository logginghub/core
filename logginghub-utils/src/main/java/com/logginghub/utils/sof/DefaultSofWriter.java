package com.logginghub.utils.sof;

import com.logginghub.utils.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

public class DefaultSofWriter implements SofWriter {

    private SofConfiguration configuration;

    private int fieldCount;
    private int lastIndex = Integer.MIN_VALUE;

    private WriterAbstraction writer;
    public final static byte NOT_NULL = 1;
    public final static byte NULL = 0;
    public final static int TYPE_INT = -1;
    public final static int TYPE_LONG = -2;
    public final static int TYPE_UTF8_ARRAY = -3;
    public final static int TYPE_BYTE_ARRAY = -4;
    public final static int TYPE_DOUBLE = -5;
    public final static int TYPE_UTF8 = -6;
    public static final int TYPE_BYTE = -7;
    public static final int TYPE_FLOAT = -9;
    public static final int TYPE_SHORT = -8;
    public static final int TYPE_BOOLEAN = -10;
    public static final int TYPE_CHAR = -11;
    public static final int TYPE_NULL_USER_TYPE = -12;
    public final static int TYPE_INT_OBJECT = -13;
    public final static int TYPE_LONG_OBJECT = -14;
    public final static int TYPE_DOUBLE_OBJECT = -15;
    public static final int TYPE_BYTE_OBJECT = -16;
    public static final int TYPE_SHORT_OBJECT = -17;
    public static final int TYPE_FLOAT_OBJECT = -18;
    public static final int TYPE_BOOLEAN_OBJECT = -19;
    public static final int TYPE_CHARACTER_OBJECT = -20;
    public static final int TYPE_DATE_OBJECT = -21;
    public static final int TYPE_BIGDECIMAL_OBJECT = -22;
    public final static int TYPE_UNIFORM_OBJECT_ARRAY = -23;
    public final static int TYPE_NON_UNIFORM_OBJECT_ARRAY = -24;

    private static final Logger logger = Logger.getLoggerFor(DefaultSofWriter.class);

    // private VariableWidthWriter variableWriterx;

    public static String resolveField(int value) {
        try {
            Field[] fields = DefaultSofWriter.class.getFields();
            for (Field field : fields) {
                Object object = field.get(null);
                if (object instanceof Number) {
                    Number number = (Number) object;
                    if (number.equals(value)) {
                        return field.getName();
                    }
                }
            }
        }
        catch (Exception e) {}

        return "" + value;
    }

    public DefaultSofWriter(WriterAbstraction writer /* , VariableWidthWriter variableWriter */, SofConfiguration configuration) {
        this.writer = writer;
        // this.variableWriter = variableWriter;
        this.configuration = configuration;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void write(int field, BigDecimal b) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : BigDecimal '{}'", field,
        // writer.getPosition(), b);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BIGDECIMAL_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeLong(b.unscaledValue().longValue());
                writer.writeInt(b.scale());
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, boolean b) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : boolean '{}'", field,
        // writer.getPosition(), b);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BOOLEAN);
            writer.writeBoolean(b);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Boolean b) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Boolean '{}'", field,
        // writer.getPosition(), b);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BOOLEAN_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeBoolean(b);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte b) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : byte '{}'", field,
        // writer.getPosition(), b);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE);
            writer.writeByte(b);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Byte b) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Byte '{}'", field,
        // writer.getPosition(), b);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE_OBJECT);
            if (b != null) {
                writer.writeByte(NOT_NULL);
                writer.writeByte(b);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte[] array, int position, int length) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : byte array length '{}'", field,
        // writer.getPosition(), array != null ? array.length: "<null>");
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_BYTE_ARRAY);
            if (array == null) {
                writer.writeInt(-1);
            }
            else {
                writer.writeInt(length);
                writer.write(array, position, length);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, byte[] array) throws SofException {
        if (array != null) {
            write(field, array, 0, array.length);
        }
        else {
            write(field, array, 0, 0);
        }
    }

    public void write(int field, char c) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : char '{}'", field,
        // writer.getPosition(), c);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_CHAR);
            SofSerialiser.writeChar(writer, c);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Character c) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Character '{}'", field,
        // writer.getPosition(), c);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_CHARACTER_OBJECT);
            if (c != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeChar(writer, c);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Date d) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Date '{}'", field,
        // writer.getPosition(), d);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DATE_OBJECT);
            if (d != null) {
                writer.writeByte(NOT_NULL);
                writer.writeLong(d.getTime());
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, double d) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : double '{}'", field,
        // writer.getPosition(), d);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DOUBLE);
            writer.writeDouble(d);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Double d) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Double '{}'", field,
        // writer.getPosition(), d);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_DOUBLE_OBJECT);
            if (d != null) {
                writer.writeByte(NOT_NULL);
                writer.writeDouble(d);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, float f) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : float '{}'", field,
        // writer.getPosition(), f);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_FLOAT);
            writer.writeFloat(f);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Float f) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Float '{}'", field,
        // writer.getPosition(), f);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_FLOAT_OBJECT);
            if (f != null) {
                writer.writeByte(NOT_NULL);
                writer.writeFloat(f);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, int i) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : int '{}'", field, writer.getPosition(),
        // i);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_INT);
            SofSerialiser.writeInt(writer, i);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Integer i) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Integer '{}'", field,
        // writer.getPosition(), i);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_INT_OBJECT);
            if (i != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeInt(writer, i);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, long l) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : long '{}'", field,
        // writer.getPosition(), l);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_LONG);
            SofSerialiser.writeLong(writer, l);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Long l) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Long '{}'", field,
        // writer.getPosition(), l);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_LONG_OBJECT);
            if (l != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeLong(writer, l);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, SerialisableObject serialisableObject) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : serialisable object type '{}'",field,writer.getPosition(),serialisableObject
        // != null ? serialisableObject.getClass().getSimpleName() : "<null>");
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObject == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            // jshaw - hopefully all of the base types (Integer, Long etc) have been covered, so we
            // should just be left with user types.
            Integer userType = configuration.resolve(serialisableObject.getClass());
            if (userType == null) {
                throw new SofException("Sub-object class '{}' has not been registered", serialisableObject.getClass().getName());
            }

            try {
                writeFieldHeaderAlways(field, userType.intValue());

                // Work out the encoded length
                CountingWriterAbstraction countingWriter = new CountingWriterAbstraction();
                DefaultSofWriter countingSofWriter = new DefaultSofWriter(countingWriter, configuration);

                // First pass
                serialisableObject.write(countingSofWriter);

                // TODO : at this stage shouldn't we be going back to the top most entry point and
                // writing out a full object header again? That way we could compress/encrypt object
                // internals?

                // Write the object length
                int length = countingWriter.getLength();
                SofSerialiser.writeInt(writer, length);

                // Second pass
                serialisableObject.write(this);

            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, SerialisableObject[] serialisableObjectArray) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : serialisable non-uniform array of '{}' items",field,serialisableObjectArray
        // != null ? serialisableObjectArray.length : "<null>");
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObjectArray == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            try {
                writeFieldHeaderAlways(field, TYPE_NON_UNIFORM_OBJECT_ARRAY);
                TypeCodex.writeNonUniformObjectArray(writer, serialisableObjectArray, configuration);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, SerialisableObject[] serialisableObjectArray, Class<? extends SerialisableObject> clazz) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : serialisable uniform array of '{}' items (type '{}')",field,writer.getPosition(),serialisableObjectArray
        // != null ? serialisableObjectArray.length : "<null>",clazz.getName());
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        if (serialisableObjectArray == null) {
            try {
                writeFieldHeaderAlways(field, TYPE_NULL_USER_TYPE);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
        else {

            try {
                writeFieldHeaderAlways(field, TYPE_UNIFORM_OBJECT_ARRAY);
                TypeCodex.writeUniformObjectArray(writer, clazz, serialisableObjectArray, configuration);
            }
            catch (IOException e) {
                throw new SofException(e);
            }
        }
    }

    public void write(int field, short s) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : short '{}'", field,
        // writer.getPosition(), s);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_SHORT);
            SofSerialiser.writeInt(writer, s);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, Short s) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : Short '{}'", field,
        // writer.getPosition(), s);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_SHORT_OBJECT);
            if (s != null) {
                writer.writeByte(NOT_NULL);
                SofSerialiser.writeInt(writer, s);
            }
            else {
                writer.writeByte(NULL);
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, String string) throws SofException {
        // //logger.fine("Writing field '{}' | position {} | : string '{}'", field,
        // writer.getPosition(), string);
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_UTF8);
            TypeCodex.writeString(writer, string);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public void write(int field, String[] array) throws SofException {
        // logger.fine("Writing field '{}' | position {} | : string array of '{}' items", field,
        // writer.getPosition(), array != null ? array.length: "<null>");
        if (field <= lastIndex) {
            throw new SofException("Out of order field index - you tried to write index '{}' but the last index was '{}'",
                                   field,
                                   writer.getPosition(),
                                   lastIndex);
        }

        try {
            writeFieldHeader(field, TYPE_UTF8_ARRAY);
            TypeCodex.writeStringArray(writer, array);
        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    private void writeFieldHeader(int field, int type) throws IOException, SofException {
        if (!configuration.isMicroFormat()) {
            writeFieldHeaderAlways(field, type);
        }
    }

    private void writeFieldHeaderAlways(int field, int type) throws IOException, SofException {
        // logger.fine("Writing field header for field '{}' type '{}'", field, writer.getPosition(),
        // type);
        SofSerialiser.writeInt(writer, field);
        SofSerialiser.writeInt(writer, type);
        fieldCount++;
    }

    @Override public String toString() {
        return "Position : " + writer.getPosition();

    }

    @Override public SofConfiguration getConfiguration() {
        return configuration;
    }
}
