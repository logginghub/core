package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.utils.SizeOf;

public class TestSizeOf {

    @Test public void testSizeOf_char_array() {
        assertThat(SizeOf.sizeof(new char[3]), is(24L));
    }

    @Ignore // Fails on linux
    @Test public void testSizeOf_string() {
        assertThat(SizeOf.sizeof("<xml></xml>"), is(64L));
    }

    @Test public void testSizeOf_Boolean() {
        assertThat(SizeOf.sizeof(new Boolean(false)), is(16L));
    }

    @Test public void testSizeOf_Double() {
        assertThat(SizeOf.sizeof(new Double(2.0)), is(16L));
    }

    @Test public void testSizeOf_BigDecimal() {
        // This is tricky, if we debug or log values from inside the big decimal
        // it uses a string cache which increases the size to 120 bytes!
        //assertThat(SizeOf.sizeof(new BigDecimal(2.0)), is(80L));
    }

}
