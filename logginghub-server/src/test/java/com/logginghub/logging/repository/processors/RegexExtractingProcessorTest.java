package com.logginghub.logging.repository.processors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataPoint;
import com.logginghub.analytics.model.TimeSeriesDataTransformer;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.repository.processors.ProcessorTester;
import com.logginghub.logging.repository.processors.RegexExtractingProcessor;
import com.logginghub.utils.FileUtils;

@Ignore // this fires up swing, it shouldnt...
public class RegexExtractingProcessorTest {

    private File results;

    @Before public void setup(){
        results = new File("target/test/results/");
        FileUtils.deleteFolderAndContents(results);
        results.mkdirs();
    }
    
    @Test public void testOverride(){
        
        RegexExtractingProcessor processor = new RegexExtractingProcessor("regex", 1000) {
            @Override public void processingEnded() {
                super.processingEnded();
                
                TimeSeriesData data = getData();
                TimeSeriesData transformed = data.transform(new TimeSeriesDataTransformer() {                   
                    public TimeSeriesDataPoint transform(TimeSeriesDataPoint original) {
                        String resouce = original.getKeys()[0];
                        String[] split = resouce.split("/");                        
                        return new TimeSeriesDataPoint(original.getTime(), new String[] { split[0] }, original.getValues());
                    }
                });                
                
                processTimeSeriesData("firstPathElement", transformed);
            }
        };
        
        processor.setSimpleExpression("Operation 'get' complete in {elapsed} ms, resource request was '[request]' and returned data size was {size} bytes");
        
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage("Operation 'get' complete in 34 ms, resource request was 'some/file/here.jpg' and returned data size was 1234 bytes");
        
        processor.processingStarted(results);
        processor.onNewLogEvent(event);
        processor.processingEnded();
        
    }
    
    
    @Test public void testOnAccessLog() {
        RegexExtractingProcessor processor = new RegexExtractingProcessor("accesslog");

        processor.setSimpleExpression("[sourceIP] - - [time] \"[request]\" [result] {responseSize} \"-\" \"[agent]\"");
        processor.setAllowNumericParseFailures(true);

        ProcessorTester.processFromAccessLog(results, processor, "access.log");

        TimeSeriesData data = processor.getData();
    }

    @Test public void testSingleEntry() {

        RegexExtractingProcessor processor = new RegexExtractingProcessor("single");

        processor.setSimpleExpression("User '[username]' successfully authenticated in {elapsed} ms");

        ProcessorTester.process(results, processor, "/testevents/series1.csv");

        TimeSeriesData data = processor.getData();

        assertThat(data.size(), is(5));
        assertThat(data.get(0).getKeys(), is(new String[] { "user1" }));
        assertThat(data.get(1).getKeys(), is(new String[] { "user2" }));
        assertThat(data.get(2).getKeys(), is(new String[] { "user2" }));
        assertThat(data.get(3).getKeys(), is(new String[] { "user3" }));
        assertThat(data.get(4).getKeys(), is(new String[] { "user4" }));

        assertThat(data.get(0).getValues(), is(new double[] { 34 }));
        assertThat(data.get(1).getValues(), is(new double[] { 19 }));
        assertThat(data.get(2).getValues(), is(new double[] { 241 }));
        assertThat(data.get(3).getValues(), is(new double[] { 2512 }));
        assertThat(data.get(4).getValues(), is(new double[] { 2.123 }));

    }

    @Test public void testTwoEntries() throws IOException {

        RegexExtractingProcessor processor = new RegexExtractingProcessor("two");

        processor.setSimpleExpression("Validation for '[username]' complete in {elapsed} ms, token was {tokenSize} bytes long");

        ProcessorTester.process(results, processor, "/testevents/series1.csv");

        TimeSeriesData data = processor.getData();

        assertThat(data.size(), is(5));
        assertThat(data.get(0).getKeys(), is(new String[] { "user1" }));
        assertThat(data.get(1).getKeys(), is(new String[] { "user2" }));
        assertThat(data.get(2).getKeys(), is(new String[] { "user2" }));
        assertThat(data.get(3).getKeys(), is(new String[] { "user3" }));
        assertThat(data.get(4).getKeys(), is(new String[] { "user4" }));

        assertThat(data.get(0).getValues(), is(new double[] { 28, 345 }));
        assertThat(data.get(1).getValues(), is(new double[] { 15, 1234 }));
        assertThat(data.get(2).getValues(), is(new double[] { 234, 24 }));
        assertThat(data.get(3).getValues(), is(new double[] { 2241, 100 }));
        assertThat(data.get(4).getValues(), is(new double[] { 0.143, 1 }));

        assertThat(new File(results, "two.elapsed.mean.csv").exists(), is(true));
        assertThat(new File(results, "two.elapsed.mean.png").exists(), is(true));
        assertThat(new File(results, "two.elapsed.count.csv").exists(), is(true));
        assertThat(new File(results, "two.elapsed.count.png").exists(), is(true));
        assertThat(new File(results, "two.tokenSize.mean.csv").exists(), is(true));
        assertThat(new File(results, "two.tokenSize.count.csv").exists(), is(true));

        assertThat(new File(results, "two.tokenSize.count.csv").exists(), is(true));
        File raw = new File(results, "two.raw.csv");
        assertThat(raw.exists(), is(true));
        
        CSVReader csvReader = new CSVReader(new FileReader(raw));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        assertThat(lines.size(), is(5));
        assertThat(lines.get(0)[1], is("user1"));
        assertThat(lines.get(1)[1], is("user2"));
        assertThat(lines.get(2)[1], is("user2"));
        assertThat(lines.get(3)[1], is("user3"));
        assertThat(lines.get(4)[1], is("user4"));
        
        assertThat(lines.get(0)[2], is("28.0"));
        assertThat(lines.get(1)[2], is("15.0"));
        assertThat(lines.get(2)[2], is("234.0"));
        assertThat(lines.get(3)[2], is("2241.0"));
        assertThat(lines.get(4)[2], is("0.143"));
        
        assertThat(lines.get(0)[3], is("345.0"));
        assertThat(lines.get(1)[3], is("1234.0"));
        assertThat(lines.get(2)[3], is("24.0"));
        assertThat(lines.get(3)[3], is("100.0"));
        assertThat(lines.get(4)[3], is("1.0"));
    }
    
    @Test public void testNonGreedyRegex(){
    
        RegexExtractingProcessor processor = new RegexExtractingProcessor("two");
        processor.setSimpleExpression("This is something with a path in it '[first]/[second]/[third]/[rest]' that falls foul of greedy matchers");
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage("This is something with a path in it 'a/b/c/d/e/f' that falls foul of greedy matchers");
        processor.onNewLogEvent(event);
           
        TimeSeriesData data = processor.getData();
        assertThat(data.size(), is(1));
        TimeSeriesDataPoint timeSeriesDataPoint = data.get(0);
        assertThat(timeSeriesDataPoint.getKeys()[0], is("a"));
        assertThat(timeSeriesDataPoint.getKeys()[1], is("b"));
        assertThat(timeSeriesDataPoint.getKeys()[2], is("c"));
        assertThat(timeSeriesDataPoint.getKeys()[3], is("d/e/f"));
        
    }
}
