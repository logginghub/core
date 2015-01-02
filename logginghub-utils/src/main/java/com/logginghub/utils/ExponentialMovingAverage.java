package com.logginghub.utils;


public class ExponentialMovingAverage extends MovingAverage {

    public ExponentialMovingAverage(int dataPoints) {
        super(dataPoints);
    }

    public double calculateMovingAverage() {

        double k = 2 / ((double)dataPoints + 1);

        double lastEma = 0;

        if (values.size() > 0) {

            for (Double value : values) {
                double ema = (value * k) + (lastEma * (1 - k));
                lastEma = ema;
            }
        }
        else {
            lastEma = Double.NaN;
        }

        return lastEma;

    }

}
