package com.logginghub.analytics.model;

import java.io.Serializable;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

/**
 * Encapsulation of a single point of aggregated time series data.
 * @author James
 *
 */
public class AggregatedDataPoint implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private long startTime;
    private long endTime;
    private double[] percentiles;
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
    
    @Override public String toString() {
        return "AggregatedDataPoint [startTime=" + startTime + ", count=" + count + "]";
    }

    public AggregatedDataPoint() {

    }

    /**
     * Creates an empty data point, used to indicate a gap in the data where no
     * results were received
     * 
     * @param startTime
     */
    public AggregatedDataPoint(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

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

    public AggregatedDataPoint(long startTime, long endTime, SinglePassStatisticsDoublePrecision currentStats) {
        this.startTime = startTime;
        this.endTime = endTime;

        currentStats.doCalculations();

        this.mean = currentStats.getMean();
        this.median = currentStats.getMedian();
        this.open = currentStats.getFirst();
        this.close = currentStats.getLast();
        this.high = currentStats.getMaximum();
        this.low = currentStats.getMinimum();
        this.stddevs = currentStats.getStandardDeviationSampleDistribution();
        this.stddevp = currentStats.getStandardDeviationPopulationDistrubution();
        this.total = currentStats.getSum();
        this.count = currentStats.getCount();

        this.percentiles = currentStats.getPercentiles();

    }

    public int getCount() {
        return count;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
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

    public double[] getPercentiles() {
        return percentiles;
    }

    public double getStddev() {
        return stddevp;
    }

    public double getTotal() {
        return total;
    }

    public Double getPercentile(int percentile) {
        double percentileValue;
        if (percentiles != null) {
            percentileValue = percentiles[percentile];
        }
        else {
            percentileValue = Double.NaN;
        }
        return percentileValue;

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
            case Percentile10:
                value = getPercentile(10);
                break;
            case Percentile20:
                value = getPercentile(20);
                break;
            case Percentile30:
                value = getPercentile(30);
                break;
            case Percentile40:
                value = getPercentile(40);
                break;
            case Percentile50:
                value = getPercentile(50);
                break;
            case Percentile60:
                value = getPercentile(60);
                break;
            case Percentile70:
                value = getPercentile(70);
                break;
            case Percentile80:
                value = getPercentile(80);
                break;
            case Percentile90:
                value = getPercentile(90);
                break;
            case Percentile95:
                value = getPercentile(95);
                break;
            case Percentile96:
                value = getPercentile(96);
                break;
            case Percentile97:
                value = getPercentile(97);
                break;
            case Percentile98:
                value = getPercentile(98);
                break;
            case Percentile99:
                value = getPercentile(99);
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

    /**
     * Merge the values from this other AggregatedDataPoint into our values.
     * This obviously invalidates quite a few of the stats we maintain... but
     * hey.
     * 
     * @param next
     */
    public void merge(AggregatedDataPoint other) {
        startTime = Math.min(startTime, other.startTime);
        endTime = Math.max(endTime, other.endTime);
        percentiles = new double[100];
        for (int i = 0; i < percentiles.length; i++){
            percentiles[i] = Double.NaN;
        }
        
        low = Math.min(low, other.low);
        high = Math.max(high, other.high);
       
        // As we dont store the open and close times
        close = Double.NaN;
        open =  Double.NaN;

        // No chance
        stddevs=  Double.NaN;
        stddevp=  Double.NaN;
        median = Double.NaN;
        
        // Finally, some stuff we _can_ do
        mean = (this.total + other.total) / (this.count + other.count);
        total += other.total;
        count += other.count;
    }
}
