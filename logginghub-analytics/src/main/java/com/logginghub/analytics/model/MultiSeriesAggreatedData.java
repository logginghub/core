package com.logginghub.analytics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.Log;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.CompareUtils.CompareBuilder;

/**
 * Encapsulates the {@link AggregatedData} for a list of different series.
 * Provides aggregate stats methods which operate on the full set of series.
 * 
 * @author James
 * 
 */
public class MultiSeriesAggreatedData {

    private List<AggregatedData> orderedData;
    private Map<String, AggregatedData> data;

    private long startTime = Long.MAX_VALUE;
    private long endTime = Long.MIN_VALUE;
    private Log log = Log.create(this);
    private String valueLegend;
    private String keysLegend;

    public MultiSeriesAggreatedData(Map<String, AggregatedData> data, String valueLegend) {

        this.valueLegend = valueLegend;
        setData(data);
    }

    /**
     * Create a new MultiSeriesAggreatedData instance for a time period.
     * 
     * @param start
     * @param end
     * @param keysLegend 
     */
    public MultiSeriesAggreatedData(long start, long end, String valueLegend, String keysLegend) {
        this.valueLegend = valueLegend;
        this.keysLegend = keysLegend;
        data = new HashMap<String, AggregatedData>();
        orderedData = new ArrayList<AggregatedData>();
        this.startTime = start;
        this.endTime = end;
    }

    public MultiSeriesAggreatedData(String valueLegend, String keysLegend, AggregatedData... aggregatedData) {
        this.valueLegend = valueLegend;
        this.keysLegend = keysLegend;
        Map<String, AggregatedData> data = new HashMap<String, AggregatedData>();
        for (AggregatedData aggregatedDataItem : aggregatedData) {
            data.put(aggregatedDataItem.getSeriesName(), aggregatedDataItem);
        }
        setData(data);
    }

    private void setData(Map<String, AggregatedData> data) {
        Collection<AggregatedData> values = data.values();
        for (AggregatedData aggregatedData : values) {
            startTime = Math.min(startTime, aggregatedData.getStartTime());
            endTime = Math.max(endTime, aggregatedData.getEndTime());
        }

        this.data = data;
        orderedData = new ArrayList<AggregatedData>();
        orderedData.addAll(data.values());
    }

    public MultiSeriesAggreatedData top(int topX, boolean createOthers, final AggregatedDataKey... keys) {

        MultiSeriesAggreatedData newData = new MultiSeriesAggreatedData(startTime, endTime, valueLegend, keysLegend);

        sortDescending(keys);

        AggregatedData others = new AggregatedData("Other", "Other");

        int counter = 0;
        for (AggregatedData aggregatedData : orderedData) {

            if (counter < topX) {
                newData.add(aggregatedData);
            }
            else if (createOthers) {
                others.merge(aggregatedData);
            }

            counter++;
        }

        if (createOthers) {
            newData.add(others);
        }

        return newData;
    }

    public void sortDescending(final AggregatedDataKey... keys) {
        // TESTME
        Collections.sort(orderedData, new Comparator<AggregatedData>() {
            public int compare(AggregatedData o1, AggregatedData o2) {
                CompareBuilder builder = CompareUtils.start();
                for (AggregatedDataKey key : keys) {
                    builder.add(o2.getValue(key), o1.getValue(key));
                }
                return builder.compare();
            }
        });
        // TESTME
    }

    public void sortAscending(final AggregatedDataKey... keys) {
        // TESTME
        Collections.sort(orderedData, new Comparator<AggregatedData>() {
            public int compare(AggregatedData o1, AggregatedData o2) {
                CompareBuilder builder = CompareUtils.start();
                for (AggregatedDataKey key : keys) {
                    builder.add(o1.getValue(key), o2.getValue(key));
                }
                return builder.compare();
            }
        });
        // TESTME
    }

    public double getOverallValue(AggregatedDataKey key) {

        double value;

        switch (key) {
            case Mean: {
                double total = 0;
                int count = 0;
                for (AggregatedData aggregatedData : orderedData) {
                    total += aggregatedData.getValue(AggregatedDataKey.Sum);
                    count += aggregatedData.getValue(AggregatedDataKey.Count);
                }
                value = total / count;
                break;
            }
            case Sum: {
                double total = 0;
                for (AggregatedData aggregatedData : orderedData) {
                    total += aggregatedData.getValue(AggregatedDataKey.Sum);
                }
                value = total;
                break;
            }
            default:
                throw new RuntimeException("Key '" + key.name() + "' isn't supported");
        }

        return value;

    }

    public long getEndTime() {
        return endTime;
    }

    public Map<String, AggregatedData> getMappedData() {
        return data;
    }

    public List<AggregatedData> getOrderedData() {
        return orderedData;
    }

    public long getStartTime() {
        return startTime;

    }

    private void add(AggregatedData aggregatedData) {

        data.put(aggregatedData.getSeriesName(), aggregatedData);
        orderedData.add(aggregatedData);

        startTime = Math.min(startTime, aggregatedData.getStartTime());
        endTime = Math.max(endTime, aggregatedData.getEndTime());
    }

    public void add(String seriesName, String seriesLegend, AggregatedDataPoint aggregatedDataPoint) {

        AggregatedData aggregatedData = data.get(seriesName);
        if (aggregatedData == null) {
            aggregatedData = new AggregatedData(seriesName, seriesLegend);
            data.put(seriesName, aggregatedData);
            orderedData.add(aggregatedData);
        }
        aggregatedData.add(aggregatedDataPoint);
        startTime = Math.min(startTime, aggregatedDataPoint.getStartTime());
        endTime = Math.max(endTime, aggregatedDataPoint.getEndTime());
    }

    public Set<String> getSeriesNames() {
        return data.keySet();
    }

    public AggregatedData getSeries(String string) {
        return data.get(string);

    }

    public void dump() {
        log.info("Multiseries data for [%s] from [%s] to [%s]", getValueLegend(), new Date(getStartTime()), new Date(getEndTime()));
        for (AggregatedData seriesData : orderedData) {
            log.info("Aggregated data for series [%s]", seriesData.getSeriesName());
            seriesData.dump();
        }
    }

    public void setLegend(String valueLegend) {
        this.valueLegend = valueLegend;
    }

    public String getValueLegend() {
        return valueLegend;
    }

    public String getKeysLegend() {
        return keysLegend;
    }

    public void append(MultiSeriesAggreatedData other) {

        // Reset the min/max
        startTime = Long.MAX_VALUE;
        endTime = Long.MIN_VALUE;

        Set<String> combinedSeriesNames = new HashSet<String>();

        combinedSeriesNames.addAll(this.getSeriesNames());
        combinedSeriesNames.addAll(other.getSeriesNames());

        for (String seriesName : combinedSeriesNames) {

            AggregatedData thisSeries = this.getSeries(seriesName);
            AggregatedData otherSeries = other.getSeries(seriesName);

            if (thisSeries == null) {
                add(otherSeries);
            }
            else if (otherSeries == null) {
                // Nothing to merge
            }
            else {
                thisSeries.merge(otherSeries);

                // See if we need to change the times
                startTime = Math.min(startTime, thisSeries.getStartTime());
                endTime = Math.max(endTime, thisSeries.getEndTime());
            }
        }
    }
}
