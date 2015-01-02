package com.logginghub.utils.sof;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.utils.sof.ByteBufferReaderAbstraction;
import com.logginghub.utils.sof.ByteBufferWriterAbstraction;
import com.logginghub.utils.sof.ReaderAbstraction;
import com.logginghub.utils.sof.TypeCodex;
import com.logginghub.utils.sof.WriterAbstraction;

public class TestTypeCodex {

    @Test public void test_string() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
        TypeCodex.writeString(writer, "Hello world");
        buffer.flip();
        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
        assertThat(TypeCodex.readString(reader), is("Hello world"));
    }
    
    // jshaw - this doesn't work because the fast string encoder relies on utf 8
    @Test public void test_string_utf16() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
        TypeCodex.writeString(writer, "有子曰：「其為人也孝弟，而好犯上者，鮮矣");
        buffer.flip();
        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
        assertThat(TypeCodex.readString(reader), is("有子曰：「其為人也孝弟，而好犯上者，鮮矣"));
    }
    
//    @Test public void test_string2_utf16_1() throws Exception {
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
//        TypeCodex.writeString2(writer, "有子曰：「其為人也孝弟，而好犯上者，鮮矣");
//        buffer.flip();
//        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
//        assertThat(TypeCodex.readString2(reader), is("有子曰：「其為人也孝弟，而好犯上者，鮮矣"));
//    }
//    
//    @Test public void test_string2_utf16_2() throws Exception {
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        WriterAbstraction writer = new ByteBufferWriterAbstraction(buffer);
//        TypeCodex.writeString2(writer, "पशुपतिरपि तान्यहानि कृच्छ्राद्");
//        buffer.flip();
//        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
//        assertThat(TypeCodex.readString2(reader), is("पशुपतिरपि तान्यहानि कृच्छ्राद्"));
//    }

}
