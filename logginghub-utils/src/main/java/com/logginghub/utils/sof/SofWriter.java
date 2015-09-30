package com.logginghub.utils.sof;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

public interface SofWriter {
    // TODO : arrays of all base types, fun fun fun
    void write(int field, int i) throws SofException;

    void write(int field, long l) throws SofException;

    void write(int field, double d) throws SofException;

    void write(int field, float f) throws SofException;

    void write(int field, short s) throws SofException;

    void write(int field, byte b) throws SofException;

    void write(int field, boolean b) throws SofException;

    void write(int field, char c) throws SofException;

    void write(int field, Integer i) throws SofException;

    void write(int field, Long l) throws SofException;

    void write(int field, Double d) throws SofException;

    void write(int field, Float f) throws SofException;

    void write(int field, Short s) throws SofException;

    void write(int field, Byte b) throws SofException;

    void write(int field, Boolean b) throws SofException;

    void write(int field, BigDecimal b) throws SofException;

    void write(int field, Date d) throws SofException;

    void write(int field, Character c) throws SofException;

    void write(int field, String string) throws SofException;

    void write(int field, String[] string) throws SofException;

    void write(int field, byte[] array) throws SofException;

    void write(int field, byte[] array, int position, int length) throws SofException;

    void write(int field, SerialisableObject o) throws SofException;

    void write(int field, SerialisableObject[] subObjectArray, Class<? extends SerialisableObject> uniformClass) throws SofException;

    void write(int field, Collection<? extends SerialisableObject> subObjectArray, Class<? extends SerialisableObject> uniformClass) throws SofException;

    void write(int field, SerialisableObject[] subObjectArray) throws SofException;

    SofConfiguration getConfiguration();
}
