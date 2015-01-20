package com.logginghub.analytics.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.logginghub.analytics.Log;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.analytics.model.MultiSeriesAggreatedData;

public class AggregatedDataSplitter {

    private Log log = Log.create(this);

    /**
     * Splits the aggregated data into a series of smaller instances containing
     * a regular sized subset of the data
     * 
     * @param aggregated
     * @param amount
     * @param units
     * @return
     */
    public List<AggregatedData> split(AggregatedData aggregated, int amount, TimeUnit units) {

        long chunkDuration = units.toMillis(amount);

        List<AggregatedData> split = new ArrayList<AggregatedData>();

        long start = aggregated.get(0).getStartTime();

        long firstChunkStart = start - (start % chunkDuration);
        long firstChunkEnd = firstChunkStart + chunkDuration;

        long end = aggregated.get(aggregated.size() - 1).getStartTime();

        long endChunkStart = end - (end % chunkDuration);
        long endChunkEnd = endChunkStart + chunkDuration;

        long currentChunkEnd = firstChunkEnd;

        AggregatedData currentChunk = new AggregatedData(aggregated.getSeriesName(), aggregated.getLegend());

        for (AggregatedDataPoint aggregatedDataPoint : aggregated) {

            long pointStart = aggregatedDataPoint.getStartTime();
            log.debug("Checking point starting %s", new Date(pointStart));

            long chunkedPointStart = pointStart - (pointStart % chunkDuration);
            if (chunkedPointStart < currentChunkEnd) {
                log.debug("Within the current chunk (ending %s)", new Date(currentChunkEnd));
                currentChunk.add(aggregatedDataPoint);
            }
            else {
                split.add(currentChunk);
                currentChunk = new AggregatedData(aggregated.getSeriesName(),aggregated.getLegend());
                currentChunkEnd += chunkDuration;
                log.debug("Outside of the current chunk, moving to next chunk (ending %s)", new Date(currentChunkEnd));
            }
        }

        return split;

    }

    public List<MultiSeriesAggreatedData> split(MultiSeriesAggreatedData aggregated, int amount, TimeUnit units) {

        // Need to return a list of the data but split into time chucks

        List<MultiSeriesAggreatedData> dataList = new ArrayList<MultiSeriesAggreatedData>();

        long start = aggregated.getStartTime();
        long end = aggregated.getEndTime();

        log.info("Splitting data between dates [%s] and [%s]", new Date(start), new Date(end));

        long chunkDuration = units.toMillis(amount);

        long firstChunkStart = start - (start % chunkDuration);
        long endChunkStart = end - (end % chunkDuration);

        // We might have got luck and have the end value sit right on the chunk
        // limit, but if not we need to push the end bucket out so to catch the
        // tail end items
        if (endChunkStart < end) {
            endChunkStart += chunkDuration;
        }
        
        long wholePeriodLength = endChunkStart - firstChunkStart;

        // Populate the list with the chunks in advance, so we can populate them
        // as we iterate through the data
        int chunks = (int) ((endChunkStart - firstChunkStart) / chunkDuration);
        for (int i = 0; i < chunks; i++) {
            long chunkStart = firstChunkStart + (i * chunkDuration);
            dataList.add(new MultiSeriesAggreatedData(chunkStart, chunkStart + chunkDuration, aggregated.getValueLegend(), aggregated.getKeysLegend()));
        }

        log.info("Splitting the data into %d chunks of %.2f minutes each", chunks, chunkDuration / 1000f / 60f);

        // Time to go trawling through the series data
        Map<String, AggregatedData> data = aggregated.getMappedData();
        Set<String> keySet = data.keySet();
        for (String key : keySet) {
            AggregatedData aggregatedData = data.get(key);

            for (AggregatedDataPoint aggregatedDataPoint : aggregatedData) {
                long startTime = aggregatedDataPoint.getStartTime();
                long howFarThroughIsThis = startTime - firstChunkStart;
                double factor = howFarThroughIsThis / (double) wholePeriodLength;
                int bucket = (int) (chunks * factor);

                log.debug("Series [%s] data point at [%s] going into bucket number %d", key, new Date(startTime), bucket);
                dataList.get(bucket).add(key, aggregatedData.getLegend(), aggregatedDataPoint);
            }

            log.info("Series [%s] split", key);
        }

        int chunk = 0;
        for (MultiSeriesAggreatedData aggreatedData : dataList) {
            log.info("Chunk [%3d] : from [%s] to [%s]", chunk++, new Date(aggreatedData.getStartTime()), new Date(aggreatedData.getEndTime()));
//            aggreatedData.dump();
        }
        
        return dataList;
    }

}
