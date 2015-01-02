package com.logginghub.utils;

import java.util.Random;

import com.logginghub.utils.logging.Logger;

/**
 * Generates random numbers between a min and max using momement tracking to create trends.
 * 
 * @author James
 * @deprecated This doesn't generate the right range of numbers due to the dodgy acceleration code - use {@link RandomWithAcceleration} instead
 * 
 */
public class RandomWithMomentum {

    private static Logger logger = Logger.getLoggerFor(RandomWithMomentum.class);
    private Random random;

    private int currentTrendLength = 0;
    private int currentTrendMidPoint = 0;

    private double currentValue = 0;
    private double currentVelocity = 0;
    private double currentAcceleration = 0;

    private double min;

    private double max;

    private int trendLengthMin;
    private int trendLengthMax;

    /**
     * 
     * @param min
     *            The lowest number that can be produced
     * @param max
     *            The highest number that can be produced
     * @param trendLengthMin
     *            The longest number of calls that will result in a single trend
     * @param trendLengthMax
     *            The fewest number of calls that will result in a single trend
     */
    public RandomWithMomentum(long seed, double min, double max, int trendLengthMin, int trendLengthMax) {
        this.min = min;
        this.max = max;
        this.trendLengthMin = trendLengthMin;
        this.trendLengthMax = trendLengthMax;

        random = new Random(seed);

        currentValue = getRandomValueInRange();

        reset();
    }
    
    public void setMin(double min) {
        this.min = min;
    }
    
    public void setMax(double max) {
        this.max = max;
    }

    public void reset() {
        resetTrend();
        double target = getRandomValueInRange();

        currentAcceleration = (target - currentValue) / (double) currentTrendLength / (double) currentTrendLength;
        currentVelocity = currentAcceleration;
    }

    private double getRandomValueInRange() {
        double nextValue = random.nextDouble();
        double inRange = (max - min) * nextValue;
        double offset = min + inRange;
        return offset;
    }

    private void resetTrend() {
        currentTrendLength = trendLengthMin + random.nextInt(trendLengthMax - trendLengthMin);
        currentTrendMidPoint = currentTrendLength / 2;
    }

    public double next() {

        currentValue += currentVelocity;

        if (currentTrendLength > currentTrendMidPoint) {
            currentVelocity += currentAcceleration;
        }
        else {
            currentVelocity -= currentAcceleration;
        }
        currentTrendLength--;

        if (currentTrendLength == 0) {
            reset();
        }

        logger.trace("Current value='{}' currentTrendLength='{}' currentTrendMidPoint='{}' currentVelocity='{}' currentAcceleration='{}'", currentValue, currentTrendLength, currentTrendMidPoint, currentVelocity, currentAcceleration);

        return currentValue;

    }

    public static void main(String[] args) {
        RandomWithMomentum momentum = new RandomWithMomentum(0, 0, 100, 5, 10);
        for (int i = 0; i < 100; i++) {
            System.out.println(momentum.next());
        }
    }

}
