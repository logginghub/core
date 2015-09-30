package com.logginghub.utils;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Variation on the theme of the {@link Statistics} class, this one will only do a single pass through the data to calculate all of the relevant stats
 * - obviously very useful on large data sets.
 *
 * @author James
 */
public class SinglePassStatisticsDoublePrecision {

    public static String newline = System.getProperty("line.separator");
    private String name;
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

    public SinglePassStatisticsDoublePrecision() {

    }

    public SinglePassStatisticsDoublePrecision(String name) {
        this.name = name;
    }

    public SinglePassStatisticsDoublePrecision(double... initialValues) {
        addValues(initialValues);
    }

    public void addValues(double... values) {
        for (double d : values) {
            addValue(d);
        }
    }

    public void addValue(double t) {
        changed = true;
        synchronized (values) {
            values.add(t);
        }
    }

    public void addValues(List<? extends Number> result) {
        for (Number f : result) {
            addValue(f.doubleValue());
        }
    }

    public void clear() {
        values.clear();
        reset();
    }

    public void dump() {
        System.out.println(toString());
    }

    public void merge(SinglePassStatisticsDoublePrecision other) {
        if(other.getValues().isEmpty()) {
            // We haven't stored the underlying values, need to do a basic merge of the values we can
            mergeSnapshot(other);
        }else{
            mergeFullData(other);
        }

    }

    private void mergeFullData(SinglePassStatisticsDoublePrecision other) {
        ArrayList<Double> values = other.getValues();
        for (Double value : values) {
            addValue(value);
        }
    }

    /**
     * Take the values we can from this snapshot and merge them in whatever way we can.
     * @param statistic
     */
    public void mergeSnapshot(SinglePassStatisticsDoublePrecision statistic) {
        this.absdev = Double.NaN;
        this.count += statistic.count;
        this.last = statistic.last;
        this.max = Math.max(this.max, statistic.max);
        this.mean = (this.mean + statistic.mean)/2;
        this.median = Double.NaN;
        this.min = Math.min(this.min, statistic.min);
        this.stddevp = Double.NaN;
        this.stddevs = Double.NaN;
        this.sum += statistic.sum;
        this.percentileValues = new double[] {};
    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        StringBuilder builder = new StringBuilder();

        synchronized (values) {

            if(!values.isEmpty()) {

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
            }else{
                builder.append(nf.format(count));
                builder.append(" elements (snapshot)");
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

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getMeanOps() {
        return (1000 * 1e6) / getMean();
    }

    public double getStandardDeviationPopulationDistrubution() {
        return stddevp;
    }

    public void fromSnapshot(StatisticsSnapshot statisticsSnaphot) {
        // TODO : this feels a little bit hacky, as we don't have access to the original values anymore
        this.absdev = statisticsSnaphot.absdev;
        this.count = statisticsSnaphot.count;
        this.first = statisticsSnaphot.first;
        this.last = statisticsSnaphot.last;
        this.max = statisticsSnaphot.max;
        this.mean = statisticsSnaphot.mean;
        this.median = statisticsSnaphot.median;
        this.min = statisticsSnaphot.min;
        this.name = statisticsSnaphot.name;
        this.stddevp = statisticsSnaphot.stddevp;
        this.stddevs = statisticsSnaphot.stddevs;
        this.sum = statisticsSnaphot.sum;
        this.percentileValues = Arrays.copyOf(statisticsSnaphot.percentileValues, statisticsSnaphot.percentileValues.length);
    }

    public double getAbsoluteDeviation() {
        return absdev;
    }

    public int getCalculationCount() {
        return calculationCount;

    }

    public int getCount() {
        return count;
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

    public double getFirst() {
        return first;
    }

    public double getLast() {
        return last;
    }

    public double getMaximum() {
        return max;
    }

    // private double calculateErrorPercentage() {
    // return 100 * calculateAverageAbsoluteDeviationFromTheMean() / mean;
    // }

    public double getMinimum() {
        return min;
    }

    public double getMode() {
        return 0;

    }

    public String getName() {
        return name;
    }

    public double getPercentageAbsoluteDeviation() {
        return 100d * (absdev / mean);
    }

    public double[] getPercentiles() {
        return percentileValues;
    }

    public StatisticsSnapshot getSnapshot() {
        doCalculations();

        StatisticsSnapshot statisticsSnaphot = new StatisticsSnapshot();
        statisticsSnaphot.absdev = absdev;
        statisticsSnaphot.count = count;
        statisticsSnaphot.first = first;
        statisticsSnaphot.last = last;
        statisticsSnaphot.max = max;
        statisticsSnaphot.mean = mean;
        statisticsSnaphot.median = median;
        statisticsSnaphot.min = min;
        statisticsSnaphot.name = name;
        statisticsSnaphot.stddevp = stddevp;
        statisticsSnaphot.stddevs = stddevs;
        statisticsSnaphot.sum = sum;
        statisticsSnaphot.percentileValues = Arrays.copyOf(percentileValues, percentileValues.length);

        return statisticsSnaphot;

    }

    public void doCalculations() {

        if (!changed)
            return;

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
            } else {
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

            // Update the mode map
            //            modeMap.get(value).increment();
        }

        stddevs = Math.sqrt(sumDeviationSquared / count - 1);
        stddevp = Math.sqrt(sumDeviationSquared / (count));
        absdev = sumAbsDeviation / count;
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

    private void calculatePercentiles(ArrayList<Double> values) {

        percentileValues = new double[101];

        for (int i = 0; i <= 100; i++) {

            double rank = ((i / 100d) * (values.size() - 1)) + 1;

            int integerRank = (int) Math.floor(rank);
            double decimalRank = rank - integerRank;

            double percentileValue;
            if (rank == 1) {
                percentileValue = values.get(0);
            } else if (integerRank == values.size()) {
                percentileValue = values.get(values.size() - 1);
            } else {
                // The -1 is because we are using zero indexed lists
                double valueAtRank = values.get(integerRank - 1);
                double valueAtNextRank = values.get(integerRank);
                percentileValue = valueAtRank + (decimalRank * (valueAtNextRank - valueAtRank));
            }

            percentileValues[i] = percentileValue;
        }
    }

    public double getStandardDeviationSampleDistribution() {
        return stddevs;
    }

    public double getSum() {
        return sum;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public enum Statistic {
        Mean,
        Count,
    }

    public static class StatisticsSnapshot implements SerialisableObject {

        private String name;
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

        public StatisticsSnapshot() {

        }

        @Override
        public void read(SofReader reader) throws SofException {
            this.name = reader.readString(0);
            this.median = reader.readDouble(1);
            this.sum = reader.readDouble(2);
            this.count = reader.readInt(3);
            this.mean = reader.readDouble(4);
            this.min = reader.readDouble(5);
            this.max = reader.readDouble(6);
            this.first = reader.readDouble(7);
            this.last = reader.readDouble(8);
            this.stddevp = reader.readDouble(9);
            this.stddevs = reader.readDouble(10);
            this.absdev = reader.readDouble(11);

            int field = 12;
            int length = reader.readInt(field++);
            this.percentileValues = new double[length];
            for (int i = 0; i < length; i++) {
                this.percentileValues[i] = reader.readDouble(field++);
            }

        }

        @Override
        public void write(SofWriter writer) throws SofException {
            writer.write(0, name);
            writer.write(1, median);
            writer.write(2, sum);
            writer.write(3, count);
            writer.write(4, mean);
            writer.write(5, min);
            writer.write(6, max);
            writer.write(7, first);
            writer.write(8, last);
            writer.write(9, stddevp);
            writer.write(10, stddevs);
            writer.write(11, absdev);

            int field = 12;
            writer.write(field++, percentileValues.length);
            for (double percentileValue : percentileValues) {
                writer.write(field++, percentileValue);
            }
        }
    }

}