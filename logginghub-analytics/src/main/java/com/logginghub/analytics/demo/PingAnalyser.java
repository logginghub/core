package com.logginghub.analytics.demo;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.AggregatedDataPlotter;
import com.logginghub.analytics.TimeSeriesAggregator;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.utils.AggregatedDataSplitter;

public class PingAnalyser {

    public static void main(String[] args) {

        // Run the extract and pull out the save data into TimeSeriesData object
        // That'll capture the time|user,type,protocol|elapsed,compressed size, uncompressed size
        // Save that file
        // Run the aggregator - against the compressed size
        
        TimeSeriesData fromFile = TimeSeriesData.fromFile(new File("demo/www.cuhk.edu.hk.data"));

        TimeSeriesAggregator aggregator = new TimeSeriesAggregator();
        AggregatedData aggregated = aggregator.aggregate("Ping data", fromFile, 10000, 0);
        aggregated.dump();
        
        AggregatedDataSplitter splitter = new AggregatedDataSplitter();
        List<AggregatedData> splitData = splitter.split(aggregated, 10, TimeUnit.MINUTES);
        System.out.println(splitData);

        aggregated.dump(AggregatedDataKey.Percentile10);
        
        File folder = new File("output/");
        folder.mkdirs();
        
        AggregatedDataPlotter plotter = new AggregatedDataPlotter();        
        for (AggregatedData aggregatedData : splitData) {
            plotter.plotDifferences(folder, "Ping time-" + aggregatedData.getStartTime(), aggregatedData, AggregatedDataKey.High, AggregatedDataKey.Low, AggregatedDataKey.Mean);    
        } 
        
//        plotter.plot(aggregated, AggregatedDataKey.Mean, AggregatedDataKey.High, AggregatedDataKey.Low);
        
        

//        plotter.plot(aggregated,
//                     AggregatedDataKey.Mean,
//                     AggregatedDataKey.Percentile10,
//                     AggregatedDataKey.Percentile20,
//                     AggregatedDataKey.Percentile50,
//                     AggregatedDataKey.Percentile95,
//                     AggregatedDataKey.Percentile99);
        
        // TODO : save images to disk
        // TODO : maybe with some html to back them up

    }

}

