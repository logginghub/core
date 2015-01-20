package com.logginghub.logging.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.repository.SofBlockPointer;
import com.logginghub.logging.repository.SofBlockStreamReader;
import com.logginghub.logging.repository.SofBlockStreamRotatingWriter;
import com.logginghub.logging.repository.SofBlockStreamRotatingWriter.RotationTrigger;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.logging.modules.LogEventFixture1;

public class TestSofBlockStreamRotatingWriter {

    // TODO : post wedding ignore, needs fixing
    @Ignore
    @Test public void testSofBlockStreamRotator() throws Exception {

        File folder = FileUtils.createRandomTestFolderForClass(TestSofBlockStreamRotatingWriter.class);
        System.out.println(folder.getAbsolutePath());
        
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);
        
        SofBlockStreamRotatingWriter rotator = new SofBlockStreamRotatingWriter(folder, "hub.", ".binary", configuration);
        rotator.setTrigger(RotationTrigger.Size);
        rotator.setBlocksize(400);
        rotator.setRotationSize(800);
        
        rotator.send(LogEventFixture1.event1);
        rotator.send(LogEventFixture1.event2);
        rotator.send(LogEventFixture1.event3);
        rotator.send(LogEventFixture1.event4);
        rotator.send(LogEventFixture1.event5);
        rotator.send(LogEventFixture1.event6);
        rotator.send(LogEventFixture1.event7);
        rotator.send(LogEventFixture1.event8);
        rotator.send(LogEventFixture1.event9);

        rotator.close();

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(2));
        
        SofBlockStreamReader reader = new SofBlockStreamReader(configuration);
        
        List<SofBlockPointer> loadPointers1 = reader.loadPointers(listFiles[0]);
        List<SofBlockPointer> loadPointers2 = reader.loadPointers(listFiles[1]);
        
        assertThat(loadPointers1.size(), is(4));
        assertThat(loadPointers2.size(), is(1));
        
        
    }

}
