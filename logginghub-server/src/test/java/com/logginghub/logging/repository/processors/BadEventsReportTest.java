package com.logginghub.logging.repository.processors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.repository.processors.BadEventsReport;
import com.logginghub.logging.repository.processors.ProcessorTester;
import com.logginghub.utils.FileUtils;

public class BadEventsReportTest {

    private File results;

    @Before public void setup() {
        results = new File("target/test/results/");
        FileUtils.deleteFolderAndContents(results);
        results.mkdirs();
    }

    @Test public void test() {

        BadEventsReport processor = new BadEventsReport();

        ProcessorTester.process(results, processor, "/testevents/series1.csv");

        File result = new File(results, "badevents.html");
        assertThat(result.exists(), is(true));
        assertThat(result.length(), is(greaterThan(0L)));

    }
    
    @Ignore
    @Test public void testRollup() throws IOException {
        
        BadEventsReport processor = new BadEventsReport();
        processor.setName("rollup-badevents");
        
        List<String> regexs = new ArrayList<String>();
        regexs.add("Connection to .*? lost, retrying...");
        processor.setRollupRegexs(regexs);
        
        ProcessorTester.process(results, processor, "/testevents/series1.csv");

        File result = new File(results, "rollup-badevents.html");
        assertThat(result.exists(), is(true));
        assertThat(result.length(), is(greaterThan(0L)));
        
        String read = FileUtils.read(result);
        assertThat(read, containsString("Connection to .*? lost, retrying..."));
        assertThat(read, containsString("<td nowrap='nowrap'>5</td>"));
        
    }
}