package com.logginghub.messaging2.encoding;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;

import com.logginghub.messaging2.encoding.ByteBufferEncodeHelper;


public class TestEncodable  {

    
    @Test public void testEncode() {

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setBooleanObject(Boolean.TRUE);
        object.setBooleanType(false);
        object.setByteObject(Byte.valueOf((byte) 7));
        object.setByteType((byte) 0xff);
        object.setCharacterObject(new Character('j'));
        object.setCharType('s');
        object.setDateObject(new Date(123123123));
        object.setDoubleObject(new Double(123.123));
        object.setDoubleType(1.23);
        object.setFloatObject(new Float(10.10f));
        object.setFloatType(20.20f);
        object.setIntegerObject(new Integer(1));
        object.setIntType(2);
        object.setLongObject(new Long(123123123123L));
        object.setLongType(300000000000L);
        object.setShortObject(new Short((short) 5555));
        object.setShortType((short) 1111);
        object.setStringObject("StringObject");
        object.setBigDecimalObject(new BigDecimal("100.125"));
        object.setByteArrayObject(new String("byte array").getBytes());
        object.setStringArrayObject(new String[] { "string1", "string2", "string3" });
        
        ByteBuffer buffer = ByteBuffer.allocate(100000);
        
        ByteBufferEncodeHelper helper = new ByteBufferEncodeHelper(buffer);
        
        object.encode(helper);
        buffer.flip();
        
        
        AllTypesDummyObject decoded = object.decode(helper);
        
        assertThat(decoded, is(object));
    }


}
