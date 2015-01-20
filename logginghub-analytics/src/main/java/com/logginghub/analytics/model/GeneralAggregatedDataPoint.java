package com.logginghub.analytics.model;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

/**
 * Encapsulation of a single point of aggregated general (non-time) series data.
 * 
 * @author James
 * 
 */
public class GeneralAggregatedDataPoint {

    private double low;
    private double high;
    private double close;
    private double mean;
    private double median;
    private double open;
    private double total;
    private double stddevs;
    private double stddevp;
    private int count;
    private double endValue;
    private double startValue;

    private SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();

    @Override public String toString() {
        return "GeneralAggregatedDataPoint [startValue=" + startValue + ", endValue=" + endValue + ", count=" + count + "]";
    }

    public GeneralAggregatedDataPoint() {

    }

    public void addValue(double value) {
        stats.addValue(value);
    }

    /**
     * Creates an empty data point, used to indicate a gap in the data where no
     * results were received
     * 
     * @param startTime
     */
    public GeneralAggregatedDataPoint(double startValue, double endValue) {
        this.startValue = startValue;
        this.endValue = endValue;

        this.mean = Double.NaN;
        this.median = Double.NaN;
        this.open = Double.NaN;
        this.close = Double.NaN;
        this.high = Double.NaN;
        this.low = Double.NaN;
        this.stddevs = Double.NaN;
        this.stddevp = Double.NaN;
        this.total = 0;
        this.count = 0;
    }

   
    public int getCount() {
        return count;
    }

    public double getStartValue() {
        return startValue;
    }
    
    public double getEndValue() {
        return endValue;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getOpen() {
        return open;
    }

    public double getStddev() {
        return stddevp;
    }

    public double getTotal() {
        return total;
    }

    public double getValue(AggregatedDataKey key) {
        double value;
        switch (key) {
            case Mean:
                value = getMean();
                break;
            case High:
                value = getHigh();
                break;
            case Low:
                value = getLow();
                break;
            case Close:
                value = getClose();
                break;
            case Median:
                value = getMedian();
                break;
            case Open:
                value = getOpen();
                break;
            case Sum:
                value = getTotal();
                break;
            case Stddevs:
                value = getStddevs();
                break;
            case Stddevp:
                value = getStddevp();
                break;
            case Count:
                value = getCount();
                break;
            default:
                throw new RuntimeException("Unsupported key " + key);
        }
        return value;
    }

    private double getStddevp() {
        return stddevp;

    }

    private double getStddevs() {
        return stddevs;

    }

    public String getLegend() {
        return null;

    }

    /**
     * Merge the values from this other AggregatedDataPoint into our values.
     * This obviously invalidates quite a few of the stats we maintain... but
     * hey.
     * 
     * @param next
     */
    public void merge(GeneralAggregatedDataPoint other) {
        startValue = Math.min(startValue, other.startValue);
        endValue = Math.max(endValue, other.endValue);

        low = Math.min(low, other.low);
        high = Math.min(high, other.high);

        // As we dont store the open and close times
        close = Double.NaN;
        open = Double.NaN;

        // No chance
        stddevs = Double.NaN;
        stddevp = Double.NaN;
        median = Double.NaN;

        // Finally, some stuff we _can_ do
        mean = (this.total + other.total) / (this.count + other.count);
        total += other.count;
        count += other.count;
    }

    public boolean isEmpty() {
        return stats.isEmpty();
         
    }

    public void captureStats() {
        stats.doCalculations();

        low = stats.getMinimum();
        high = stats.getMaximum();
        close = stats.getLast();
        mean = stats.getMean();
        median = stats.getMedian();
        open = stats.getFirst();
        total=  stats.getSum();
        stddevs = stats.getStandardDeviationSampleDistribution();
        stddevp = stats.getStandardDeviationPopulationDistrubution();
        count= stats.getCount();
        
        stats.clear();
    }
}
