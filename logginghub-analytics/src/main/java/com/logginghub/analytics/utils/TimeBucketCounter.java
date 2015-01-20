package com.logginghub.analytics.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataContainer;
import com.logginghub.utils.FactoryMap;

/**
 * Counts events up into buckets, based on a bucket duration. Events are keyed
 * by a string, and the counter is an integer.
 * 
 * Note this method is NOT THREAD SAFE. You have to worry about that if you plan
 * to interact with it from multiple threads.
 * 
 * @author James
 * 
 */
public class TimeBucketCounter {

    @SuppressWarnings("serial") private FactoryMap<String, FactoryMap<Long, AtomicInteger>> data = new FactoryMap<String, FactoryMap<Long, AtomicInteger>>() {
        @Override protected FactoryMap<Long, AtomicInteger> createEmptyValue(String key) {
            return new FactoryMap<Long, AtomicInteger>() {
                @Override protected AtomicInteger createEmptyValue(Long key) {
                    return new AtomicInteger(0);
                }

            };
        }
    };
    private final long bucketDuration;

    public TimeBucketCounter(long bucketDuration) {
        this.bucketDuration = bucketDuration;
    }

    public void count(long time, String seriesKey, int increment) {
        data.get(seriesKey).get(chunk(time)).getAndAdd(increment);
    }

    private long chunk(long time) {
        return time - time % bucketDuration;

    }

    public TimeSeriesDataContainer extractAllSeries() {
        TimeSeriesDataContainer container = new TimeSeriesDataContainer();
        Set<String> keySet = data.keySet();
        for (String seriesName : keySet) {
            
            TimeSeriesData timeSeriesData = new TimeSeriesData();
            Map<Long, AtomicInteger> timeData = data.get(seriesName);
            Set<Long> timeDataPoints = timeData.keySet();
            for (Long time : timeDataPoints) {
                timeSeriesData.add(time, "value", timeData.get(time).intValue());
            }
            
            timeSeriesData.sort();
            container.add(seriesName, timeSeriesData);
        }
        return container;
    }

    public TimeSeriesData extractTotalsSeries() {
        
        // Create a set containing all of the unique time points
        Set<Long> times = new HashSet<Long>();
        
        Set<String> keySet = data.keySet();
        for (String seriesName : keySet) {            
            Map<Long, AtomicInteger> timeData = data.get(seriesName);            
            times.addAll(timeData.keySet());
        }
        
        // Now iterate through each time totalling up the values across all series
        TimeSeriesData timeSeriesData = new TimeSeriesData();
        for (Long time : times) {
            int sumAtTime = 0;
            for (String seriesName : keySet) {
                                
                FactoryMap<Long, AtomicInteger> timeData = data.get(seriesName);
                AtomicInteger atomicInteger = timeData.getOnlyIfExists(time);
                if(atomicInteger != null){
                    sumAtTime += atomicInteger.get();
                }
            }
            timeSeriesData.add(time, "total", sumAtTime);
        }

        return timeSeriesData;
    }
}
