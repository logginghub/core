package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.logginghub.utils.ByteUtils;

public class TestByteUtils {

    @Test public void testParse() throws Exception {

        assertThat(ByteUtils.parse("-1K"), is(ByteUtils.kilobytes(-1)));
        
        assertThat(ByteUtils.parse("1 K"), is(ByteUtils.kilobytes(1)));
        assertThat(ByteUtils.parse("1 k"), is(ByteUtils.kilobytes(1)));
        assertThat(ByteUtils.parse("1 Kb"), is(ByteUtils.kilobytes(1)));
        assertThat(ByteUtils.parse("1 KB"), is(ByteUtils.kilobytes(1)));
        assertThat(ByteUtils.parse("1 Kilobyte"), is(ByteUtils.kilobytes(1)));
        assertThat(ByteUtils.parse("1 KBYTE"), is(ByteUtils.kilobytes(1)));

        assertThat(ByteUtils.parse("1 "), is(ByteUtils.bytes(1)));
        assertThat(ByteUtils.parse("1 b"), is(ByteUtils.bytes(1)));
        assertThat(ByteUtils.parse("1 B"), is(ByteUtils.bytes(1)));
        assertThat(ByteUtils.parse("1 byte"), is(ByteUtils.bytes(1)));

        assertThat(ByteUtils.parse(""), is(ByteUtils.bytes(0)));

        assertThat(ByteUtils.parse("5"), is(ByteUtils.bytes(5)));
        assertThat(ByteUtils.parse("5 KB"), is(ByteUtils.kilobytes(5)));
        assertThat(ByteUtils.parse("5 MB"), is(ByteUtils.megabytes(5)));
        assertThat(ByteUtils.parse("5 GB"), is(ByteUtils.gigabytes(5)));
        assertThat(ByteUtils.parse("5 TB"), is(ByteUtils.terabytes(5)));
        assertThat(ByteUtils.parse("5 PB"), is(ByteUtils.petabytes(5)));

        assertThat(ByteUtils.parse("5.5 KB"), is(ByteUtils.kilobytes(5) + ByteUtils.bytes(512)));
        assertThat(ByteUtils.parse("5.5 MB"), is(ByteUtils.megabytes(5) + ByteUtils.kilobytes(512)));
        assertThat(ByteUtils.parse("5.5 GB"), is(ByteUtils.gigabytes(5) + ByteUtils.megabytes(512)));
        assertThat(ByteUtils.parse("5.5 TB"), is(ByteUtils.terabytes(5) + ByteUtils.gigabytes(512)));
        assertThat(ByteUtils.parse("5.5 PB"), is(ByteUtils.petabytes(5) + ByteUtils.terabytes(512)));

        assertThat(ByteUtils.parse("5KB"), is(ByteUtils.kilobytes(5)));
        assertThat(ByteUtils.parse("5MB"), is(ByteUtils.megabytes(5)));
        assertThat(ByteUtils.parse("5GB"), is(ByteUtils.gigabytes(5)));
        assertThat(ByteUtils.parse("5TB"), is(ByteUtils.terabytes(5)));
        assertThat(ByteUtils.parse("5PB"), is(ByteUtils.petabytes(5)));
        
        
        try {
            assertThat(ByteUtils.parse("5.5"), is(ByteUtils.bytes(5)));
            fail("Should have thrown");
        }
        catch (IllegalArgumentException e) {

        }
    }

}
