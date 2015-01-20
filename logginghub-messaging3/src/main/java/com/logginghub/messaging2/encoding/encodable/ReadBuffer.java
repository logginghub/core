package com.logginghub.messaging2.encoding.encodable;

import java.math.BigDecimal;
import java.util.Date;

public interface ReadBuffer {

    byte[] readByteArray();

    boolean readBoolean();

    byte readByte();

    short readShort();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    char readChar();

    String readString();

    Date readDate();

    BigDecimal readBigDecimal();

    String[] readStringArray();
}
