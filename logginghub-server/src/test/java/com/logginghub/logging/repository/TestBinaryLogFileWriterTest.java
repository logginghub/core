package com.logginghub.logging.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.repository.BinaryLogFileReader;
import com.logginghub.logging.repository.BinaryLogFileWriter;

public class TestBinaryLogFileWriterTest {
    @Test public void testSingleItem() throws IOException {

        File tempFile = File.createTempFile("test", "file");
        BinaryLogFileWriter writer = new BinaryLogFileWriter(tempFile);
        
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        writer.write(event);
        writer.close();
        
        assertThat(tempFile.exists(), is(true));
        assertThat(tempFile.length(), is(greaterThan(0L)));
        
        BinaryLogFileReader reader = new BinaryLogFileReader();
        List<LogEvent> events = reader.readAll(tempFile);
        
        assertThat(events.size(), is(1));
                
        assertThat(events.get(0).getFlavour(), is(event.getFlavour()));
        assertThat(events.get(0).getFormattedException(), is(event.getFormattedException()));
        assertThat(events.get(0).getFormattedObject(), is(event.getFormattedObject()));
        assertThat(events.get(0).getJavaLevel(), is(event.getJavaLevel()));
        assertThat(events.get(0).getLevel(), is(event.getLevel()));
        assertThat(events.get(0).getLevelDescription(), is(event.getLevelDescription()));
        assertThat(events.get(0).getOriginTime(), is(event.getOriginTime()));
        assertThat(events.get(0).getLoggerName(), is(event.getLoggerName()));
        assertThat(events.get(0).getMessage(), is(event.getMessage()));
        assertThat(events.get(0).getSequenceNumber(), is(event.getSequenceNumber()));
        assertThat(events.get(0).getSourceAddress(), is(event.getSourceAddress()));
        assertThat(events.get(0).getSourceApplication(), is(event.getSourceApplication()));
        assertThat(events.get(0).getSourceClassName(), is(event.getSourceClassName()));
        assertThat(events.get(0).getSourceHost(), is(event.getSourceHost()));
        assertThat(events.get(0).getSourceMethodName(), is(event.getSourceMethodName()));
        assertThat(events.get(0).getThreadName(), is(event.getThreadName()));
    }
    
    @Test public void testLots() throws IOException {

        File tempFile = File.createTempFile("test", "file");
        BinaryLogFileWriter writer = new BinaryLogFileWriter(tempFile);
        
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        int eventCount = 100000;
        for(int i = 0; i < eventCount; i++){
        writer.write(event);
        }
        writer.close();
        
        assertThat(tempFile.exists(), is(true));
        assertThat(tempFile.length(), is(greaterThan(0L)));
        
        BinaryLogFileReader reader = new BinaryLogFileReader();
        List<LogEvent> events = reader.readAll(tempFile);
        
        assertThat(events.size(), is(eventCount));
        
        assertThat(events.get(0).getFlavour(), is(event.getFlavour()));
        assertThat(events.get(0).getFormattedException(), is(event.getFormattedException()));
        assertThat(events.get(0).getFormattedObject(), is(event.getFormattedObject()));
        assertThat(events.get(0).getJavaLevel(), is(event.getJavaLevel()));
        assertThat(events.get(0).getLevel(), is(event.getLevel()));
        assertThat(events.get(0).getLevelDescription(), is(event.getLevelDescription()));
        assertThat(events.get(0).getOriginTime(), is(event.getOriginTime()));
        assertThat(events.get(0).getLoggerName(), is(event.getLoggerName()));
        assertThat(events.get(0).getMessage(), is(event.getMessage()));
        assertThat(events.get(0).getSequenceNumber(), is(event.getSequenceNumber()));
        assertThat(events.get(0).getSourceAddress(), is(event.getSourceAddress()));
        assertThat(events.get(0).getSourceApplication(), is(event.getSourceApplication()));
        assertThat(events.get(0).getSourceClassName(), is(event.getSourceClassName()));
        assertThat(events.get(0).getSourceHost(), is(event.getSourceHost()));
        assertThat(events.get(0).getSourceMethodName(), is(event.getSourceMethodName()));
        assertThat(events.get(0).getThreadName(), is(event.getThreadName()));
    }
}
