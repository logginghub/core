package com.logginghub.messaging2.encoding;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;

import com.logginghub.messaging2.encoding.ByteBufferEncodeHelper;
import com.logginghub.messaging2.encoding.reflection.ReflectionSerialiser;

public class TestReflectionEncodable {

    @Test public void test_ambiguous() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        AmbigousObject ambigousObject = new AmbigousObject();
        ambigousObject.setValue("hello");

        test_base(ambigousObject);
    }

    @Test public void test_base_type() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        test_base("hello");
        test_base(10);
        test_base(10L);
        test_base(10f);
        test_base(10d);
        test_base((short) 10);
        test_base((byte) 10);
        test_base('c');
        test_base(null);
    }

    private void test_base(Object in) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(100000);
        ByteBufferEncodeHelper helper = new ByteBufferEncodeHelper(buffer);
        ReflectionSerialiser serialiser = new ReflectionSerialiser();
        serialiser.encodeWithClass(in, helper);
        buffer.flip();
        Object decoded = serialiser.decode(helper);
        assertThat(decoded, is(in));

    }

    @Test public void test_nested() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        NestedObject object = new NestedObject();
        object.setTopLevelString("different value");
        object.getNestedObject().setIntType(666);

        ByteBuffer buffer = ByteBuffer.allocate(100000);

        ByteBufferEncodeHelper helper = new ByteBufferEncodeHelper(buffer);

        ReflectionSerialiser serialiser = new ReflectionSerialiser();

        serialiser.encodeWithClass(object, helper);
        buffer.flip();

        NestedObject decoded = serialiser.decode(helper);

        assertThat(decoded, is(object));

    }

    @Test public void test_encode_decode_known_class() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

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

        ReflectionSerialiser serialiser = new ReflectionSerialiser();

        serialiser.encode(object, helper);
        buffer.flip();

        AllTypesDummyObject decoded = serialiser.decode(AllTypesDummyObject.class, helper);

        assertThat(decoded, is(object));
    }

    @Test public void test_encode_decode_unknown_class() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

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

        ReflectionSerialiser serialiser = new ReflectionSerialiser();

        serialiser.encodeWithClass(object, helper);
        buffer.flip();

        AllTypesDummyObject decoded = serialiser.decode(helper);

        assertThat(decoded, is(object));
    }
}
