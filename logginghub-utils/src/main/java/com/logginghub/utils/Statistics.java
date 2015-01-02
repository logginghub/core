package com.logginghub.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statistics {
    private ArrayList<Double> values = new ArrayList<Double>();

    public enum Statistic {
        Mean,
        Count,
    }

    public Statistics() {

    }

    public double extract(Statistic statistic) {
        double result;
        switch (statistic) {
            case Mean:
                result = calculateMean();
                break;
            case Count:
                result = getCount();
                break;
            default:
                throw new RuntimeException(statistic + " not supported yet.");
        }
        return result;
    }

    public void addValue(double t) {
        synchronized (values) {
            values.add(t);
        }
    }

    public double calculateMedian() {
        if (values.isEmpty()) return Double.NaN;

        List<Double> temp = getSortedValues();

        double median;

        if (temp.size() % 2 == 0) {
            int midPoint = temp.size() / 2;
            median = (temp.get(midPoint) + temp.get(midPoint - 1)) / 2;
        }
        else {
            median = temp.get(temp.size() / 2);
        }

        return median;
    }

    private List<Double> getSortedValues() {
        List<Double> temp = new ArrayList<Double>();
        synchronized (values) {
            temp.addAll(values);
        }

        Collections.sort(temp);
        return temp;
    }

    public int calculateModeCount() {
        int maxCount = 0;

        synchronized (values) {
            for (int i = 0; i < values.size(); ++i) {
                int count = 0;
                for (int j = 0; j < values.size(); ++j) {
                    if (values.get(j) == values.get(i)) {
                        ++count;
                    }
                }
                if (count > maxCount) {
                    maxCount = count;
                }
            }
        }

        return maxCount;
    }

    public double calculateMode() {
        double maxValue = 0;
        int maxCount = 0;

        synchronized (values) {
            for (int i = 0; i < values.size(); ++i) {
                int count = 0;
                for (int j = 0; j < values.size(); ++j) {
                    if (values.get(j) == values.get(i)) {
                        ++count;
                    }
                }
                if (count > maxCount) {
                    maxCount = count;
                    maxValue = values.get(i);
                }
            }
        }

        return maxValue;
    }

    public double calculateMean() {
        double mean;

        if (values.size() > 0) {
            double total = calculateSum();
            mean = total / values.size();
        }
        else {
            mean = Double.NaN;
        }

        return mean;
    }

    public double calculateAverageAbsoluteDeviationFromTheMean() {
        double mean = calculateMean();
        double averageAbsoluteDeviationFromTheMean = calculateAverageDeviationFromExpected(mean);
        return averageAbsoluteDeviationFromTheMean;
    }

    public double calculateAverageDeviationFromExpected(double expected) {
        double deviationRunningTotal = 0;
        double averageAbsoluteDeviationFromTheMean;
        synchronized (values) {
            for (double value : values) {
                double absoluteDeviation = Math.abs(value - expected);
                deviationRunningTotal += absoluteDeviation;
            }

            averageAbsoluteDeviationFromTheMean = deviationRunningTotal / values.size();
        }

        return averageAbsoluteDeviationFromTheMean;
    }

    public double calculateAverageAbsoluteDeviationFromTheMedian() {
        double median = calculateMedian();
        double averageAbsoluteDeviationFromTheMedian = calculateAverageDeviationFromExpected(median);
        return averageAbsoluteDeviationFromTheMedian;
    }

    public double calculateStandardDeviationFast() {
        final int n = values.size();
        double sum = 0;

        synchronized (values) {
            // sd is sqrt of sum of (values-mean) squared divided by n - 1
            // Calculate the mean
            double mean = 0;

            if (n < 2) {
                return Double.NaN;
            }
            for (int i = 0; i < n; i++) {
                mean += values.get(i);
            }
            mean /= n;
            // calculate the sum of squares

            for (int i = 0; i < n; i++) {
                final double v = values.get(i) - mean;
                sum += v * v;
            }
        }
        // Change to ( n - 1 ) to n if you have complete data instead of a
        // sample.
        return Math.sqrt(sum / (n - 1));
    }

    /**
     * Calculates the sample standard deviation of an array of numbers, when the number are obtained
     * by a random sampling. Code must be modified if you have a complete set of data. To get
     * estimate of a complete sample, use n instead of n-1 in last line.
     * 
     * see Knuth's The Art Of Computer Programming Volume II: Seminumerical Algorithms This
     * algorithm is slower, but more resistant to error propagation.
     * 
     * @param data
     *            Numbers to compute the standard deviation of. Array must contain two or more
     *            numbers.
     * @return standard deviation estimate of population
     */
    public double calculateStandardDeviationKnuth() {
        final int n = values.size();
        double sum = 0;

        if (n < 2) {
            return Double.NaN;
        }

        synchronized (values) {
            double avg = values.get(0);

            for (int i = 1; i < values.size(); i++) {
                double value = values.get(i);
                double newavg = avg + (value - avg) / (i + 1);
                sum += (value - avg) * (value - newavg);
                avg = newavg;
            }
        }
        // Change to ( n - 1 ) to n if you have complete data instead of a
        // sample.
        return Math.sqrt(sum / (n - 1));
    }

    public void dump() {
        System.out.println(toString());
    }

    public static String newline = System.getProperty("line.separator");

    @Override public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        StringBuilder builder = new StringBuilder();

        synchronized (values) {
            builder.append(values.size());
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
            builder.append(nf.format(calculateMean()));
            builder.append(newline);
            builder.append(" median=");
            builder.append(nf.format(calculateMedian()));
            if (calculateModeCount() > 1) {
                builder.append(newline);
                builder.append(" mode=");
                builder.append(nf.format(calculateMode()));
            }
            builder.append(newline);
            builder.append(" std dev=");
            builder.append(nf.format(calculateStandardDeviationKnuth()));
            builder.append(newline);
            builder.append(" ave abs dev mean=");
            builder.append(nf.format(calculateAverageAbsoluteDeviationFromTheMean()));
            builder.append(newline);
            builder.append(" ave abs dev median=");
            builder.append(nf.format(calculateAverageAbsoluteDeviationFromTheMedian()));
            builder.append(newline);
            builder.append(" error percentage=");
            builder.append(nf.format(calculateErrorPercentage()));
            builder.append("%");
        }
        return builder.toString();
    }

    private double calculateErrorPercentage() {
        return 100 * calculateAverageAbsoluteDeviationFromTheMean() / calculateMean();
    }

    public double getCount() {
        return values.size();
    }

    public double calculateSum() {
        double sum = 0;
        for (Double value : values) {
            sum += value.doubleValue();
        }
        return sum;
    }

    public int calculatePercentileCount(int lower, int upper) {

        double lowerPercentileValue = calculatePercentile(lower);
        if (lower == 0) {
            lowerPercentileValue = minimum();
        }
        double upperPercentileValue = calculatePercentile(upper);

        int count = 0;
        List<Double> sortedValues = getSortedValues();
        for (Double value : sortedValues) {
            if (value >= lowerPercentileValue && value < upperPercentileValue) {
                count++;
            }

            // Early exit optimisation
            if (value > upperPercentileValue) {
                break;
            }
        }

        return count;
    }

    private double minimum() {
        List<Double> sortedValues = getSortedValues();
        return sortedValues.get(0);
    }

    public double calculatePercentile(int percentile) {
        if (values.size() < 2 || percentile == 0 || percentile > 100) {
            return Double.NaN;
        }
        List<Double> sortedValues = getSortedValues();
        int count = sortedValues.size();
        float exactIndex = (percentile / 100f) * count;
        int above = (int) Math.ceil(exactIndex);
        int below = (int) Math.floor(exactIndex);

        double value;
        if (above == below) {
            value = sortedValues.get(above - 1);
        }
        else {
            double aboveValue = sortedValues.get(above - 1);
            double belowValue;
            if (below == 0) {
                belowValue = sortedValues.get(0);
            }
            else {
                belowValue = sortedValues.get(below - 1);
            }
            value = belowValue + ((aboveValue - belowValue) * (exactIndex - below));
        }

        return value;
    }

    public double calculateMaximum() {
        double max = -Double.MAX_VALUE;
        synchronized (values) {
            for (double value : values) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    public double calculateMinimum() {
        double min = Double.MAX_VALUE;
        synchronized (values) {
            for (double value : values) {
                if (value < min) {
                    min = value;
                }
            }
        }
        return min;
    }

    public double getLastValue() {
        double lastValue;
        synchronized (values) {
            if (values.size() > 0) {
                lastValue = values.get(values.size() - 1);
            }
            else {
                lastValue = Double.NaN;
            }
        }

        return lastValue;
    }

}
