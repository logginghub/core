package com.logginghub.utils;

import java.text.NumberFormat;

public class ConcurrentMovingAverage {
    private CircularArray<Double> array;

    public ConcurrentMovingAverage(int dataPoints) {
        array = new CircularArray<Double>(Double.class, dataPoints);
    }

    public void addValue(double t) {
        array.append(t);
    }

    public double calculateMovingAverage() {
        double mean;

        int populatedElements = 0;

        double total = 0;
        Double[] data = array.getData();
        for (Double d : data) {
            if (d != null) {
                total += d.doubleValue();
                populatedElements++;
            }
        }

        if (populatedElements > 0) {
            mean = total / populatedElements;
        }
        else {
            mean = Double.NaN;
        }

        return mean;
    }

    public ConcurrentMovingAverage calculateDifferential() {
        ConcurrentMovingAverage differential = new ConcurrentMovingAverage(array.getData().length - 1);

        Double[] values = array.getData();
        double previous = Double.NaN;
        for (Double value : values) {
            if (!Double.isNaN(previous)) {
                double delta = value - previous;
                differential.addValue(delta);
            }

            previous = value;
        }

        return differential;
    }

    @Override public String toString() {
        return NumberFormat.getInstance().format(calculateMovingAverage());
    }
}
