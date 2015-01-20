package com.logginghub.messaging2.encoding;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import com.logginghub.messaging2.encoding.encodable.Encodable;
import com.logginghub.messaging2.encoding.encodable.ReadBuffer;
import com.logginghub.messaging2.encoding.encodable.WriteBuffer;

public class AllTypesDummyObject implements Encodable<AllTypesDummyObject> {

    private boolean booleanType;
    private byte byteType;
    private short shortType;
    private int intType;
    private long longType;
    private float floatType;
    private double doubleType;
    private char charType;

    private Boolean booleanObject;
    private Byte byteObject;
    private Short shortObject;
    private Integer integerObject;
    private Long longObject;
    private Float floatObject;
    private Double doubleObject;
    private Character characterObject;

    private String stringObject;
    private Date dateObject;

    private BigDecimal bigDecimalObject;
    private byte[] byteArrayObject;

    private String[] stringArrayObject;
    
    public AllTypesDummyObject() {
        setBooleanObject(Boolean.TRUE);
        setBooleanType(false);
        setByteObject(Byte.valueOf((byte) 7));
        setByteType((byte) 0xff);
        setCharacterObject(new Character('j'));
        setCharType('s');
        setDateObject(new Date(123123123));
        setDoubleObject(new Double(123.123));
        setDoubleType(1.23);
        setFloatObject(new Float(10.10f));
        setFloatType(20.20f);
        setIntegerObject(new Integer(1));
        setIntType(2);
        setLongObject(new Long(123123123123L));
        setLongType(300000000000L);
        setShortObject(new Short((short) 5555));
        setShortType((short) 1111);
        setStringObject("StringObject");
        setBigDecimalObject(new BigDecimal("100.125"));
        setByteArrayObject(new String("byte array").getBytes());
        setStringArrayObject(new String[] { "string1", "string2", "string3" });
    }

    public void setStringArrayObject(String[] stringArrayObject) {
        this.stringArrayObject = stringArrayObject;
    }

    public String[] getStringArrayObject() {
        return stringArrayObject;
    }

    public void setByteArrayObject(byte[] byteArrayObject) {
        this.byteArrayObject = byteArrayObject;
    }

    public byte[] getByteArrayObject() {
        return byteArrayObject;
    }

    public void setBigDecimalObject(BigDecimal bigDecimalObject) {
        this.bigDecimalObject = bigDecimalObject;
    }

    public BigDecimal getBigDecimalObject() {
        return bigDecimalObject;
    }

    public boolean isBooleanType() {
        return booleanType;
    }

    public void setBooleanType(boolean booleanType) {
        this.booleanType = booleanType;
    }

    public byte getByteType() {
        return byteType;
    }

    public void setByteType(byte byteType) {
        this.byteType = byteType;
    }

    public short getShortType() {
        return shortType;
    }

    public void setShortType(short shortType) {
        this.shortType = shortType;
    }

    public int getIntType() {
        return intType;
    }

    public void setIntType(int intType) {
        this.intType = intType;
    }

    public long getLongType() {
        return longType;
    }

    public void setLongType(long longType) {
        this.longType = longType;
    }

    public float getFloatType() {
        return floatType;
    }

    public void setFloatType(float floatType) {
        this.floatType = floatType;
    }

    public double getDoubleType() {
        return doubleType;
    }

    public void setDoubleType(double doubleType) {
        this.doubleType = doubleType;
    }

    public char getCharType() {
        return charType;
    }

    public void setCharType(char charType) {
        this.charType = charType;
    }

    public Boolean getBooleanObject() {
        return booleanObject;
    }

    public void setBooleanObject(Boolean booleanObject) {
        this.booleanObject = booleanObject;
    }

    public Byte getByteObject() {
        return byteObject;
    }

    public void setByteObject(Byte byteObject) {
        this.byteObject = byteObject;
    }

    public Short getShortObject() {
        return shortObject;
    }

    public void setShortObject(Short shortObject) {
        this.shortObject = shortObject;
    }

    public Integer getIntegerObject() {
        return integerObject;
    }

    public void setIntegerObject(Integer integerObject) {
        this.integerObject = integerObject;
    }

    public Long getLongObject() {
        return longObject;
    }

    public void setLongObject(Long longObject) {
        this.longObject = longObject;
    }

    public Float getFloatObject() {
        return floatObject;
    }

    public void setFloatObject(Float floatObject) {
        this.floatObject = floatObject;
    }

    public Double getDoubleObject() {
        return doubleObject;
    }

    public void setDoubleObject(Double doubleObject) {
        this.doubleObject = doubleObject;
    }

    public Character getCharacterObject() {
        return characterObject;
    }

    public void setCharacterObject(Character characterObject) {
        this.characterObject = characterObject;
    }

    public String getStringObject() {
        return stringObject;
    }

    public void setStringObject(String stringObject) {
        this.stringObject = stringObject;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public void encode(WriteBuffer buffer) {
        buffer.writeBoolean(booleanType);
        buffer.writeByte(byteType);
        buffer.writeShort(shortType);
        buffer.writeInt(intType);
        buffer.writeLong(longType);
        buffer.writeFloat(floatType);
        buffer.writeDouble(doubleType);
        buffer.writeChar(charType);

        buffer.writeBoolean(booleanObject);
        buffer.writeByte(byteObject);
        buffer.writeShort(shortObject);
        buffer.writeInt(integerObject);
        buffer.writeLong(longObject);
        buffer.writeFloat(floatObject);
        buffer.writeDouble(doubleObject);
        buffer.writeChar(characterObject);

        System.out.println(buffer);
        buffer.writeString(stringObject);
        buffer.writeDate(dateObject);

        buffer.writeBigDecimal(bigDecimalObject);
        buffer.writeStringArray(stringArrayObject);
        buffer.writeByteArray(byteArrayObject);
    }

    public AllTypesDummyObject decode(ReadBuffer buffer) {

        AllTypesDummyObject decoded = new AllTypesDummyObject();

        decoded.booleanType = buffer.readBoolean();
        decoded.byteType = buffer.readByte();
        decoded.shortType = buffer.readShort();
        decoded.intType = buffer.readInt();
        decoded.longType = buffer.readLong();
        decoded.floatType = buffer.readFloat();
        decoded.doubleType = buffer.readDouble();
        decoded.charType = buffer.readChar();

        decoded.booleanObject = buffer.readBoolean();
        decoded.byteObject = buffer.readByte();
        decoded.shortObject = buffer.readShort();
        decoded.integerObject = buffer.readInt();
        decoded.longObject = buffer.readLong();
        decoded.floatObject = buffer.readFloat();
        decoded.doubleObject = buffer.readDouble();
        decoded.characterObject = buffer.readChar();

        System.out.println(buffer);
        decoded.stringObject = buffer.readString();
        decoded.dateObject = buffer.readDate();

        decoded.bigDecimalObject = buffer.readBigDecimal();
        decoded.stringArrayObject = buffer.readStringArray();
        decoded.byteArrayObject = buffer.readByteArray();

        return decoded;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bigDecimalObject == null) ? 0 : bigDecimalObject.hashCode());
        result = prime * result + ((booleanObject == null) ? 0 : booleanObject.hashCode());
        result = prime * result + (booleanType ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(byteArrayObject);
        result = prime * result + ((byteObject == null) ? 0 : byteObject.hashCode());
        result = prime * result + byteType;
        result = prime * result + charType;
        result = prime * result + ((characterObject == null) ? 0 : characterObject.hashCode());
        result = prime * result + ((dateObject == null) ? 0 : dateObject.hashCode());
        result = prime * result + ((doubleObject == null) ? 0 : doubleObject.hashCode());
        long temp;
        temp = Double.doubleToLongBits(doubleType);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((floatObject == null) ? 0 : floatObject.hashCode());
        result = prime * result + Float.floatToIntBits(floatType);
        result = prime * result + intType;
        result = prime * result + ((integerObject == null) ? 0 : integerObject.hashCode());
        result = prime * result + ((longObject == null) ? 0 : longObject.hashCode());
        result = prime * result + (int) (longType ^ (longType >>> 32));
        result = prime * result + ((shortObject == null) ? 0 : shortObject.hashCode());
        result = prime * result + shortType;
        result = prime * result + Arrays.hashCode(stringArrayObject);
        result = prime * result + ((stringObject == null) ? 0 : stringObject.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AllTypesDummyObject other = (AllTypesDummyObject) obj;
        if (bigDecimalObject == null) {
            if (other.bigDecimalObject != null) return false;
        }
        else if (!bigDecimalObject.equals(other.bigDecimalObject)) return false;
        if (booleanObject == null) {
            if (other.booleanObject != null) return false;
        }
        else if (!booleanObject.equals(other.booleanObject)) return false;
        if (booleanType != other.booleanType) return false;
        if (!Arrays.equals(byteArrayObject, other.byteArrayObject)) return false;
        if (byteObject == null) {
            if (other.byteObject != null) return false;
        }
        else if (!byteObject.equals(other.byteObject)) return false;
        if (byteType != other.byteType) return false;
        if (charType != other.charType) return false;
        if (characterObject == null) {
            if (other.characterObject != null) return false;
        }
        else if (!characterObject.equals(other.characterObject)) return false;
        if (dateObject == null) {
            if (other.dateObject != null) return false;
        }
        else if (!dateObject.equals(other.dateObject)) return false;
        if (doubleObject == null) {
            if (other.doubleObject != null) return false;
        }
        else if (!doubleObject.equals(other.doubleObject)) return false;
        if (Double.doubleToLongBits(doubleType) != Double.doubleToLongBits(other.doubleType)) return false;
        if (floatObject == null) {
            if (other.floatObject != null) return false;
        }
        else if (!floatObject.equals(other.floatObject)) return false;
        if (Float.floatToIntBits(floatType) != Float.floatToIntBits(other.floatType)) return false;
        if (intType != other.intType) return false;
        if (integerObject == null) {
            if (other.integerObject != null) return false;
        }
        else if (!integerObject.equals(other.integerObject)) return false;
        if (longObject == null) {
            if (other.longObject != null) return false;
        }
        else if (!longObject.equals(other.longObject)) return false;
        if (longType != other.longType) return false;
        if (shortObject == null) {
            if (other.shortObject != null) return false;
        }
        else if (!shortObject.equals(other.shortObject)) return false;
        if (shortType != other.shortType) return false;
        if (!Arrays.equals(stringArrayObject, other.stringArrayObject)) return false;
        if (stringObject == null) {
            if (other.stringObject != null) return false;
        }
        else if (!stringObject.equals(other.stringObject)) return false;
        return true;
    }

    @Override public String toString() {
        return "AllTypesDummyObject [booleanType=" +
               booleanType +
               ", byteType=" +
               byteType +
               ", shortType=" +
               shortType +
               ", intType=" +
               intType +
               ", longType=" +
               longType +
               ", floatType=" +
               floatType +
               ", doubleType=" +
               doubleType +
               ", charType=" +
               charType +
               ", booleanObject=" +
               booleanObject +
               ", byteObject=" +
               byteObject +
               ", shortObject=" +
               shortObject +
               ", integerObject=" +
               integerObject +
               ", longObject=" +
               longObject +
               ", floatObject=" +
               floatObject +
               ", doubleObject=" +
               doubleObject +
               ", characterObject=" +
               characterObject +
               ", stringObject=" +
               stringObject +
               ", dateObject=" +
               dateObject +
               ", bigDecimalObject=" +
               bigDecimalObject +
               ", byteArrayObject=" +
               Arrays.toString(byteArrayObject) +
               ", stringArrayObject=" +
               Arrays.toString(stringArrayObject) +
               "]";
    }

    
    
}
