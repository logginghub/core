package com.logginghub.utils.sof;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;

import org.junit.Test;

import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.sof.ByteBufferReaderAbstraction;
import com.logginghub.utils.sof.ExpandingByteBufferWriterAbstraction;
import com.logginghub.utils.sof.ReaderAbstraction;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofExpandingBufferSerialiser;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.SofStreamSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;
import com.logginghub.utils.sof.StreamWriterAbstraction;
import com.logginghub.utils.sof.WriterAbstraction;
import com.logginghub.utils.sof.fixtures.AllTypesDummyObject;
import com.logginghub.utils.sof.fixtures.ObjectVersion1;
import com.logginghub.utils.sof.fixtures.ObjectVersion2;
import com.logginghub.utils.sof.fixtures.SimpleIntObject;
import com.logginghub.utils.sof.fixtures.SimpleIntegerObject;
import com.logginghub.utils.sof.fixtures.SimpleNestedObject;
import com.logginghub.utils.sof.fixtures.SimpleNestedObjectWithNonUniformArray;
import com.logginghub.utils.sof.fixtures.SimpleNestedObjectWithUniformArray;
import com.logginghub.utils.sof.fixtures.SimpleStringArrayObject;
import com.logginghub.utils.sof.fixtures.SimpleStringObject;

public class TestSofSerialiser {

    private SofConfiguration sofConfiguration = new SofConfiguration();

    @Test public void test_forgiving_nested_object() throws SofException, EOFException {
        sofConfiguration.registerType(SimpleNestedObject.class, 0);
        sofConfiguration.registerType(SimpleIntObject.class, 1);

        SimpleNestedObject nested = new SimpleNestedObject();
        nested.setString("foo");
        nested.setValue(1232);
        nested.setIntObject(new SimpleIntObject(666));

        byte[] bytes = SofStreamSerialiser.write(nested, sofConfiguration);
        SimpleNestedObject decoded1 = SofStreamSerialiser.read(bytes, sofConfiguration);
        assertThat(decoded1.getString(), is("foo"));
        assertThat(decoded1.getIntObject().getValue(), is(666));
        assertThat(decoded1.getValue(), is(1232));

        sofConfiguration.unregisterType(1);
        try {
            SimpleNestedObject decoded2 = SofStreamSerialiser.read(bytes, sofConfiguration);
        }
        catch (SofException e) {
            assertThat(e.getMessage(), is("Decode failed, sub-object type '1' has not been registered"));
        }

        sofConfiguration.setAllowUnknownNestedTypes(true);
        SimpleNestedObject decoded3 = SofStreamSerialiser.read(bytes, sofConfiguration);
        assertThat(decoded3.getString(), is("foo"));
        assertThat(decoded3.getIntObject(), is(nullValue()));
        assertThat(decoded3.getValue(), is(1232));
    }

    @Test public void test_uniform_array_nested_object() throws SofException, EOFException {
        sofConfiguration.registerType(SimpleNestedObjectWithUniformArray.class, 0);
        sofConfiguration.registerType(SimpleIntObject.class, 1);

        SimpleNestedObjectWithUniformArray object = new SimpleNestedObjectWithUniformArray();

        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(null);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        SimpleNestedObjectWithUniformArray decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(nullValue()));

        // Give it an empty array
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[0]);

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(not(nullValue())));
        assertThat(decoded.getObjectArray().length, is(0));

        // Now null entries
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[] { null, null, null });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(not(nullValue())));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(decoded.getObjectArray()[1], is(nullValue()));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));

        // Now a value
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[] { null, new SimpleIntObject(22), null });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(not(nullValue())));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(decoded.getObjectArray()[1], is(not(nullValue())));
        assertThat(decoded.getObjectArray()[1].getValue(), is(22));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));

        // Now all values
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[] { new SimpleIntObject(1), new SimpleIntObject(2), new SimpleIntObject(3) });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(not(nullValue())));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0].getValue(), is(1));
        assertThat(decoded.getObjectArray()[1].getValue(), is(2));
        assertThat(decoded.getObjectArray()[2].getValue(), is(3));

        // Now something they can't decode
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[] { new SimpleIntObject(1), new SimpleIntObject(2), new SimpleIntObject(3) });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        sofConfiguration.unregisterType(1);
        try {
            SofStreamSerialiser.read(bytes, sofConfiguration);
        }
        catch (SofException e) {
            assertThat(e.getMessage(), is("Decode failed, uniform array sub-type '1' has not been registered"));
        }

        // Now relaxed mode
        object.setString("string");
        object.setValue(-13);
        object.setObjectArray(new SimpleIntObject[] { new SimpleIntObject(1), null, new SimpleIntObject(3) });

        sofConfiguration.registerType(SimpleIntObject.class, 1);
        bytes = SofStreamSerialiser.write(object, sofConfiguration);

        sofConfiguration.unregisterType(1);
        sofConfiguration.setAllowUnknownNestedTypes(true);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getObjectArray(), is(not(nullValue())));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(decoded.getObjectArray()[1], is(nullValue()));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));

        // A couple of other little bits

        // Extract with a good config
        sofConfiguration.registerType(SimpleIntObject.class, 1);
        SimpleIntObject[] extract = (SimpleIntObject[]) SofSerialiser.extract(bytes, 2, sofConfiguration);
        assertThat(extract.length, is(3));
        assertThat(extract[0].getValue(), is(1));
        assertThat(extract[1], is(nullValue()));
        assertThat(extract[2].getValue(), is(3));

        // Extract with the type missing in unforgiving mode
        sofConfiguration.unregisterType(1);
        sofConfiguration.setAllowUnknownNestedTypes(false);
        try {
            assertThat((Short) SofSerialiser.extract(bytes, 2, sofConfiguration), is((short) 666));
        }
        catch (SofException e) {
            assertThat(e.getMessage(), is("Decode failed, uniform array sub-type '1' has not been registered"));
        }

        // Extract with the type missing in forgiving mode
        sofConfiguration.unregisterType(1);
        sofConfiguration.setAllowUnknownNestedTypes(true);
        Object extracted = SofSerialiser.extract(bytes, 2, sofConfiguration);
        assertThat(extracted, is(not(nullValue())));
        assertThat(extracted, is(instanceOf(SerialisableObject[].class)));
        SerialisableObject[] extractedArray = (SerialisableObject[]) extracted;
        assertThat(extractedArray.length, is(3));

    }

    @Test public void test_string_array_nested_object() throws SofException, EOFException {
        sofConfiguration.registerType(SimpleStringArrayObject.class, 0);

        SimpleStringArrayObject object = new SimpleStringArrayObject();

        object.setString("string");
        object.setValue(-13);
        object.setStringArray(new String[] { "a", "foo", "c" });

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        SimpleStringArrayObject decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getStringArray(), is(new String[] { "a", "foo", "c" }));

        // Give it an empty array
        object.setString("string");
        object.setValue(-13);
        object.setStringArray(new String[0]);

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getStringArray(), is(not(nullValue())));
        assertThat(decoded.getStringArray().length, is(0));

        // Now null entries
        object.setString("string");
        object.setValue(-13);
        object.setStringArray(new String[] { null, null, null });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getStringArray(), is(not(nullValue())));
        assertThat(decoded.getStringArray().length, is(3));
        assertThat(decoded.getStringArray()[0], is(nullValue()));
        assertThat(decoded.getStringArray()[1], is(nullValue()));
        assertThat(decoded.getStringArray()[2], is(nullValue()));

        // Now a value
        object.setString("string");
        object.setValue(-13);
        object.setStringArray(new String[] { null, "foo", null });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getStringArray(), is(not(nullValue())));
        assertThat(decoded.getStringArray().length, is(3));
        assertThat(decoded.getStringArray()[0], is(nullValue()));
        assertThat(decoded.getStringArray()[1], is(not(nullValue())));
        assertThat(decoded.getStringArray()[1], is("foo"));
        assertThat(decoded.getStringArray()[2], is(nullValue()));

        // Now all values
        object.setString("string");
        object.setValue(-13);
        object.setStringArray(new String[] { "a", "foo", "b" });

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getString(), is("string"));
        assertThat(decoded.getValue(), is(-13));
        assertThat(decoded.getStringArray().length, is(3));
        assertThat(decoded.getStringArray()[0], is("a"));
        assertThat(decoded.getStringArray()[1], is("foo"));
        assertThat(decoded.getStringArray()[2], is("b"));
    }

    @Test public void test_extract_and_skip() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setShortType((short) 666);
        object.setStringObject("Hello");
        object.setStringArrayObject(new String[] { "string1", null, "string3" });

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);

        assertThat((Short) SofSerialiser.extract(bytes, 1, sofConfiguration), is((short) 666));
        assertThat((Boolean) SofSerialiser.extract(bytes, 2, sofConfiguration), is((boolean) false));
        assertThat((Byte) SofSerialiser.extract(bytes, 3, sofConfiguration), is((byte) 0xff));
        assertThat((Integer) SofSerialiser.extract(bytes, 4, sofConfiguration), is(2));
        assertThat((Long) SofSerialiser.extract(bytes, 5, sofConfiguration), is(300000000000L));
        assertThat((Float) SofSerialiser.extract(bytes, 6, sofConfiguration), is(20.20f));
        assertThat((Double) SofSerialiser.extract(bytes, 7, sofConfiguration), is(1.23));
        assertThat((Character) SofSerialiser.extract(bytes, 8, sofConfiguration), is('s'));

        assertThat((byte[]) SofSerialiser.extract(bytes, 9, sofConfiguration), is(new String("byte array").getBytes()));
        assertThat((String[]) SofSerialiser.extract(bytes, 10, sofConfiguration), is(new String[] { "string1", null, "string3" }));
        assertThat((Object) SofSerialiser.extract(bytes, 11, sofConfiguration), is(nullValue()));
        assertThat(((SimpleStringObject) SofSerialiser.extract(bytes, 12, sofConfiguration)).getValue(), is("nested string value"));

        SimpleIntegerObject[] array = (SimpleIntegerObject[]) SofSerialiser.extract(bytes, 13, sofConfiguration);
        assertThat(array.length, is(3));
        assertThat(array[0].getIntType(), is(1));
        assertThat(array[1].getIntType(), is(2));
        assertThat(array[2].getIntType(), is(3));

        SerialisableObject[] nonUniformArray = (SerialisableObject[]) SofSerialiser.extract(bytes, 14, sofConfiguration);
        assertThat(nonUniformArray.length, is(3));
        assertThat(nonUniformArray[0], is(instanceOf(SimpleIntegerObject.class)));
        assertThat(nonUniformArray[1], is(instanceOf(SimpleStringObject.class)));
        assertThat(nonUniformArray[2], is(nullValue()));

        assertThat((Boolean) SofSerialiser.extract(bytes, 15, sofConfiguration), is(Boolean.TRUE));
        assertThat((Byte) SofSerialiser.extract(bytes, 16, sofConfiguration), is((byte) 7));
        assertThat((Short) SofSerialiser.extract(bytes, 17, sofConfiguration), is((short) 5555));
        assertThat((Integer) SofSerialiser.extract(bytes, 18, sofConfiguration), is(1));
        assertThat((Long) SofSerialiser.extract(bytes, 19, sofConfiguration), is(123123123123L));
        assertThat((Float) SofSerialiser.extract(bytes, 20, sofConfiguration), is(10.10f));
        assertThat((Double) SofSerialiser.extract(bytes, 21, sofConfiguration), is(123.123));
        assertThat((Character) SofSerialiser.extract(bytes, 22, sofConfiguration), is('j'));
        assertThat((String) SofSerialiser.extract(bytes, 23, sofConfiguration), is("Hello"));
        assertThat((Date) SofSerialiser.extract(bytes, 24, sofConfiguration), is(new Date(123123123)));
        assertThat((BigDecimal) SofSerialiser.extract(bytes, 25, sofConfiguration), is(new BigDecimal("100.125")));

        assertThat((Integer) SofSerialiser.extract(bytes, 26, sofConfiguration), is(0));
    }

    @Test public void test_extract_and_skip_nulls() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setBigDecimalObject(null);
        object.setBooleanObject(null);
        object.setByteArrayObject(null);
        object.setByteObject(null);
        object.setCharacterObject(null);
        object.setDateObject(null);
        object.setDoubleObject(null);
        object.setFloatObject(null);
        object.setIntegerObject(null);
        object.setLongObject(null);
        object.setNonUniformSubObjectArray(null);
        object.setShortObject(null);
        object.setStringArrayObject(null);
        object.setStringObject(null);
        object.setSubObject(null);
        object.setSubObjectArray(null);
        object.setSubStringObject(null);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);

        assertThat((Short) SofSerialiser.extract(bytes, 1, sofConfiguration), is((short) 1111));
        assertThat((Boolean) SofSerialiser.extract(bytes, 2, sofConfiguration), is((boolean) false));
        assertThat((Byte) SofSerialiser.extract(bytes, 3, sofConfiguration), is((byte) 0xff));
        assertThat((Integer) SofSerialiser.extract(bytes, 4, sofConfiguration), is(2));
        assertThat((Long) SofSerialiser.extract(bytes, 5, sofConfiguration), is(300000000000L));
        assertThat((Float) SofSerialiser.extract(bytes, 6, sofConfiguration), is(20.20f));
        assertThat((Double) SofSerialiser.extract(bytes, 7, sofConfiguration), is(1.23));
        assertThat((Character) SofSerialiser.extract(bytes, 8, sofConfiguration), is('s'));

        assertThat((byte[]) SofSerialiser.extract(bytes, 9, sofConfiguration), is(nullValue()));
        assertThat((String[]) SofSerialiser.extract(bytes, 10, sofConfiguration), is(nullValue()));
        assertThat((Object) SofSerialiser.extract(bytes, 11, sofConfiguration), is(nullValue()));
        assertThat(((SimpleStringObject) SofSerialiser.extract(bytes, 12, sofConfiguration)), is(nullValue()));
        assertThat((SimpleIntegerObject[]) SofSerialiser.extract(bytes, 13, sofConfiguration), is(nullValue()));
        assertThat((SerialisableObject[]) SofSerialiser.extract(bytes, 14, sofConfiguration), is(nullValue()));
        assertThat((Boolean) SofSerialiser.extract(bytes, 15, sofConfiguration), is(nullValue()));
        assertThat((Byte) SofSerialiser.extract(bytes, 16, sofConfiguration), is(nullValue()));
        assertThat((Short) SofSerialiser.extract(bytes, 17, sofConfiguration), is(nullValue()));
        assertThat((Integer) SofSerialiser.extract(bytes, 18, sofConfiguration), is(nullValue()));
        assertThat((Long) SofSerialiser.extract(bytes, 19, sofConfiguration), is(nullValue()));
        assertThat((Float) SofSerialiser.extract(bytes, 20, sofConfiguration), is(nullValue()));
        assertThat((Double) SofSerialiser.extract(bytes, 21, sofConfiguration), is(nullValue()));
        assertThat((Character) SofSerialiser.extract(bytes, 22, sofConfiguration), is(nullValue()));
        assertThat((String) SofSerialiser.extract(bytes, 23, sofConfiguration), is(nullValue()));
        assertThat((Date) SofSerialiser.extract(bytes, 24, sofConfiguration), is(nullValue()));
        assertThat((BigDecimal) SofSerialiser.extract(bytes, 25, sofConfiguration), is(nullValue()));

        assertThat((Integer) SofSerialiser.extract(bytes, 26, sofConfiguration), is(0));
    }

    @Test public void test_sizing_actual() throws SofException {

        sofConfiguration.registerType(SimpleStringObject.class, 10);

        SimpleStringObject object = new SimpleStringObject();
        object.setValue("hello");

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        assertThat(bytes[0], is((byte) 1 /* version */));
        assertThat(bytes[1], is((byte) 0 /* flags */));
        assertThat(bytes[2], is((byte) 10 /* user registered type */));
        assertThat(bytes[3], is((byte) 1 /* one field in the object */));
        assertThat(bytes[4], is((byte) 8 /*
                                          * two bytes for field header, then byte string length plus
                                          * 5 chars
                                          */));
        assertThat(bytes[5], is((byte) 1 /* field index */));
        assertThat(bytes[6], is((byte) (Integer.parseInt("01000000", 2) | (6 - 1)/*
                                                                                  * field type, with
                                                                                  * varint encoding
                                                                                  * and twos
                                                                                  * compliment
                                                                                  * negative value
                                                                                  */)));
        assertThat(bytes[7], is((byte) 5/* string length */));
        assertThat(bytes[8], is((byte) 'h'));
        assertThat(bytes[9], is((byte) 'e'));
        assertThat(bytes[10], is((byte) 'l'));
        assertThat(bytes[11], is((byte) 'l'));
        assertThat(bytes[12], is((byte) 'o'));
        assertThat(bytes.length, is(13));

    }

    @Test public void test_actual_encoding() throws SofException {

        sofConfiguration.registerType(SimpleIntObject.class, 0);

        SimpleIntObject object = new SimpleIntObject();
        object.setValue(63);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        assertThat(bytes[0], is((byte) 1 /* version */));
        assertThat(bytes[1], is((byte) 0 /* flags */));
        assertThat(bytes[2], is((byte) 0 /* user registered type */));
        assertThat(bytes[3], is((byte) 1 /* one field in the object */));
        assertThat(bytes[4], is((byte) 3 /*
                                          * one byte int plus the two bytes for the field index +
                                          * header
                                          */));
        assertThat(bytes[5], is((byte) 1 /* field index */));
        assertThat(bytes[6], is((byte) (Integer.parseInt("01000000", 2) | (1 - 1)/*
                                                                                  * field type, with
                                                                                  * varint encoding
                                                                                  * and twos
                                                                                  * compliment
                                                                                  * negative value
                                                                                  */)));
        assertThat(bytes[7], is((byte) 63 /* value inside object */));
        // assertThat(combine(bytes[7], bytes[8], bytes[9], bytes[10]), is(63 /* value inside object
        // */));
        assertThat(bytes.length, is(8));
    }

    @Test public void test_object_version2() throws SofException, EOFException {
        // Going to achieve this with two classes and regsiter them under the same ID
        sofConfiguration.registerType(ObjectVersion2.class, 1);

        ObjectVersion2 objectVersion2 = new ObjectVersion2(123, "Hello", "World");

        byte[] bytes = SofStreamSerialiser.write(objectVersion2, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(objectVersion2, sofConfiguration), is(bytes));

        ObjectVersion2 decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(123));
        assertThat(decoded.getString(), is("Hello"));
        assertThat(decoded.getString2(), is("World"));
    }

    @Test public void test_add_fields_forwards_compatible() throws SofException, EOFException {
        // Going to achieve this with two classes and regsiter them under the same ID
        sofConfiguration.registerType(ObjectVersion1.class, 1);

        ObjectVersion1 objectVersion1 = new ObjectVersion1(123, "Hello");

        byte[] bytes = SofStreamSerialiser.write(objectVersion1, sofConfiguration);
        ObjectVersion1 decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(123));
        assertThat(decoded.getString(), is("Hello"));

        // Switch to a second configuration with the version 2 class
        SofConfiguration secondConfiguration = new SofConfiguration();
        secondConfiguration.registerType(ObjectVersion2.class, 1);

        // So this object will find its missing some bytes when it comes to reading the final field
        ObjectVersion2 decoded2 = SofStreamSerialiser.read(bytes, secondConfiguration);

        assertThat(decoded2.getValue(), is(123));
        assertThat(decoded2.getString(), is("Hello"));
        assertThat(decoded2.getString2(), is("default"));
    }

    @Test public void test_add_fields_backwards_compatible() throws SofException, IOException {
        // Going to achieve this with two classes and regsiter them under the same ID
        sofConfiguration.registerType(ObjectVersion2.class, 1);

        ObjectVersion2 object1Version2 = new ObjectVersion2(123, "Hello", "World");
        ObjectVersion2 object2Version2 = new ObjectVersion2(5, "Foo", "Moo");

        byte[] bytes1 = SofStreamSerialiser.write(object1Version2, sofConfiguration);
        byte[] bytes2 = SofStreamSerialiser.write(object2Version2, sofConfiguration);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bytes1);
        baos.write(bytes2);

        byte[] byteArray = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

        // TODO : this is weird, we wouldn't be putting the length if this was as stream?
        // TODO : there should be a partial message thing going on somewhere?
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bais, byteArray.length);
        ObjectVersion2 decoded1 = SofSerialiser.read(reader, sofConfiguration);
        ObjectVersion2 decoded2 = SofSerialiser.read(reader, sofConfiguration);

        assertThat(decoded1.getValue(), is(123));
        assertThat(decoded1.getString(), is("Hello"));
        assertThat(decoded1.getString2(), is("World"));

        assertThat(decoded2.getValue(), is(5));
        assertThat(decoded2.getString(), is("Foo"));
        assertThat(decoded2.getString2(), is("Moo"));

        // Switch to a second configuration with old version 1 class
        SofConfiguration secondConfiguration = new SofConfiguration();
        secondConfiguration.registerType(ObjectVersion1.class, 1);

        reader = new StreamReaderAbstraction(new ByteArrayInputStream(byteArray), byteArray.length);
        ObjectVersion1 decoded1V1 = SofSerialiser.read(reader, secondConfiguration);
        ObjectVersion1 decoded2V1 = SofSerialiser.read(reader, secondConfiguration);

        assertThat(decoded1V1.getValue(), is(123));
        assertThat(decoded1V1.getString(), is("Hello"));

        assertThat(decoded2V1.getValue(), is(5));
        assertThat(decoded2V1.getString(), is("Foo"));

    }

    @Test public void test_lazy_nested_object_decode() throws Exception {
        sofConfiguration.registerType(SimpleNestedObject.class, 0);
        sofConfiguration.registerType(SimpleIntObject.class, 1);
        sofConfiguration.setLazyDecodeOfNestedTypes(true);

        SimpleNestedObject object = new SimpleNestedObject();
        object.setValue(2341);
        object.setIntObject(new SimpleIntObject(1111));

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        SimpleNestedObject decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(2341));
        assertThat(decoded.getIntObject().getValue(), is(1111));
    }

    @Test public void test_decode_unknown_nested_object() throws Exception {

        sofConfiguration.registerType(SimpleNestedObject.class, 0);
        sofConfiguration.registerType(SimpleIntObject.class, 1);

        SimpleNestedObject object = new SimpleNestedObject();
        object.setValue(2341);
        object.setIntObject(new SimpleIntObject(1111));
        object.setString("hello");

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        SimpleNestedObject decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(2341));
        assertThat(decoded.getIntObject().getValue(), is(1111));
        assertThat(decoded.getString(), is("hello"));

        SofConfiguration secondConfiguration = new SofConfiguration();
        secondConfiguration.setAllowUnknownNestedTypes(true);
        secondConfiguration.registerType(SimpleNestedObject.class, 0);
        SimpleNestedObject secondDecoded = SofStreamSerialiser.read(bytes, secondConfiguration);

        assertThat(secondDecoded.getValue(), is(2341));
        assertThat(secondDecoded.getIntObject(), is(nullValue()));
        assertThat(secondDecoded.getString(), is("hello"));
    }

    @Test public void test_decode_unknown_nested_object_array() throws Exception {

        sofConfiguration.registerType(SimpleNestedObjectWithNonUniformArray.class, 0);
        sofConfiguration.registerType(SimpleIntObject.class, 1);
        sofConfiguration.registerType(SimpleNestedObject.class, 2);

        // Try the encode with everything null
        SimpleNestedObjectWithNonUniformArray object = new SimpleNestedObjectWithNonUniformArray();
        object.setIntObject(null);
        object.setString(null);
        object.setObjectArray(null);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        SimpleNestedObjectWithNonUniformArray decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray(), is(nullValue()));
        assertThat(decoded.getString(), is(nullValue()));

        // Try with the last item not null
        object.setString("hello");

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray(), is(nullValue()));
        assertThat(decoded.getString(), is("hello"));

        // Store an empty array with nulls
        object.setObjectArray(new SerialisableObject[3]);

//        Logger.setLevel(SofSerialiser.class, Logger.finest);
        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(decoded.getObjectArray()[1], is(nullValue()));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));
        assertThat(decoded.getString(), is("hello"));

        // Put an object in the array
        object.getObjectArray()[1] = new SimpleIntObject(231);

        bytes = SofStreamSerialiser.write(object, sofConfiguration);
        decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(((SimpleIntObject) decoded.getObjectArray()[1]).getValue(), is(231));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));
        assertThat(decoded.getString(), is("hello"));

        // Put something in the array we can't decode
        object.getObjectArray()[2] = new SimpleNestedObject();

        bytes = SofStreamSerialiser.write(object, sofConfiguration);

        SofConfiguration secondConfiguration = new SofConfiguration();
        secondConfiguration.registerType(SimpleNestedObjectWithNonUniformArray.class, 0);
        secondConfiguration.registerType(SimpleIntObject.class, 1);

        try {
            decoded = SofStreamSerialiser.read(bytes, secondConfiguration);
        }
        catch (SofException e) {
            assertThat(e.getMessage(), is("Decode failed, sub-object type '2' has not been registered"));
        }

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(((SimpleIntObject) decoded.getObjectArray()[1]).getValue(), is(231));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));
        assertThat(decoded.getString(), is("hello"));

        // With the forgiving decode setting
        secondConfiguration.setAllowUnknownNestedTypes(true);
        decoded = SofStreamSerialiser.read(bytes, secondConfiguration);

        assertThat(decoded.getIntObject(), is(nullValue()));
        assertThat(decoded.getObjectArray().length, is(3));
        assertThat(decoded.getObjectArray()[0], is(nullValue()));
        assertThat(((SimpleIntObject) decoded.getObjectArray()[1]).getValue(), is(231));
        assertThat(decoded.getObjectArray()[2], is(nullValue()));
        assertThat(decoded.getString(), is("hello"));
    }

    @Test public void test_null_user_object_not_registered() throws Exception {

        sofConfiguration.registerType(SimpleNestedObject.class, 0);

        SimpleNestedObject object = new SimpleNestedObject();
        object.setValue(2341);
        object.setIntObject(null);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        SimpleNestedObject decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(2341));
        assertThat(decoded.getIntObject(), is(nullValue()));
    }

    @Test public void test_exceed_byte_length() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject object = new AllTypesDummyObject();
        String longString = "This is going to be longer than 255 chars                                                                                                                                                                            really really really really really looooooooooooooooooooooooooooooooooooooooooooooooong                   defintely.";
        object.setStringObject(longString);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);

        AllTypesDummyObject decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getStringObject(), is(longString));
    }

    @Test public void test_simple_with_compression() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setIntType(666);

        sofConfiguration.setCompressed(false);
        byte[] uncompressed = SofStreamSerialiser.write(object, sofConfiguration);
        byte[] uncompressed2 = SofExpandingBufferSerialiser.write(object, sofConfiguration);
        assertThat(uncompressed2, is(uncompressed));

        sofConfiguration.setCompressed(true);
        byte[] compressed = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(uncompressed2, is(uncompressed));

        // This shouldn't matter, but lets set it to false to be suure
        sofConfiguration.setCompressed(false);
        AllTypesDummyObject fromUncompressed = SofStreamSerialiser.read(uncompressed, sofConfiguration);
        AllTypesDummyObject fromCompressed = SofStreamSerialiser.read(compressed, sofConfiguration);

        assertThat(fromUncompressed.getIntType(), is(666));
        assertThat(fromCompressed.getIntType(), is(666));
        assertThat(compressed.length, is(lessThan(uncompressed.length)));
    }

    @Test public void test_simple() throws Exception {

        sofConfiguration.registerType(SimpleIntObject.class, 0);

        SimpleIntObject object = new SimpleIntObject();
        object.setValue(666);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        int expected = 0;

        expected += 1; /* version */
        expected += 1; /* flags */
        expected += 1; /* type */
        expected += 1; /* field count */
        expected += 1; /* length */
        expected += 1; /* field #1 type */
        expected += 1; /* field #1index */
        expected += 2; /* field #1 width */

        assertThat(bytes.length, is(expected));

        SimpleIntObject fromBytes = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(fromBytes, is(not(nullValue())));
        assertThat(fromBytes.getValue(), is(666));

    }

    @Test public void test_with_sub_object() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject subObject = new AllTypesDummyObject();
        subObject.setShortType((short) 2221);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setShortType((short) 666);
        object.setSubObject(subObject);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        AllTypesDummyObject fromBytes = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(fromBytes, is(not(nullValue())));
        assertThat(fromBytes.isBooleanType(), is(false));
        assertThat(fromBytes.getByteType(), is((byte) 0xff));
        assertThat(fromBytes.getCharType(), is('s'));
        assertThat(fromBytes.getDoubleType(), is(1.23));
        assertThat(fromBytes.getFloatType(), is(20.20f));
        assertThat(fromBytes.getIntType(), is(2));
        assertThat(fromBytes.getLongType(), is(300000000000L));
        assertThat(fromBytes.getShortType(), is((short) 666));

        assertThat(fromBytes.getFloatObject(), is(10.10f));
        assertThat(fromBytes.getCharacterObject(), is('j'));
        assertThat(fromBytes.getBooleanObject(), is(Boolean.TRUE));
        assertThat(fromBytes.getByteObject(), is((byte) 7));
        assertThat(fromBytes.getDateObject(), is(new Date(123123123)));
        assertThat(fromBytes.getDoubleObject(), is(123.123));
        assertThat(fromBytes.getIntegerObject(), is(1));
        assertThat(fromBytes.getLongObject(), is(123123123123L));
        assertThat(fromBytes.getShortObject(), is((short) 5555));
        assertThat(fromBytes.getStringObject(), is("StringObject"));
        assertThat(fromBytes.getBigDecimalObject().doubleValue(), is(100.125));
        assertThat(fromBytes.getByteArrayObject(), is(new String("byte array").getBytes()));
        assertThat(fromBytes.getStringArrayObject(), is(new String[] { "string1", "string2", "string3" }));

        assertThat(fromBytes.getSubObject().getShortType(), is((short) 2221));

    }

    @Test public void test_micro_format_with_sub_object() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject subObject = new AllTypesDummyObject();
        subObject.setShortType((short) 2221);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setShortType((short) 666);
        object.setSubObject(subObject);

        byte[] bytesNormal = SofStreamSerialiser.write(object, sofConfiguration);
        sofConfiguration.setMicroFormat(true);
        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);

        assertThat(bytes.length, is(lessThan(bytesNormal.length)));
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        AllTypesDummyObject fromBytes = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(fromBytes, is(not(nullValue())));
        assertThat(fromBytes.isBooleanType(), is(false));
        assertThat(fromBytes.getByteType(), is((byte) 0xff));
        assertThat(fromBytes.getCharType(), is('s'));
        assertThat(fromBytes.getDoubleType(), is(1.23));
        assertThat(fromBytes.getFloatType(), is(20.20f));
        assertThat(fromBytes.getIntType(), is(2));
        assertThat(fromBytes.getLongType(), is(300000000000L));
        assertThat(fromBytes.getShortType(), is((short) 666));

        assertThat(fromBytes.getFloatObject(), is(10.10f));
        assertThat(fromBytes.getCharacterObject(), is('j'));
        assertThat(fromBytes.getBooleanObject(), is(Boolean.TRUE));
        assertThat(fromBytes.getByteObject(), is((byte) 7));
        assertThat(fromBytes.getDateObject(), is(new Date(123123123)));
        assertThat(fromBytes.getDoubleObject(), is(123.123));
        assertThat(fromBytes.getIntegerObject(), is(1));
        assertThat(fromBytes.getLongObject(), is(123123123123L));
        assertThat(fromBytes.getShortObject(), is((short) 5555));
        assertThat(fromBytes.getStringObject(), is("StringObject"));
        assertThat(fromBytes.getBigDecimalObject().doubleValue(), is(100.125));
        assertThat(fromBytes.getByteArrayObject(), is(new String("byte array").getBytes()));
        assertThat(fromBytes.getStringArrayObject(), is(new String[] { "string1", "string2", "string3" }));

        assertThat(fromBytes.getSubObject().getShortType(), is((short) 2221));

    }

    @Test public void test_micro_simple() throws Exception {
        sofConfiguration.registerType(ObjectVersion2.class, 1);
        sofConfiguration.setMicroFormat(true);
        
        ObjectVersion2 object = new ObjectVersion2(1111, "aaaa", "bbbb");

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        ObjectVersion2 decoded = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(decoded.getValue(), is(1111));
        assertThat(decoded.getString(), is("aaaa"));
        assertThat(decoded.getString2(), is("bbbb"));
    }

    @Test public void test_micro_format_without_sub_object() throws Exception {

//        Logger.setLevel(Logger.finest, CountingWriterAbstraction.class, TypeCodex.class, SofSerialiser.class, DefaultSofWriter.class, StreamWriterAbstraction.class, StreamReaderAbstraction.class);
        
        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleStringObject.class, 1);
        sofConfiguration.registerType(SimpleIntegerObject.class, 2);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setShortType((short) 666);

//        byte[] bytesNormal = SofStreamSerialiser.write(object, sofConfiguration);
        sofConfiguration.setMicroFormat(true);
        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);

//        assertThat(bytes.length, is(lessThan(bytesNormal.length)));
//        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        AllTypesDummyObject fromBytes = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(fromBytes, is(not(nullValue())));
        assertThat(fromBytes.isBooleanType(), is(false));
        assertThat(fromBytes.getByteType(), is((byte) 0xff));
        assertThat(fromBytes.getCharType(), is('s'));
        assertThat(fromBytes.getDoubleType(), is(1.23));
        assertThat(fromBytes.getFloatType(), is(20.20f));
        assertThat(fromBytes.getIntType(), is(2));
        assertThat(fromBytes.getLongType(), is(300000000000L));
        assertThat(fromBytes.getShortType(), is((short) 666));

        assertThat(fromBytes.getFloatObject(), is(10.10f));
        assertThat(fromBytes.getCharacterObject(), is('j'));
        assertThat(fromBytes.getBooleanObject(), is(Boolean.TRUE));
        assertThat(fromBytes.getByteObject(), is((byte) 7));
        assertThat(fromBytes.getDateObject(), is(new Date(123123123)));
        assertThat(fromBytes.getDoubleObject(), is(123.123));
        assertThat(fromBytes.getIntegerObject(), is(1));
        assertThat(fromBytes.getLongObject(), is(123123123123L));
        assertThat(fromBytes.getShortObject(), is((short) 5555));
        assertThat(fromBytes.getStringObject(), is("StringObject"));
        assertThat(fromBytes.getBigDecimalObject().doubleValue(), is(100.125));
        assertThat(fromBytes.getByteArrayObject(), is(new String("byte array").getBytes()));
        assertThat(fromBytes.getStringArrayObject(), is(new String[] { "string1", "string2", "string3" }));

    }

    @Test public void test_with_sub_object_array() throws Exception {

        sofConfiguration.registerType(AllTypesDummyObject.class, 0);
        sofConfiguration.registerType(SimpleIntegerObject.class, 1);
        sofConfiguration.registerType(SimpleStringObject.class, 2);

        SimpleIntegerObject[] subObjectArray = new SimpleIntegerObject[] { new SimpleIntegerObject(5), new SimpleIntegerObject(10) };

        AllTypesDummyObject subObject = new AllTypesDummyObject();
        subObject.setShortType((short) 2221);

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setShortType((short) 666);
        object.setSubObject(subObject);
        object.setSubObjectArray(subObjectArray);

        byte[] bytes = SofStreamSerialiser.write(object, sofConfiguration);
        assertThat(SofExpandingBufferSerialiser.write(object, sofConfiguration), is(bytes));

        AllTypesDummyObject fromBytes = SofStreamSerialiser.read(bytes, sofConfiguration);

        assertThat(fromBytes, is(not(nullValue())));
        assertThat(fromBytes.getShortType(), is((short) 666));
        assertThat(fromBytes.getSubObject().getShortType(), is((short) 2221));
        assertThat(fromBytes.getSubObjectArray().length, is(2));
        assertThat(fromBytes.getSubObjectArray()[0].getIntType(), is(5));
        assertThat(fromBytes.getSubObjectArray()[1].getIntType(), is(10));

    }

    @Test public void test_int_encoding() throws Exception {

        validateInt(-65, 2);
        validateInt(-64, 1);
        validateInt(-63, 1);
        validateInt(-10, 1);
        validateInt(0, 1);
        validateInt(10, 1);
        validateInt(63, 1);
        validateInt(64, 2);
        validateInt(127, 2);

    }
    
    @Test public void test_long_encoding() throws Exception {

        validateLong(-65, 2);
        validateLong(-64, 1);
        validateLong(-63, 1);
        validateLong(-10, 1);
        validateLong(0, 1);
        validateLong(10, 1);
        validateLong(63, 1);
        validateLong(64, 2);
        validateLong(127, 2);
        
        validateLong(2147483647, 5);
        validateLong(-2147483648, 5);
        validateLong(2147483648L, 5);
        validateLong(-2147483648L, 5);

        validateLong(9223372036854775807L, 10);
        validateLong(-9223372036854775808L, 10);
        
        
    }

    private void validateInt(int value, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WriterAbstraction out = new StreamWriterAbstraction(baos);
        SofSerialiser.writeInt(out, value);
        byte[] byteArray = baos.toByteArray();
        assertThat(byteArray.length, is(length));
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        ReaderAbstraction in = new StreamReaderAbstraction(bais, 212312312);
        int readInt = SofSerialiser.readInt(in);
        assertThat(readInt, is(value));

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        ExpandingByteBufferWriterAbstraction out2 = new ExpandingByteBufferWriterAbstraction(buffer);
        SofSerialiser.writeInt(out2, value);
        assertThat(buffer.getBuffer().position(), is(length));
        ByteBuffer buffer2 = buffer.getBuffer();
        buffer2.flip();
        ByteBufferReaderAbstraction in2 = new ByteBufferReaderAbstraction(buffer2);
        int readInt2 = SofSerialiser.readInt(in2);
        assertThat(readInt2, is(value));

    }
    
    private void validateLong(long value, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WriterAbstraction out = new StreamWriterAbstraction(baos);
        SofSerialiser.writeLong(out, value);
        byte[] byteArray = baos.toByteArray();
        assertThat(byteArray.length, is(length));
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        ReaderAbstraction in = new StreamReaderAbstraction(bais, 212312312);
        long read = SofSerialiser.readLong(in);
        assertThat(read, is(value));

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        ExpandingByteBufferWriterAbstraction out2 = new ExpandingByteBufferWriterAbstraction(buffer);
        SofSerialiser.writeLong(out2, value);
        assertThat(buffer.getBuffer().position(), is(length));
        ByteBuffer buffer2 = buffer.getBuffer();
        buffer2.flip();
        ByteBufferReaderAbstraction in2 = new ByteBufferReaderAbstraction(buffer2);
        long read2 = SofSerialiser.readLong(in2);
        assertThat(read2, is(value));

    }

}
