package com.logginghub.analytics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.analytics.model.MultiSeriesAggreatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataPoint;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

/**
 * Aggregates TimeSeriesData based on their capture time, and produces a list of
 * AggregatedData as a result.
 * 
 * @author James
 * 
 */
public class TimeSeriesAggregator {

    private Log log = Log.create(this);

    public AggregatedData aggregate(TimeSeriesData data, long aggregationPeriod, int valueIndex) {
        return aggregate(data.getSeriesName(), data, aggregationPeriod, valueIndex);
    }
    
    public AggregatedData aggregate(TimeSeriesData data, long aggregationPeriod) {
        return aggregate(data.getSeriesName(), data, aggregationPeriod, 0);
    }
    
    public AggregatedData aggregate(String seriesName, TimeSeriesData data, long aggregationPeriod, int valueIndex) {

        // james : not sure about this next line, it sounds useful-ish, but it
        // should be possible to specify it - thats why we are passing it in?!
        // If we need to alter it with the value label and aggregation period, that should be a display issue? 
        
        // String seriesName = seriesName + " (" +
        // data.getValuesLegend()[valueIndex] + " [" + aggregationPeriod + "])";
        AggregatedData aggregatedData = new AggregatedData(seriesName, data.getLegend());
        data.sort();

        log.debug("Sorted %d elements", data.getSize());

        long currentBucketStart = -1;
        SinglePassStatisticsDoublePrecision currentStats = new SinglePassStatisticsDoublePrecision();

        int size = data.size();
        for (int i = 0; i < size; i++) {
            TimeSeriesDataPoint timeSeriesDataPoint = data.get(i);

            long time = timeSeriesDataPoint.getTime();
            long aggregatedTime = time - (time % aggregationPeriod);

            long currentBucketEnd = currentBucketStart + aggregationPeriod;

            log.debug("Current bucket start : %s, Current bucket end %s : next item time %s (aggregated to %s)",
                      currentBucketStart,
                      currentBucketEnd,
                      time,
                      aggregatedTime);

            if (currentBucketStart == -1) {
                // First bucket
                currentBucketStart = aggregatedTime;
                log.debug("Creating first bucket");
            }
            else if (aggregatedTime >= currentBucketEnd) {
                // Need a new bucket
                log.debug("Bucket ended");

                // Capture the last bucket
                AggregatedDataPoint currentPoint = new AggregatedDataPoint(currentBucketStart, currentBucketEnd, currentStats);
                aggregatedData.add(currentPoint);

                // Fill in any gaps
                long timeDelta = aggregatedTime - currentBucketStart;
                int numberOfPeriods = (int) (timeDelta / aggregationPeriod);
                int gaps = numberOfPeriods - 1;
                log.debug("Gaps needed %d", gaps);
                for (int gap = 1; gap <= gaps; gap++) {
                    long gapStartTime = currentBucketStart + (aggregationPeriod * gap);
                    log.debug("Created gap starting %d", gapStartTime);
                    AggregatedDataPoint gapPoint = new AggregatedDataPoint(gapStartTime, gapStartTime + aggregationPeriod);
                    aggregatedData.add(gapPoint);
                }

                // Create the next bucket
                currentBucketStart = aggregatedTime;
                currentStats.clear();
            }

            double value = timeSeriesDataPoint.getValues()[valueIndex];
            currentStats.addValue(value);
        }

        // Add the last bucket
        if (!currentStats.isEmpty()) {
            AggregatedDataPoint currentPoint = new AggregatedDataPoint(currentBucketStart,
                                                                       currentBucketStart + aggregationPeriod,
                                                                       currentStats);
            aggregatedData.add(currentPoint);
        }

        return aggregatedData;
    }

    public MultiSeriesAggreatedData aggregate(HashMap<String, TimeSeriesData> groups, int aggregationPeriod, int valueIndex) {

        // TODO: make sure that value index has the same name in all the groups
        String valueLegend = groups.values().iterator().next().getValuesLegend()[valueIndex];

        Map<String, AggregatedData> aggregatedDataMap = new HashMap<String, AggregatedData>();
        Set<String> keySet = groups.keySet();
        for (String seriesKey : keySet) {
            TimeSeriesData timeSeriesData = groups.get(seriesKey);
            AggregatedData aggregate = aggregate(seriesKey, timeSeriesData, aggregationPeriod, valueIndex);

            log.info("Series [%s] : aggregated into %d buckets from [%s] to [%s]",
                     seriesKey,
                     aggregate.size(),
                     new Date(aggregate.getStartTime()),
                     new Date(aggregate.getEndTime()));

            aggregatedDataMap.put(seriesKey, aggregate);
        }

        MultiSeriesAggreatedData data = new MultiSeriesAggreatedData(aggregatedDataMap, valueLegend);
        return data;

    }

}
