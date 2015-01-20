package com.logginghub.logging.modules;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.logging.Logger;

public class OpenAggregationInterval {

    private static final Logger logger = Logger.getLoggerFor(OpenAggregationInterval.class);
    private String seriesKey;
    private Aggregation aggregation;
    private long intervalStart;
    private long intervalLength;
    private SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();
    private AggregationType aggregationType;
    private double lastValue;
    private int totalCount;
    private double totalSum;

    public OpenAggregationInterval(String seriesKey, Aggregation aggregationKey, long intervalStart, long intervalLength, AggregationType aggregationType) {
        super();
        this.seriesKey = seriesKey;
        this.aggregation = aggregationKey;
        this.intervalStart = intervalStart;
        this.intervalLength = intervalLength;
        this.aggregationType = aggregationType;
    }

    public void setAggregationType(AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    public AggregationType getAggregationType() {
        return aggregationType;
    }

    public void setAggregation(Aggregation aggregationKey) {
        this.aggregation = aggregationKey;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public String getSeriesKey() {
        return seriesKey;
    }

    public void setSeriesKey(String seriesKey) {
        this.seriesKey = seriesKey;
    }

    public long getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(long intervalStart) {
        this.intervalStart = intervalStart;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(long intervalLength) {
        this.intervalLength = intervalLength;
    }

    public SinglePassStatisticsDoublePrecision getStats() {
        return stats;
    }

    public void setStats(SinglePassStatisticsDoublePrecision stats) {
        this.stats = stats;
    }

    public void update(double value) {
        stats.addValue(value);
        lastValue = value;
        totalSum += value;
        totalCount++;
    }

    public double getValue() {

        double value;
        stats.doCalculations();

        switch (aggregationType) {
            case Count:
                value = stats.getCount();
                break;
            case Mean:
                value = stats.getMean();
                break;
            case Median:
                value = stats.getMedian();
                break;
            case Mode:                
                value = stats.getMode();
                break;
            case Percentile90:
                value = stats.getPercentiles()[90];
                break;
            case StandardDeviation:
                value = stats.getStandardDeviationSampleDistribution();
                break;
            case LastValue:
                value = lastValue;
                break;
            case Sum:
                value = stats.getSum();
                break;
            case TotalCount:
                value = totalCount;
                break;
            case TotalSum:
                value = totalSum;
                break;
            default:
                logger.warn("Unsupported aggregation type '{}' is not supported for interval statistics '{}'", aggregationType, this);
                value = -1;
                break;
        }

        return value;

    }

    @Override public String toString() {
        return "OpenInterval [aggregationType=" +
               aggregationType +
               ", aggregation=" +
               aggregation +
               ", seriesKey=" +
               seriesKey +
               ", intervalStart=" +
               intervalStart +
               ", intervalLength=" +
               intervalLength +
               "]";
    }

}
