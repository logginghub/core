package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.JSONWriter;

public class TestJSONWriter {

    private JSONWriter writer = new JSONWriter();

    @Test public void test_empty() {
        assertThat(writer.toString(), is(""));
    }

    @Test public void test_element() {
        writer.startElement().writeProperty("key", "value").endElement();
        assertThat(writer.toString(), is("{\"key\":\"value\"}"));
    }

    @Test public void test_array_single() {
        writer.startArray().startElement().writeProperty("key", "value").endElement().endArray();
        assertThat(writer.toString(), is("[{\"key\":\"value\"}]"));
    }

    @Test public void test_array_two() {
        writer.startArray().startElement().writeProperty("key", "value").endElement().startElement().writeProperty("key 2", "value 2").endElement().endArray();
        assertThat(writer.toString(), is("[{\"key\":\"value\"},{\"key 2\":\"value 2\"}]"));
    }

    @Test public void test_array_three() {
        writer.startArray();
        writer.startElement().writeProperty("key", "value").endElement();
        writer.startElement().writeProperty("key 2", "value 2").endElement();
        writer.startElement().writeProperty("key 3", "value 3").endElement();
        writer.endArray();
        assertThat(writer.toString(), is("[{\"key\":\"value\"},{\"key 2\":\"value 2\"},{\"key 3\":\"value 3\"}]"));
    }

    @Test public void test_sub_object() {
        writer.startArray();
        writer.startElement().writeProperty("key", "value");
        writer.startElementProperty("sub");
        writer.writeProperty("sub key", "sub value");
        writer.endElementProperty();
        writer.endArray();

        assertThat(writer.toString(), is("[{\"key\":\"value\",\"sub\":{\"sub key\":\"sub value\"}]"));
    }

    @Test public void test_complex_sub_object() {

        // From an example here : http://www.hunlock.com/blogs/Mastering_JSON_(_JavaScript_Object_Notation_)

        writer.startElement();

        writer.startArrayProperty("accounting");
        writer.startElement().writeProperty("firstName", "John").writeProperty("lastName", "Doe").writeProperty("age", 23).endElement();
        writer.startElement().writeProperty("firstName", "Mary").writeProperty("lastName", "Smith").writeProperty("age", 32).endElement();
        writer.endArrayProperty();

        writer.startArrayProperty("sales");
        writer.startElement().writeProperty("firstName", "Sally").writeProperty("lastName", "Green").writeProperty("age", 27).endElement();
        writer.startElement().writeProperty("firstName", "Jim").writeProperty("lastName", "Galley").writeProperty("age", 41).endElement();
        writer.startElement().writeProperty("firstName", "Anne").writeProperty("lastName", "Flan").writeProperty("age", 33).endElement();
        writer.endArrayProperty();

        writer.startArrayProperty("trading");
        writer.startElement().writeProperty("firstName", "Hugh").writeProperty("lastName", "McHugh").writeProperty("age", 23).endElement();
        writer.startElement().writeProperty("firstName", "Julia").writeProperty("lastName", "Hulia").writeProperty("age", 20).endElement();
        writer.endArrayProperty();

        writer.endElement();

        assertThat(writer.toString(), is(loadExpected(0)));
    }
    
    @Test public void test_int_array() {
        writer.write(new int[] { 100, 200, 2, 5});
        assertThat(writer.toString(), is("[100,200,2,5]"));
    }
    
    @Test public void test_string_array() {
        writer.write(new String[] { "hello", "a", "b", "c"});
        assertThat(writer.toString(), is("[\"hello\",\"a\",\"b\",\"c\"]"));
    }
    
    private String loadExpected(int index) {
        return FileUtils.readAsStringArray("jsonExpected.txt")[index];
    }

    @Test
    public void testJsonify() {
        String jsonify = JSONWriter.jsonify("\"Quoted\"");
        assertThat(jsonify, is("\\\"Quoted\\\""));
    }

}
