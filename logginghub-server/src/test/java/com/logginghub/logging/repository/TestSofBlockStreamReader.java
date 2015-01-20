package com.logginghub.logging.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.SofBlockPointer;
import com.logginghub.logging.repository.SofBlockStreamReader;
import com.logginghub.logging.repository.SofBlockStreamWriter;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.logging.modules.LogEventFixture1;

public class TestSofBlockStreamReader {

    @Test public void testLoadPointers() throws Exception {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);

        File file = FileUtils.createRandomTestFileForClass(TestSofBlockStreamWriter.class);
        FileOutputStream outputStream = new FileOutputStream(file);
        SofBlockStreamWriter streamer = new SofBlockStreamWriter(outputStream, configuration, 400);

        streamer.write(LogEventFixture1.event1);
        streamer.write(LogEventFixture1.event2);
        streamer.write(LogEventFixture1.event3);
        streamer.write(LogEventFixture1.event4);
        streamer.write(LogEventFixture1.event5);
        streamer.write(LogEventFixture1.event6);
        streamer.write(LogEventFixture1.event7);
        streamer.write(LogEventFixture1.event8);
        streamer.write(LogEventFixture1.event9);

        streamer.close();
        outputStream.close();

        SofBlockStreamReader reader = new SofBlockStreamReader(configuration);
        List<SofBlockPointer> loadPointers = reader.loadPointers(file);
        assertThat(loadPointers.size(), is(5));

        final Bucket<LogEvent> events = new Bucket<LogEvent>();
        reader.visit(new FileInputStream(file), new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                events.add((LogEvent) t);
            }
        }, loadPointers.get(0));
        
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getMessage(), is(LogEventFixture1.event1.getMessage()));
        assertThat(events.get(1).getMessage(), is(LogEventFixture1.event2.getMessage()));
        
        // Try another
        events.clear();
        reader.visit(new FileInputStream(file), new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                events.add((LogEvent) t);
            }
        }, loadPointers.get(3));
        
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getMessage(), is(LogEventFixture1.event7.getMessage()));
        assertThat(events.get(1).getMessage(), is(LogEventFixture1.event8.getMessage()));
        
    }

}
