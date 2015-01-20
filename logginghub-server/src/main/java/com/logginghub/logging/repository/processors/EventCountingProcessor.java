package com.logginghub.logging.repository.processors;

import java.io.File;

import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataContainer;
import com.logginghub.analytics.utils.TimeBucketCounter;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.LogDataProcessor;

public class EventCountingProcessor implements LogDataProcessor {

    private File resultsFolder;
    private TimeBucketCounter counter;

    public EventCountingProcessor(){
        this(1000);
    }
    
    public EventCountingProcessor(long bucketDuration){
        counter =  new TimeBucketCounter(bucketDuration);
    }
    
    public void onNewLogEvent(LogEvent event) {
        counter.count(event.getOriginTime(), event.getLevelDescription(), 1);
    }

    public void processingStarted(File resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public void processingEnded() {
        TimeSeriesDataContainer series = counter.extractAllSeries();
        TimeSeriesData totals = counter.extractTotalsSeries();

        ChartBuilder.startXY().setTitle("Log event counts by level").addSeries(series, 0).addSeries("Total", totals,0).toPng(new File(resultsFolder, "eventCount.png"));
    }
    
    public TimeBucketCounter getCounter() {
        return counter;
    }

}
