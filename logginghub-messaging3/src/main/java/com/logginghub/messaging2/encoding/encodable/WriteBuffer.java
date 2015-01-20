package com.logginghub.messaging2.encoding.encodable;

import java.math.BigDecimal;
import java.util.Date;

public interface WriteBuffer {

    void writeByteArray(byte[] byteArrayObject);

    void writeBoolean(boolean booleanType);

    void writeByte(byte byteType);

    void writeShort(short shortType);

    void writeInt(int intType);

    void writeLong(long longType);

    void writeFloat(float floatType);

    void writeDouble(double doubleType);

    void writeChar(char charType);

    void writeString(String stringObject);

    void writeDate(Date dateObject);

    void writeBigDecimal(BigDecimal bigDecimalObject);

    void writeStringArray(String[] stringArrayObject);
    

}
