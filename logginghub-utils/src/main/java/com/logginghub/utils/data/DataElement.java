package com.logginghub.utils.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class DataElement implements SerialisableObject {
    public Object object;
    public Type type;

    public enum Type {
        Null,
        String,
        Int,
        Short,
        Double,
        Float,
        Long,
        Boolean,
        DataStructure
    }

    /**
     * Serialisation constructor
     */
    public DataElement() {}

    public DataElement(Object object, Type type) {
        super();
        this.object = object;
        this.type = type;
        if (object == null) {
            type = Type.Null;
        }
    }

    public String toString() {
        String formatted;
        if (object == null) {
            formatted = "<null>";
        }
        else {
            formatted = object.toString();
        }
        return formatted;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeByte((byte) type.ordinal());
        switch (type) {
            case Null: {
                // Do nothing
                break;
            }
            case Boolean: {
                dos.writeByte(((Boolean) object) ? 1 : 0);
                break;
            }
            case Double: {
                dos.writeDouble((Double) object);
                break;
            }
            case Float: {
                dos.writeFloat((Float) object);
                break;
            }
            case Int: {
                dos.writeInt((Integer) object);
                break;
            }
            case Long: {
                dos.writeLong((Long) object);
                break;
            }
            case Short: {
                dos.writeShort((Short) object);
                break;
            }
            case String: {
                dos.writeUTF((String) object);
                break;
            }
            case DataStructure: {
                DataStructure ds = (DataStructure) object;
                byte[] byteArray = ds.toByteArray();
                dos.writeInt(byteArray.length);
                dos.write(byteArray);
                break;
            }
        }
    }

    public static DataElement read(DataInputStream dis) throws IOException {

        byte typeOrdinal = (byte) dis.read();
        Type type = Type.values()[typeOrdinal];
        Object value;

        switch (type) {
            case Null: {
                value = null;
                break;
            }
            case Boolean: {
                value = dis.read() == 1;
                break;
            }
            case Double: {
                value = dis.readDouble();
                break;
            }
            case Float: {
                value = dis.readFloat();
                break;
            }
            case Int: {
                value = dis.readInt();
                break;
            }
            case Long: {
                value = dis.readLong();
                break;
            }
            case Short: {
                value = dis.readShort();
                break;
            }
            case String: {
                value = dis.readUTF();
                break;
            }
            case DataStructure: {

                int length = dis.readInt();
                byte[] data = new byte[length];
                dis.read(data);
                DataStructure ds = DataStructure.fromByteArray(data);
                value = ds;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown type : " + type);
            }

        }

        DataElement dataElement = new DataElement(value, type);
        return dataElement;

    }

    public Double asDouble() {
        if (object instanceof Double) {
            return (Double) object;
        }
        else {
            if (object instanceof Number) {
                Number number = (Number) object;
                return number.doubleValue();
            }
            else if (object instanceof String) {
                return Double.parseDouble((String) object);
            }
            else {
                throw new IllegalAccessError(StringUtils.format("Cannot coerce object '{}' type '{}' into a Double", object, object.getClass()));
            }
        }
    }

    public String asString() {
        if (object instanceof String) {
            return (String) object;
        }
        else if (object == null) {
            return null;
        }
        else {
            return object.toString();
        }

    }

    public Long asLong() {
        if (object instanceof Long) {
            return (Long) object;
        }
        else {
            if (object instanceof Number) {
                Number number = (Number) object;
                return number.longValue();
            }
            else if (object instanceof String) {
                return Long.parseLong((String) object);
            }
            else {
                throw new IllegalAccessError(StringUtils.format("Cannot coerce object '{}' type '{}' into a Long", object, object.getClass()));
            }
        }
    }

    public Integer asInteger() {
        if (object instanceof Integer) {
            return (Integer) object;
        }
        else {
            if (object instanceof Number) {
                Number number = (Number) object;
                return number.intValue();
            }
            else if (object instanceof String) {
                return Integer.parseInt((String) object);
            }
            else {
                throw new IllegalAccessError(StringUtils.format("Cannot coerce object '{}' type '{}' into a Integer", object, object.getClass()));
            }
        }
    }

    public void read(SofReader reader) throws SofException {
        this.type = DataElement.Type.valueOf(reader.readString(0));

        switch (type) {
            case Boolean:
                this.object = reader.readBooleanObject(1);
                break;
            case DataStructure:
                this.object = reader.readObject(1);
                break;
            case Double:
                this.object = reader.readDoubleObject(1);
                break;
            case Float:
                this.object = reader.readFloatObject(1);
                break;
            case Int:
                this.object = reader.readIntObject(1);
                break;
            case Long:
                this.object = reader.readLongObject(1);
                break;
            case Null:
                this.object = null;
                break;
            case Short:
                this.object = reader.readShortObject(1);
                break;
            case String:
                this.object = reader.readString(1);
                break;
            default: {
                throw new IllegalArgumentException("Unhandled type '" + type + "'");
            }
        }
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, type.name());

        switch (type) {
            case Boolean:
                writer.write(1, (Boolean) object);
                break;
            case DataStructure:
                writer.write(1, (DataStructure) object);
                break;
            case Double:
                writer.write(1, (Double) object);
                break;
            case Float:
                writer.write(1, (Float) object);
                break;
            case Int:
                writer.write(1, (Integer) object);
                break;
            case Long:
                writer.write(1, (Long) object);
                break;
            case Null:
                break;
            case Short:
                writer.write(1, (Short) object);
                break;
            case String:
                writer.write(1, (String) object);
                break;
            default: {
                throw new IllegalArgumentException("Unhandled type '" + type + "'");
            }
        }
    }
}
