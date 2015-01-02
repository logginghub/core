package com.logginghub.utils.sof;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.logginghub.utils.data.SerialisedObject;

public class DefaultSofReader implements SofReader {

    private SofConfiguration configuration;
    private boolean hasNext;
    // private VariableWidthReader variableReader;
    private int lengthLimit;
    private SerialisedObject object;
    private ReaderAbstraction reader;

    public DefaultSofReader(ReaderAbstraction reader/* , VariableWidthReader variableReader */, SofConfiguration sofConfiguration) {
        this.reader = reader;
        // this.variableReader = variableReader;
        this.configuration = sofConfiguration;
    }

    public long getPosition() {
        return reader.getPosition();

    }

    public boolean hasMore() {
        return reader.hasMore();
    }

    public BigDecimal readBigDecimal(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BIGDECIMAL_OBJECT) {
                    throw new SofException("Field type mismatch - call was for BigDecimal ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BIGDECIMAL_OBJECT,
                                           type);
                }
            }

            BigDecimal value = TypeCodex.readBigDecimalObject(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public boolean readBoolean(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BOOLEAN) {
                    throw new SofException("Field type mismatch - call was for boolean ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BOOLEAN,
                                           type);
                }
            }
            boolean value = TypeCodex.readBoolean(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Boolean readBooleanObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BOOLEAN_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Boolean ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BOOLEAN_OBJECT,
                                           type);
                }
            }

            Boolean booleanValue = TypeCodex.readBooleanObject(reader);
            return booleanValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public byte readByte(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BYTE) {
                    throw new SofException("Field type mismatch - call was for byte ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BYTE,
                                           type);
                }
            }
            byte value = TypeCodex.readByte(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public byte[] readByteArray(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BYTE_ARRAY) {
                    throw new SofException("Field type mismatch - call was for byte[] ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BYTE_ARRAY,
                                           type);
                }
            }

            byte[] value = TypeCodex.readByteArray(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Byte readByteObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_BYTE_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Byte ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_BYTE_OBJECT,
                                           type);
                }
            }

            Byte byteValue = TypeCodex.readByteObject(reader);
            return byteValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public char readChar(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_CHAR) {
                    throw new SofException("Field type mismatch - call was for char ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_CHAR,
                                           type);
                }
            }
            char value = TypeCodex.readChar(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Character readCharObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_CHARACTER_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Character ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_CHARACTER_OBJECT,
                                           type);
                }
            }

            Character characterValue = TypeCodex.readCharacterObject(reader);
            return characterValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Date readDate(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_DATE_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Date ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_DATE_OBJECT,
                                           type);
                }
            }
            Date value = TypeCodex.readDateObject(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public double readDouble(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_DOUBLE) {
                    throw new SofException("Field type mismatch - call was for double ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_DOUBLE,
                                           type);
                }
            }
            double value = TypeCodex.readDouble(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Double readDoubleObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_DOUBLE_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Double ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_DOUBLE_OBJECT,
                                           type);
                }
            }
            Double doubleValue = TypeCodex.readDoubleObject(reader);
            return doubleValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public float readFloat(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_FLOAT) {
                    throw new SofException("Field type mismatch - call was for float ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_FLOAT,
                                           type);
                }
            }
            float value = TypeCodex.readFloat(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Float readFloatObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_FLOAT_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Float ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_FLOAT_OBJECT,
                                           type);
                }
            }
            Float floatValue = TypeCodex.readFloatObject(reader);
            return floatValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public int readInt(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_INT) {
                    throw new SofException("Field type mismatch - call was for int ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_INT,
                                           type);
                }
            }
            int value = TypeCodex.readInt(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public Integer readIntObject(int field) throws SofException {

        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_INT_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Integer ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_INT_OBJECT,
                                           type);
                }
            }

            Integer integer = TypeCodex.readIntObject(reader);
            return integer;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public long readLong(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_LONG) {
                    throw new SofException("Field type mismatch - call was for long ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_LONG,
                                           type);
                }
            }
            long value = TypeCodex.readLong(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Long readLongObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_LONG_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Long ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_LONG_OBJECT,
                                           type);
                }
            }

            Long longValue = TypeCodex.readLongObject(reader);
            return longValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public SerialisableObject readObject(int field) throws SofException {

        try {
            // if (!configuration.isMicroFormat()) {
            skipToField(field);
            // }

            int type = SofSerialiser.readInt(reader);
            if (type == DefaultSofWriter.TYPE_NULL_USER_TYPE) {
                return null;
            }
            else {
                SerialisableObject value = TypeCodex.readObject(type, reader, configuration);
                return value;
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public SerialisableObject[] readObjectArray(int field) throws SofException {

        try {
            skipToField(field);

            int type = SofSerialiser.readInt(reader);

            if (type == DefaultSofWriter.TYPE_NULL_USER_TYPE) {
                return null;
            }
            else {

                if (type != DefaultSofWriter.TYPE_NON_UNIFORM_OBJECT_ARRAY) {
                    throw new SofException("Field type mismatch - call was for non-uniform SerialisedObject[] ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_NON_UNIFORM_OBJECT_ARRAY,
                                           type);
                }

                SerialisableObject[] array = TypeCodex.readNonUniformObjectArray(reader, configuration);
                return array;
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public SerialisableObject[] readObjectArray(int field, Class<? extends SerialisableObject> clazz) throws SofException {

        try {
            skipToField(field);

            int type = SofSerialiser.readInt(reader);

            if (type == DefaultSofWriter.TYPE_NULL_USER_TYPE) {
                return null;
            }
            else {

                if (type != DefaultSofWriter.TYPE_UNIFORM_OBJECT_ARRAY) {
                    throw new SofException("Field type mismatch - call was for uniform SerialisedObject[] ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_UNIFORM_OBJECT_ARRAY,
                                           type);
                }

                SerialisableObject[] array = TypeCodex.readUniformObjectArray(reader, clazz, configuration);
                return array;
            }
        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public short readShort(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_SHORT) {
                    throw new SofException("Field type mismatch - call was for short ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_SHORT,
                                           type);
                }
            }

            short value = TypeCodex.readShort(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public Short readShortObject(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_SHORT_OBJECT) {
                    throw new SofException("Field type mismatch - call was for Short ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_SHORT_OBJECT,
                                           type);
                }
            }

            Short shortValue = TypeCodex.readShortObject(reader);
            return shortValue;

        }
        catch (IOException e) {
            throw new SofException(e);
        }
    }

    public String readString(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_UTF8) {
                    throw new SofException("Field type mismatch - call was for String ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_UTF8,
                                           type);
                }
            }

            String value = TypeCodex.readString(reader);
            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public String[] readStringArray(int field) throws SofException {
        try {
            if (!configuration.isMicroFormat()) {
                skipToField(field);

                int type = SofSerialiser.readInt(reader);
                if (type != DefaultSofWriter.TYPE_UTF8_ARRAY) {
                    throw new SofException("Field type mismatch - call was for String ('{}'), but encoded type was '{}'",
                                           DefaultSofWriter.TYPE_UTF8_ARRAY,
                                           type);
                }
            }

            String[] value = TypeCodex.readStringArray(reader);

            return value;

        }
        catch (IOException e) {
            throw new SofException(e);
        }

    }

    public void skip(long extra) throws IOException {
        reader.skip(extra);
    }

    private void skipToField(int field) throws IOException, SofException {

        if (!reader.hasMore()) {
            throw new SofPartialDecodeException("No more bytes available from this reader");
        }

        int actualField = SofSerialiser.readInt(reader);
        // TODO : what if its less than? is that broken?
        while (field > actualField) {

            // TODO : this whole thing is broken, should be using the TypeCodex

            switch (actualField) {
                case DefaultSofWriter.TYPE_INT: {
                    reader.skip(4);
                    break;
                }
                case DefaultSofWriter.TYPE_LONG: {
                    reader.skip(8);
                    break;
                }
                case DefaultSofWriter.TYPE_UTF8_ARRAY: {
                    int count = reader.readInt();
                    for (int i = 0; i < count; i++) {
                        int length = reader.readInt();
                        reader.skip(length);
                    }
                    break;
                }
                case DefaultSofWriter.TYPE_BYTE_ARRAY: {
                    int length = reader.readInt();
                    if (length != -1) {
                        reader.skip(length);
                    }
                    break;
                }
                case DefaultSofWriter.TYPE_DOUBLE: {
                    reader.skip(8);
                    break;
                }
                case DefaultSofWriter.TYPE_UTF8: {
                    int length = reader.readInt();
                    if (length != -1) {
                        reader.skip(length);
                    }
                    break;
                }
                case DefaultSofWriter.TYPE_BYTE: {
                    reader.skip(1);
                    break;
                }
                case DefaultSofWriter.TYPE_SHORT: {
                    reader.skip(1);
                    break;
                }
                case DefaultSofWriter.TYPE_FLOAT: {
                    reader.skip(1);
                    break;
                }
                case DefaultSofWriter.TYPE_BOOLEAN: {
                    reader.skip(1);
                    break;
                }
                case DefaultSofWriter.TYPE_CHAR: {
                    reader.skip(1);
                    break;
                }
            }

            actualField = reader.readShort();
        }
    }

}
