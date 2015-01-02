package com.logginghub.utils.data;

public class SerialisedField {

    private int type;
    private Object value;

    public SerialisedField(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String asString() {
        return (String) value;
    }

    public int asInt() {
        return (Integer) value;
    }

    public String[] asStringArray() {
        return (String[]) value;

    }

    public long asLong() {
        return (Long) value;

    }

    public byte[] asByteArray() {
        return (byte[])value;
         
    }

}
