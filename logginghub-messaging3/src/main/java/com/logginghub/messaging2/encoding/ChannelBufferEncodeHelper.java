package com.logginghub.messaging2.encoding;

import java.math.BigDecimal;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;

import com.logginghub.messaging2.encoding.encodable.ReadBuffer;
import com.logginghub.messaging2.encoding.encodable.WriteBuffer;

public class ChannelBufferEncodeHelper implements WriteBuffer, ReadBuffer {

    private ChannelBuffer buffer;

    public ChannelBufferEncodeHelper(ChannelBuffer buffer) {
        this.buffer = buffer;
    }

    public byte[] readByteArray() {
        int length = buffer.readInt();
        byte[] bytes;
        if (length != -1) {
            bytes = new byte[length];
            buffer.readBytes(bytes);
        }
        else {
            bytes = null;
        }
        return bytes;
    }

    public boolean readBoolean() {
        return buffer.readByte() != 0;
    }

    public byte readByte() {
        return buffer.readByte();
    }

    public short readShort() {
        return buffer.readShort();
    }

    public int readInt() {
        return buffer.readInt();
    }

    public long readLong() {
        return buffer.readLong();
    }

    public float readFloat() {
        return buffer.readFloat();
    }

    public double readDouble() {
        return buffer.readDouble();
    }

    public char readChar() {
        return buffer.readChar();
    }

    public String readString() {
        byte[] byteArray = readByteArray();
        String result;
        if (byteArray != null) {
            result = new String(byteArray);
        }
        else {
            result = null;
        }
        
        return result;
    }

    public Date readDate() {
        return new Date(buffer.readLong());
    }

    public BigDecimal readBigDecimal() {
        long unscaledValue = buffer.readLong();
        int scale = buffer.readInt();
        return BigDecimal.valueOf(unscaledValue, scale);
    }

    public String[] readStringArray() {
        int length = buffer.readInt();
        String[] array = new String[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = readString();
        }
        return array;
    }

    public void writeByteArray(byte[] byteArrayObject) {
        buffer.writeInt(byteArrayObject.length);
        buffer.writeBytes(byteArrayObject);
    }

    public void writeBoolean(boolean booleanType) {
        buffer.writeByte(booleanType ? (byte) 1 : (byte) 0);
    }

    public void writeByte(byte byteType) {
        buffer.writeByte(byteType);
    }

    public void writeShort(short shortType) {
        buffer.writeShort(shortType);
    }

    public void writeInt(int intType) {
        buffer.writeInt(intType);
    }

    public void writeLong(long longType) {
        buffer.writeLong(longType);
    }

    public void writeFloat(float floatType) {
        buffer.writeFloat(floatType);
    }

    public void writeDouble(double doubleType) {
        buffer.writeDouble(doubleType);
    }

    public void writeChar(char charType) {
        buffer.writeChar(charType);
    }

    public void writeString(String stringObject) {
        if (stringObject == null) {
            writeNullByteArray();
        }
        else {
            writeByteArray(stringObject.getBytes());
        }
    }

    private void writeNullByteArray() {
        buffer.writeInt(-1);
    }

    public void writeDate(Date dateObject) {
        buffer.writeLong(dateObject.getTime());
    }

    public void writeBigDecimal(BigDecimal bigDecimalObject) {
        buffer.writeLong(bigDecimalObject.unscaledValue().longValue());
        buffer.writeInt(bigDecimalObject.scale());
    }

    public void writeStringArray(String[] stringArrayObject) {
        buffer.writeInt(stringArrayObject.length);
        for (String string : stringArrayObject) {
            writeString(string);
        }
    }

    @Override public String toString() {
        return buffer.toString();
    }
}
