package com.logginghub.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * A version of the single pass statistics that uses long values for extra
 * precision. Useful when dealing with largish nanosecond times that might not
 * do so well in floating point land.
 * 
 * @author James
 * 
 */
public class SinglePassStatisticsLongPrecision {

    private ArrayList<Long> values = new ArrayList<Long>();

    private double median;
    private double sum;
    private int count;
    private double mean;
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
    private double[] percentileValues;
    private Long first = null;
    private Long last = null;
    private double stddevp;
    private double stddevs;
    private double absdev;

    private int calculationCount = 0;

    private boolean changed = false;

    private TimeUnit units = TimeUnit.MILLISECONDS;

    public enum Statistic {
        Mean,
        Count,
    }

    public SinglePassStatisticsLongPrecision(TimeUnit units){
        setUnits(units);
    }

    public SinglePassStatisticsLongPrecision(long... initialValues) {
        addValues(initialValues);
    }

    /**
     * Create a new SinglePassStatisticsLongPrecision instance wrapping the
     * values list passed in.
     * 
     * @param clone
     */
    public SinglePassStatisticsLongPrecision(ArrayList<Long> list) {
        values = list;
    }
    
    public void setUnits(TimeUnit units) {
        this.units = units;
    }

    public ArrayList<Long> getValues() {
        return values;
    }

    public void doCalculations() {

        if (!changed) return;

        changed = false;
        calculationCount++;

        reset();

        if (values.size() == 0) {
            return;
        }

        first = values.get(0);
        last = values.get(values.size() - 1);

        synchronized (values) {

            Collections.sort(values);

            calculatePercentiles(values);

            count = values.size();

            for (int i = 0; i < count; ++i) {
                long value = values.get(i);
                sum += value;

                // Min/max
                max = Math.max(value, max);
                min = Math.min(value, min);
            }

            // Calculate the mean
            mean = sum / (double) count;

            // Calculate the median
            if (count % 2 == 0) {
                int midPoint = count / 2;
                median = (values.get(midPoint) + values.get(midPoint - 1)) / 2;
            }
            else {
                median = values.get(count / 2);
            }
        }

        // Do the second pass for the deviations
        double sumDeviationSquared = 0;
        double sumAbsDeviation = 0;

        for (int i = 0; i < count; ++i) {
            double value = values.get(i);
            double deviation = (value - mean);
            double absDeviation = Math.abs(deviation);
            double deviationSquared = deviation * deviation;
            sumDeviationSquared += deviationSquared;
            sumAbsDeviation += absDeviation;
        }

        stddevs = Math.sqrt(sumDeviationSquared / count - 1);
        stddevp = Math.sqrt(sumDeviationSquared / (count));
        absdev = sumAbsDeviation / count;
    }

    private void calculatePercentiles(ArrayList<Long> values) {

        percentileValues = new double[101];

        for (int i = 0; i <= 100; i++) {

            double rank = ((i / 100d) * (values.size() - 1)) + 1;

            int integerRank = (int) Math.floor(rank);
            double decimalRank = rank - integerRank;

            double percentileValue;
            if (rank == 1) {
                percentileValue = values.get(0);
            }
            else if (integerRank == values.size()) {
                percentileValue = values.get(values.size() - 1);
            }
            else {
                // The -1 is because we are using zero indexed lists
                double valueAtRank = values.get(integerRank - 1);
                double valueAtNextRank = values.get(integerRank);
                percentileValue = valueAtRank + (decimalRank * (valueAtNextRank - valueAtRank));
            }

            percentileValues[i] = percentileValue;
        }
    }

    private void reset() {
        median = 0;
        sum = 0;
        count = 0;
        mean = 0;
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        first = null;
        last = null;
        stddevp = 0;
        stddevs = 0;
    }

    public void addValue(long t) {
        changed = true;
        synchronized (values) {
            values.add(t);
        }
    }

    /**
     * If you only want to maintain the latest n values, call this method to
     * remove the items at the start of the list
     * 
     * @param maximumLength
     */
    public void evictOldestItems(int maximumLength) {
        if (maximumLength >= 0) {
            synchronized (values) {
                while (values.size() > maximumLength) {
                    values.remove(0);
                    changed = true;
                }
            }
        }
    }

    public double getAbsoluteDeviation() {
        return absdev;
    }

    public double getPercentageAbsoluteDeviation() {
        return 100d * (absdev / mean);
    }

    public double getStandardDeviationPopulationDistrubution() {
        return stddevp;
    }

    public double getStandardDeviationSampleDistribution() {
        return stddevs;
    }

    public double getMedian() {
        return median;
    }

    public double getMean() {
        return mean;
    }

    // public double calculateAverageAbsoluteDeviationFromTheMean() {
    // double averageAbsoluteDeviationFromTheMean =
    // calculateAverageDeviationFromExpected(mean);
    // return averageAbsoluteDeviationFromTheMean;
    // }

    // public double calculateAverageDeviationFromExpected(double expected) {
    // double deviationRunningTotal = 0;
    // double averageAbsoluteDeviationFromTheMean;
    // synchronized (values) {
    // for (double value : values) {
    // double absoluteDeviation = Math.abs(value - expected);
    // deviationRunningTotal += absoluteDeviation;
    // }
    //
    // averageAbsoluteDeviationFromTheMean = deviationRunningTotal /
    // values.size();
    // }
    //
    // return averageAbsoluteDeviationFromTheMean;
    // }

    // public double calculateAverageAbsoluteDeviationFromTheMedian() {
    // double median = calculateMedian();
    // double averageAbsoluteDeviationFromTheMedian =
    // calculateAverageDeviationFromExpected(median);
    // return averageAbsoluteDeviationFromTheMedian;
    // }

    public void dump() {
        System.out.println(toString());
    }

    public static String newline = System.getProperty("line.separator");

    @Override public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        StringBuilder builder = new StringBuilder();

        synchronized (values) {
            builder.append(nf.format(values.size())).append(" elements").append(newline);
            builder.append(nf.format(getSum())).append(" sum").append(newline);
            
            if (values.size() <= 10) {
                builder.append("{ ");
                for (double value : values) {
                    builder.append(nf.format(value));
                    builder.append(" ");
                }
                builder.append("}");
            }

            builder.append(newline);
            builder.append(" mean=");
            builder.append(nf.format(getMean()));
            builder.append(newline);
            builder.append(" median=");
            builder.append(nf.format(getMedian()));
            builder.append(newline);
            builder.append(" min=");
            builder.append(nf.format(min));
            builder.append(newline);
            builder.append(" max=");
            builder.append(nf.format(max));
            builder.append(newline);
            builder.append(" mean ops/sec=");
            builder.append(nf.format(getMeanOps()));

            // if (calculateModeCount() > 1) {
            // builder.append(newline);
            // builder.append(" mode=");
            // builder.append(nf.format(calculateMode()));
            // }
            builder.append(newline);
            builder.append(" std dev=");
            builder.append(nf.format(getStandardDeviationPopulationDistrubution()));
            builder.append(newline);
            builder.append(StringUtils.format("TP50={}", nf.format(percentileValues[50])));
            builder.append(newline);
            builder.append(StringUtils.format("TP90={}", nf.format(percentileValues[90])));
            builder.append(newline);
            builder.append(StringUtils.format("TP95={}", nf.format(percentileValues[95])));
            builder.append(newline);
            builder.append(StringUtils.format("TP99={}", nf.format(percentileValues[99])));
            builder.append(newline);
            
            builder.append(Arrays.toString(percentileValues));
            // builder.append(" ave abs dev mean=");
            // builder.append(nf.format(calculateAverageAbsoluteDeviationFromTheMean()));
            // builder.append(newline);
            // builder.append(" ave abs dev median=");
            // builder.append(nf.format(calculateAverageAbsoluteDeviationFromTheMedian()));
            // builder.append(newline);
            // builder.append(" error percentage=");
            // builder.append(nf.format(calculateErrorPercentage()));
            // builder.append("%");
        }
        return builder.toString();
    }

    // private double calculateErrorPercentage() {
    // return 100 * calculateAverageAbsoluteDeviationFromTheMean() / mean;
    // }

    /**
     * @return the count as of the last time doCalculations was called.
     */
    public int getCount() {
        return count;
    }
    
    public int getCurrentCount() {
        synchronized (values) {
         return values.size();
        }
    }

    public double getSum() {
        return sum;
    }

    public double[] getPercentiles() {
        return percentileValues;
    }

    public long getMaximum() {
        return max;
    }

    public long getMinimum() {
        return min;
    }

    public double getMeanOps() {
        long convert = units.convert(1, TimeUnit.SECONDS);
        double mean = getMean();
        double ops = convert / mean;
        return ops;
    }

    public void clear() {
        values.clear();
        reset();
    }

    public long getLast() {
        return last;
    }

    public long getFirst() {
        return first;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void addValues(long... values) {
        for (long d : values) {
            addValue(d);
        }
    }

    public int getCalculationCount() {
        return calculationCount;

    }

    @SuppressWarnings("unchecked") public SinglePassStatisticsLongPrecision copy() {

        synchronized (values) {
            SinglePassStatisticsLongPrecision copy = new SinglePassStatisticsLongPrecision((ArrayList<Long>) values.clone());

            copy.median = median;
            copy.sum = sum;
            copy.count = count;
            copy.mean = mean;
            copy.min = min;
            copy.max = max;
            copy.percentileValues = percentileValues;
            copy.first = first;
            copy.last = last;
            copy.stddevp = stddevp;
            copy.stddevs = stddevs;
            copy.absdev = absdev;
            copy.calculationCount = calculationCount;
            copy.changed = changed;

            return copy;
        }

    }

}