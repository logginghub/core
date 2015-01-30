package com.logginghub.logging.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.RotatingHelper;
import com.logginghub.logging.repository.SofBlockStreamRotatingReader;
import com.logginghub.logging.repository.SofBlockStreamWriter;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.logging.modules.LogEventFixture1;

public class TestSofBlockStreamRotatingReader {

    @Test public void testVisit() throws Exception {

        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);
        String postfix = ".postfix";
        String prefix = "prefix.";
        File folder = FileUtils.createRandomTestFolderForClass(getClass());
        folder.mkdirs();

        File firstFile = new File(folder, "prefix.19700101.000000.0.postfix");
        File secondFile = new File(folder, "prefix.19700101.000000.1.postfix");
        File thirdFile = new File(folder, "prefix.19700101.010000.0.postfix");

        FileOutputStream fos1 = new FileOutputStream(firstFile);
        FileOutputStream fos2 = new FileOutputStream(secondFile);
        FileOutputStream fos3 = new FileOutputStream(thirdFile);

        SofBlockStreamWriter writer1 = new SofBlockStreamWriter(fos1, configuration, 200);
        SofBlockStreamWriter writer2 = new SofBlockStreamWriter(fos2, configuration, 200);
        SofBlockStreamWriter writer3 = new SofBlockStreamWriter(fos3, configuration, 200);

        writer1.write(LogEventFixture1.event1);
        writer1.write(LogEventFixture1.event2);
        writer1.write(LogEventFixture1.event3);

        writer2.write(LogEventFixture1.event4);
        writer2.write(LogEventFixture1.event5);
        writer2.write(LogEventFixture1.event6);

        writer3.write(LogEventFixture1.event7);
        writer3.write(LogEventFixture1.event8);
        writer3.write(LogEventFixture1.event9);

        writer1.close();
        writer2.close();
        writer3.close();

        fos1.close();
        fos2.close();
        fos3.close();

        assertThat(folder.listFiles().length, is(3));
        
        SofBlockStreamRotatingReader reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, configuration);

        File[] sortedFileList = RotatingHelper.getSortedFileList(folder, prefix, postfix, false);
        assertThat(sortedFileList.length, is(3));
        assertThat(sortedFileList[0].getName(), is("prefix.19700101.000000.0.postfix"));
        assertThat(sortedFileList[1].getName(), is("prefix.19700101.000000.1.postfix"));
        assertThat(sortedFileList[2].getName(), is("prefix.19700101.010000.0.postfix"));
        
        final Bucket<LogEvent> events = new Bucket<LogEvent>();
        reader.visit(0, 100000, new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                events.add((LogEvent) t);
            }
        }, false);

        assertThat(events.size(), is(9));
        assertThat(events.get(0).getMessage(), is(LogEventFixture1.event1.getMessage()));
        assertThat(events.get(1).getMessage(), is(LogEventFixture1.event2.getMessage()));
        assertThat(events.get(2).getMessage(), is(LogEventFixture1.event3.getMessage()));
        
        assertThat(events.get(3).getMessage(), is(LogEventFixture1.event4.getMessage()));
        assertThat(events.get(4).getMessage(), is(LogEventFixture1.event5.getMessage()));
        assertThat(events.get(5).getMessage(), is(LogEventFixture1.event6.getMessage()));
        
        assertThat(events.get(6).getMessage(), is(LogEventFixture1.event7.getMessage()));
        assertThat(events.get(7).getMessage(), is(LogEventFixture1.event8.getMessage()));
        assertThat(events.get(8).getMessage(), is(LogEventFixture1.event9.getMessage()));
    }

}
