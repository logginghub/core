package com.logginghub.utils;

import java.util.LinkedList;

public class MovingAverage {
    protected LinkedList<Double> values = new LinkedList<Double>();
    protected final int dataPoints;

    public MovingAverage(int dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void addValue(double t) {
        values.add(t);
        if (values.size() > dataPoints) {
            values.removeFirst();
        }
    }

    public LinkedList<Double> getValues() {
        return values;
    }

    public double calculateMovingAverage() {
        double mean;

        if (values.size() > 0) {
            double total = 0;
            for (Double value : values) {
                total += value.doubleValue();
            }

            mean = total / values.size();
        }
        else {
            mean = Double.NaN;
        }

        return mean;
    }

    public MovingAverage calculateDifferential() {
        MovingAverage differential = new MovingAverage(dataPoints - 1);

        LinkedList<Double> values = getValues();
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
}
