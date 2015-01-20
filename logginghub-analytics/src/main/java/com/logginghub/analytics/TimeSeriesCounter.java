package com.logginghub.analytics;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataPoint;
import com.logginghub.utils.IntegerFrequencyCount;

/**
 * Counts the frequencies of name elements in the TimeData. Doesn't do any time based aggregation.
 * 
 * @author James
 * 
 */
public class TimeSeriesCounter {

    public IntegerFrequencyCount count(String seriesName, TimeSeriesData data, int nameIndex) {
       
        IntegerFrequencyCount counter = new IntegerFrequencyCount();
        
        int size = data.size();
        for (int i = 0; i < size; i++) {
            TimeSeriesDataPoint timeSeriesDataPoint = data.get(i);

            String string = timeSeriesDataPoint.getKeys()[nameIndex];
            counter.count(string, 1);
        }

        return counter;
    }

}
