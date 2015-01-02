package com.logginghub.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Variation on the theme of the {@link Statistics} class, this one will only do a single pass
 * through the data to calculate all of the relevant stats - obviously very useful on large data
 * sets.
 * 
 * @author James
 * 
 */
public class SinglePassStatisticsDoublePrecision {

    private ArrayList<Double> values = new ArrayList<Double>();

    private double median;
    private double sum;
    private int count;
    private double mean;
    private double min = Double.MAX_VALUE;
    private double max = -Double.MAX_VALUE;
    private double[] percentileValues;
    private double first = Double.NaN;
    private double last = Double.NaN;
    private double stddevp;
    private double stddevs;
    private double absdev;

    private int calculationCount = 0;

    private boolean changed = false;

    public enum Statistic {
        Mean,
        Count,
    }

    public SinglePassStatisticsDoublePrecision() {

    }

    public SinglePassStatisticsDoublePrecision(double... initialValues) {
        addValues(initialValues);
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public void doCalculations() {

        if(!changed) return;
        
        changed = false;
        calculationCount++;
        
        reset();

        if (values.size() == 0) {
            return;
        }

        first = values.get(0);
        last = values.get(values.size() - 1);
        
//        Map<Double, MutableInt> modeMap = new FactoryMap<Double, MutableInt>() {
//            @Override protected MutableInt createEmptyValue(Double key) {
//                return new MutableInt(0);
//            }
//        };
        
        synchronized (values) {

            Collections.sort(values);

            calculatePercentiles(values);
            
            count = values.size();

            for (int i = 0; i < count; ++i) {
                double value = values.get(i);
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
        double sumAbsDeviation= 0;
        
        for (int i = 0; i < count; ++i) {
            double value = values.get(i);
            double deviation = (value - mean);
            double absDeviation = Math.abs(deviation);
            double deviationSquared = deviation * deviation;
            sumDeviationSquared += deviationSquared;
            sumAbsDeviation += absDeviation;
            
            // Update the mode map
//            modeMap.get(value).increment();
        }
        
        stddevs = Math.sqrt(sumDeviationSquared/count-1);
        stddevp = Math.sqrt(sumDeviationSquared/(count));
        absdev = sumAbsDeviation / count;
    }

    private void calculatePercentiles(ArrayList<Double> values) {

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
        min = Double.MAX_VALUE;
        max = -Double.MAX_VALUE;
        first = Double.NaN;
        last = Double.NaN;
        stddevp = 0;
        stddevs = 0;
    }

    public void addValue(double t) {
        changed = true;
        synchronized (values) {
            values.add(t);
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
            builder.append(nf.format(values.size()));
            builder.append(" elements ");
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

    public int getCount() {
        return count;
    }

    public double getSum() {
        return sum;
    }

    public double[] getPercentiles() {
        return percentileValues;
    }

    public double getMaximum() {
        return max;
    }

    public double getMinimum() {
        return min;
    }

    public double getMeanOps() {
        return (1000 * 1e6) / getMean();
    }

    public void clear() {
        values.clear();
        reset();
    }

    public double getLast() {
        return last;
    }

    public double getFirst() {
        return first;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void addValues(double... values) {
        for (double d : values) {
            addValue(d);
        }
    }

    public void addValues(List<? extends Number> result) {
        for (Number f : result) {
            addValue(f.doubleValue());
        }
    }

    public int getCalculationCount() {
        return calculationCount;

    }

    public double getMode() {
        return 0;

    }

}