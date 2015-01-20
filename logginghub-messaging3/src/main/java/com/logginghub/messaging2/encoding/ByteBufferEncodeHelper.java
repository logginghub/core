package com.logginghub.messaging2.encoding;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;

import com.logginghub.messaging2.encoding.encodable.ReadBuffer;
import com.logginghub.messaging2.encoding.encodable.WriteBuffer;

public class ByteBufferEncodeHelper implements WriteBuffer, ReadBuffer {

    private ByteBuffer buffer;

    public ByteBufferEncodeHelper(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public byte[] readByteArray() {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    public boolean readBoolean() {
        return buffer.get() != 0;
    }

    public byte readByte() {
        return buffer.get();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public long readLong() {
        return buffer.getLong();
    }

    public float readFloat() {
        return buffer.getFloat();
    }

    public double readDouble() {
        return buffer.getDouble();
    }

    public char readChar() {
        return buffer.getChar();
    }

    public String readString() {
        return new String(readByteArray());
    }

    public Date readDate() {
        return new Date(buffer.getLong());
    }

    public BigDecimal readBigDecimal() {
        long unscaledValue = buffer.getLong();
        int scale = buffer.getInt();
        return BigDecimal.valueOf(unscaledValue, scale);
    }

    public String[] readStringArray() {
        int length = buffer.getInt();
        String[] array = new String[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = readString();
        }
        return array;
    }

    public void writeByteArray(byte[] byteArrayObject) {
        buffer.putInt(byteArrayObject.length);
        buffer.put(byteArrayObject);
    }

    public void writeBoolean(boolean booleanType) {
        buffer.put(booleanType?(byte)1:(byte)0);
    }

    public void writeByte(byte byteType) {
        buffer.put(byteType);
    }

    public void writeShort(short shortType) {
        buffer.putShort(shortType);
    }

    public void writeInt(int intType) {
        buffer.putInt(intType);
    }

    public void writeLong(long longType) {
        buffer.putLong(longType);
    }

    public void writeFloat(float floatType) {
        buffer.putFloat(floatType);
    }

    public void writeDouble(double doubleType) {
        buffer.putDouble(doubleType);
    }

    public void writeChar(char charType) {
        buffer.putChar(charType);
    }

    public void writeString(String stringObject) {
        writeByteArray(stringObject.getBytes());
    }

    public void writeDate(Date dateObject) {
        buffer.putLong(dateObject.getTime());
    }

    public void writeBigDecimal(BigDecimal bigDecimalObject) {
        buffer.putLong(bigDecimalObject.unscaledValue().longValue());
        buffer.putInt(bigDecimalObject.scale());
    }

    public void writeStringArray(String[] stringArrayObject) {
        buffer.putInt(stringArrayObject.length);
        for (String string : stringArrayObject) {
            writeString(string);
        }
    }
    
    @Override public String toString() {
        return buffer.toString();
    }
}
