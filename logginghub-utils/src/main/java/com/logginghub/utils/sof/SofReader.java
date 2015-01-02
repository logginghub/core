package com.logginghub.utils.sof;

import java.math.BigDecimal;
import java.util.Date;

public interface SofReader {
    int readInt(int field) throws SofException;

    long readLong(int field) throws SofException;

    double readDouble(int field) throws SofException;

    float readFloat(int field) throws SofException;

    short readShort(int field) throws SofException;

    byte readByte(int field) throws SofException;

    String readString(int field) throws SofException;

    byte[] readByteArray(int field) throws SofException;

    Object readObject(int field) throws SofException;

    boolean readBoolean(int field) throws SofException;

    char readChar(int field) throws SofException;

    Date readDate(int field) throws SofException;

    BigDecimal readBigDecimal(int field) throws SofException;

    String[] readStringArray(int field) throws SofException;

    Boolean readBooleanObject(int i) throws SofException;

    Byte readByteObject(int i) throws SofException;

    Short readShortObject(int i) throws SofException;

    Integer readIntObject(int i) throws SofException;

    Long readLongObject(int i) throws SofException;

    Float readFloatObject(int i) throws SofException;

    Character readCharObject(int i) throws SofException;

    Double readDoubleObject(int i) throws SofException;

    SerialisableObject[] readObjectArray(int i, Class<? extends SerialisableObject> clazz) throws SofException;
    
    SerialisableObject[] readObjectArray(int i) throws SofException;

    boolean hasMore();
    
}
