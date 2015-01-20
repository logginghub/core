package com.logginghub.analytics;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.analytics.model.GeneralAggregatedDataPoint;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

/**
 * Similar to the {@link TimeSeriesAggregator} class, but works on raw double lists.
 * 
 * @author James
 * 
 */
public class GeneralAggregator {

    private Log log = Log.create(this);

    public GeneralAggregatedData aggregate(String seriesKey, List<Double> data, double aggregationPeriod, boolean padBackToZero) {

        GeneralAggregatedData aggregatedData = new GeneralAggregatedData(seriesKey);

        log.debug("Sorted %d elements", data.size());
        if (!data.isEmpty()) {

            double firstResult = data.get(0);
            int periodsAtTheStart = (int) Math.floor(firstResult / aggregationPeriod);
            if (padBackToZero) {
                for (int i = 0; i < periodsAtTheStart; i++) {
                    double start = i * aggregationPeriod;
                    double end = (i + 1) * aggregationPeriod;
                    aggregatedData.add(new GeneralAggregatedDataPoint(start, end));
                }
            }

            Iterator<Double> iterator = data.iterator();
            double lastValue = data.get(data.size() - 1).doubleValue();
            double firstRealBucketStart = periodsAtTheStart * aggregationPeriod;
            double currentBucketStart = firstRealBucketStart;
            double currentBucketEnd = firstRealBucketStart + aggregationPeriod;
            GeneralAggregatedDataPoint currentPoint = new GeneralAggregatedDataPoint(currentBucketStart, currentBucketEnd);

            while (iterator.hasNext()) {

                double value = iterator.next();
                if (value >= currentBucketStart && value < currentBucketEnd) {
                    currentPoint.addValue(value);
                }
                else {
                    // Store the current point
                    aggregatedData.add(currentPoint);

                    // Any gaps to fill?
                    int gaps = (int) ((value - currentBucketEnd) / aggregationPeriod);
                    for (int i = 0; i < gaps; i++) {
                        currentBucketStart += aggregationPeriod;
                        currentBucketEnd += aggregationPeriod;

                        aggregatedData.add(new GeneralAggregatedDataPoint(currentBucketStart, currentBucketEnd));
                    }

                    // Moving on to a new point
                    currentBucketStart += aggregationPeriod;
                    currentBucketEnd += aggregationPeriod;

                    currentPoint = new GeneralAggregatedDataPoint(currentBucketStart, currentBucketEnd);
                    currentPoint.addValue(value);
                }
            }

            // Add the last bucket
            if (!currentPoint.isEmpty()) {
                aggregatedData.add(currentPoint);
            }

            // Turn the stats into values - not sure why we do it this way for
            // General data, the time series stuff captures the values as
            // SinglePassStats first...
            for (GeneralAggregatedDataPoint generalAggregatedDataPoint : aggregatedData) {
                generalAggregatedDataPoint.captureStats();
            }
        }

        return aggregatedData;
    }

}
