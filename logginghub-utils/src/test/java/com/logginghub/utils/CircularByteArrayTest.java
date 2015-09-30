package com.logginghub.utils;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by james on 24/08/15.
 */
public class CircularByteArrayTest {

    @Test
    public void testSingleWrites() {
        assertThat(build(new char[]{}), is(""));
        assertThat(build('a'), is("a"));
        assertThat(build('a', 'b'), is("ab"));
        assertThat(build('a', 'b', 'c'), is("abc"));
        assertThat(build('a', 'b', 'c', 'd'), is("bcd"));
        assertThat(build('a', 'b', 'c', 'd', 'e'), is("cde"));
        assertThat(build('a', 'b', 'c', 'd', 'e', 'f'), is("def"));
        assertThat(build('a', 'b', 'c', 'd', 'e', 'f', 'g'), is("efg"));
    }

    @Test
    public void testLength() throws UnsupportedEncodingException {
        assertThat(array(new String[]{}).getLength(), is(0));
        assertThat(array("a").getLength(), is(1));
        assertThat(array("ab").getLength(), is(2));
        assertThat(array("abc").getLength(), is(3));
        assertThat(array("abcd").getLength(), is(3));
        assertThat(array("abcde").getLength(), is(3));
        assertThat(array("abcdef").getLength(), is(3));
        assertThat(array("abcdefg").getLength(), is(3));

        assertThat(array("ab", "c").getLength(), is(3));
        assertThat(array("ab", "cd").getLength(), is(3));
        assertThat(array("ab", "cde").getLength(), is(3));
        assertThat(array("abcd", "defg").getLength(), is(3));
    }


    @Test
    public void testBlockWrites() throws UnsupportedEncodingException {
        assertThat(build(new String[]{}), is(""));

        assertThat(build("a"), is("a"));
        assertThat(build("ab"), is("ab"));
        assertThat(build("abc"), is("abc"));
        assertThat(build("abcd"), is("bcd"));
        assertThat(build("abcde"), is("cde"));
        assertThat(build("abcdef"), is("def"));
        assertThat(build("abcdefg"), is("efg"));

        assertThat(build("ab", "c"), is("abc"));
        assertThat(build("ab", "cd"), is("bcd"));
        assertThat(build("ab", "cde"), is("cde"));
        assertThat(build("abcd", "defg"), is("efg"));
    }

    private String build(String... values) throws UnsupportedEncodingException {
        CircularByteArray array = new CircularByteArray(3);
        for (String value : values) {
            array.write(value.getBytes("US-ASCII"));
        }

        return new String(array.getBytes());
    }

    private CircularByteArray array(String... values) throws UnsupportedEncodingException {
        CircularByteArray array = new CircularByteArray(3);
        for (String value : values) {
            array.write(value.getBytes("US-ASCII"));
        }

        return array;
    }

    private CircularByteArray array(char... values) throws UnsupportedEncodingException {
        CircularByteArray array = new CircularByteArray(3);
        for (char value : values) {
            array.write((byte) value);
        }

        return array;
    }

    private String build(char... values) {
        CircularByteArray array = new CircularByteArray(3);
        for (char value : values) {
            array.write((byte) value);
        }

        return new String(array.getBytes());
    }

}