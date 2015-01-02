package com.logginghub.utils.sof.fixtures;

import java.math.BigDecimal;
import java.util.Date;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class AllTypesDummyObject implements SerialisableObject {

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

    private AllTypesDummyObject subObject;
    
    private SimpleIntegerObject[] subObjectArray;
    private SerialisableObject[] nonUniformSubObjectArray;
    private SimpleStringObject subStringObject;

    private int secondIntType;
    
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
        setSubStringObject(new SimpleStringObject("nested string value"));
        setSubObjectArray(new SimpleIntegerObject[] { new SimpleIntegerObject(1), new SimpleIntegerObject(2), new SimpleIntegerObject(3) });
        setNonUniformSubObjectArray(new SerialisableObject[] { new SimpleIntegerObject(1), new SimpleStringObject("hello"), null});
    }
    
    public void setNonUniformSubObjectArray(SerialisableObject[] nonUniformSubObjectArray) {
        this.nonUniformSubObjectArray = nonUniformSubObjectArray;
    }
    
    public SerialisableObject[] getNonUniformSubObjectArray() {
        return nonUniformSubObjectArray;
    }
    
    public void setSubStringObject(SimpleStringObject subStringObject) {
        this.subStringObject = subStringObject;
    }
    
    public SimpleStringObject getSubStringObject() {
        return subStringObject;
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

    public void read(SofReader reader) throws SofException {
        shortType = reader.readShort(1);
        booleanType = reader.readBoolean(2);
        byteType = reader.readByte(3);
        intType = reader.readInt(4);
        longType = reader.readLong(5);
        floatType = reader.readFloat(6);
        doubleType = reader.readDouble(7);
        charType = reader.readChar(8);

        // Making a concious decision here to move the scary variable encodings into the middle of
        // the object rather than the end, making it more obvious if one of these blows up and
        // destroys the encoding after it
        byteArrayObject = reader.readByteArray(9);
        stringArrayObject = reader.readStringArray(10);
        subObject = (AllTypesDummyObject) reader.readObject(11);
        subStringObject = (SimpleStringObject) reader.readObject(12);
        subObjectArray = (SimpleIntegerObject[]) reader.readObjectArray(13, SimpleIntegerObject.class);
        nonUniformSubObjectArray = reader.readObjectArray(14);

        booleanObject = reader.readBooleanObject(15);
        byteObject = reader.readByteObject(16);
        shortObject = reader.readShortObject(17);
        integerObject = reader.readIntObject(18);
        longObject = reader.readLongObject(19);
        floatObject = reader.readFloatObject(20);
        doubleObject = reader.readDoubleObject(21);
        characterObject = reader.readCharObject(22);
        stringObject = reader.readString(23);
        dateObject = reader.readDate(24);
        bigDecimalObject = reader.readBigDecimal(25);
        
        secondIntType = reader.readInt(26);
    }

    public void write(SofWriter writer) throws SofException {

        writer.write(1, shortType);
        writer.write(2, booleanType);
        writer.write(3, byteType);
        writer.write(4, intType);
        writer.write(5, longType);
        writer.write(6, floatType);
        writer.write(7, doubleType);
        writer.write(8, charType);
        
        writer.write(9, byteArrayObject);
        writer.write(10, stringArrayObject);
        writer.write(11, subObject);
        writer.write(12, subStringObject);
        writer.write(13, subObjectArray, SimpleIntegerObject.class);
        writer.write(14, nonUniformSubObjectArray);
        
        writer.write(15, booleanObject);
        writer.write(16, byteObject);
        writer.write(17, shortObject);
        writer.write(18, integerObject);
        writer.write(19, longObject);
        writer.write(20, floatObject);
        writer.write(21, doubleObject);
        writer.write(22, characterObject);
        writer.write(23, stringObject);
        writer.write(24, dateObject);
        writer.write(25, bigDecimalObject);

        writer.write(26, secondIntType);
    }

    public void setSecondIntType(int secondIntType) {
        this.secondIntType = secondIntType;
    }
    
    public int getSecondIntType() {
        return secondIntType;
    }
    
    public AllTypesDummyObject getSubObject() {
        return subObject;
    }

    public void setSubObject(AllTypesDummyObject subObject) {
        this.subObject = subObject;
    }

    public void setSubObjectArray(SimpleIntegerObject[] subObjectArray) {
        this.subObjectArray = subObjectArray;
    }

    public SimpleIntegerObject[] getSubObjectArray() {
        return subObjectArray;
    }
}
