package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.utils.Metadata;

public class TestMetadata {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test public void testGetString() {

        Metadata metadata = new Metadata();
        metadata.put("int", 12);
        metadata.put("string", "value");

        assertThat(metadata.getInt("int", -1), is(12));
        assertThat(metadata.getString("int"), is("12"));
        assertThat(metadata.getString("string"), is("value"));

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("You tried to get the value for key 'string' out of this metadata as an int, but the object stored was actually a class java.lang.String");
        metadata.getInt("string", -1);
    }

    @Test public void testParse() {

        Metadata metadata = new Metadata();
        metadata.parse("name1='value1' name2=\"value2 with spaces\" name3=\"value3 with 'quotes' foo\"");
        assertThat(metadata.getString("name1"), is("value1"));
        assertThat(metadata.getString("name2"), is("value2 with spaces"));
        assertThat(metadata.getString("name3"), is("value3 with 'quotes' foo"));
        
    }

}
