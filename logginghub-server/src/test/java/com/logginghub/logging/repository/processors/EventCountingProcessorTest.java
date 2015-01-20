package com.logginghub.logging.repository.processors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataContainer;
import com.logginghub.analytics.utils.TimeBucketCounter;
import com.logginghub.logging.repository.processors.EventCountingProcessor;
import com.logginghub.logging.repository.processors.ProcessorTester;

@Ignore // swing!
public class EventCountingProcessorTest  {

    @Test public void test(){
        
        EventCountingProcessor processor = new EventCountingProcessor();
        
        File results = new File("tmp/testresults/");
        results.mkdirs();
        
        ProcessorTester.process(results, processor, "/testevents/series1.csv");
        
        TimeBucketCounter counter = processor.getCounter();
        TimeSeriesDataContainer allSeries = counter.extractAllSeries();
        assertThat(allSeries.size(), is(7));
        
        TimeSeriesData infoSeries = allSeries.getSeries("INFO");
        assertThat(infoSeries, is(not(nullValue())));
        assertThat(infoSeries.getSize(), is(4));
        assertThat(infoSeries.get(0).getValues()[0], is(1d));
        assertThat(infoSeries.get(1).getValues()[0], is(2d));
        assertThat(infoSeries.get(2).getValues()[0], is(1d));
        assertThat(infoSeries.get(3).getValues()[0], is(1d));
    }
    
}
