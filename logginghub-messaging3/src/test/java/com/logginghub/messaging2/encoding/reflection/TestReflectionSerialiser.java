package com.logginghub.messaging2.encoding.reflection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;

import org.junit.Test;

import com.logginghub.messaging2.encoding.ByteBufferEncodeHelper;
import com.logginghub.messaging2.encoding.encodable.ReadBuffer;
import com.logginghub.messaging2.encoding.encodable.WriteBuffer;
import com.logginghub.messaging2.encoding.reflection.ReflectionSerialiser;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestReflectionSerialiser {

    private ReflectionSerialiser reflectionSerialiser = new ReflectionSerialiser();

    @Test public void test_encode_decode_list() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 100);
        WriteBuffer buffer = new ByteBufferEncodeHelper(byteBuffer);

        List<String> strings = new ArrayList<String>();
        strings.add("one");
        strings.add("two");
        strings.add("three");

        reflectionSerialiser.encodeWithClass(strings, buffer);

        byteBuffer.flip();
        ReadBuffer reader = new ByteBufferEncodeHelper(byteBuffer);
        List<String> decoded = reflectionSerialiser.decode(reader);

        assertThat(strings.equals(decoded), is(true));

    }

    @Test public void test_encode_decode_set() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 100);
        WriteBuffer buffer = new ByteBufferEncodeHelper(byteBuffer);

        Set<String> strings = new HashSet<String>();
        strings.add("one");
        strings.add("two");
        strings.add("three");

        reflectionSerialiser.encodeWithClass(strings, buffer);

        byteBuffer.flip();
        ReadBuffer reader = new ByteBufferEncodeHelper(byteBuffer);
        Set<String> decoded = reflectionSerialiser.decode(reader);

        assertThat(strings.equals(decoded), is(true));
    }
    
    @Test public void test_encode_decode_map() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 100);
        WriteBuffer buffer = new ByteBufferEncodeHelper(byteBuffer);

        Map<Integer, String> strings = new HashMap<Integer, String>();
        strings.put(1, "one");
        strings.put(2, "two");
        strings.put(3, "three");

        reflectionSerialiser.encodeWithClass(strings, buffer);

        byteBuffer.flip();
        ReadBuffer reader = new ByteBufferEncodeHelper(byteBuffer);
        Map<Integer, String> decoded = reflectionSerialiser.decode(reader);

        assertThat(strings.equals(decoded), is(true));

    }

    
    public enum Enumtastic {
        Yes, Completely;
    }
    
    @Test public void test_encode_decode_enum() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 100);
        WriteBuffer buffer = new ByteBufferEncodeHelper(byteBuffer);

        Enumtastic value = Enumtastic.Completely;
        reflectionSerialiser.encodeWithClass(value, buffer);

        byteBuffer.flip();
        ReadBuffer reader = new ByteBufferEncodeHelper(byteBuffer);
        Enumtastic decoded = reflectionSerialiser.decode(reader);

        assertThat(value.equals(decoded), is(true));

    }
    
}
