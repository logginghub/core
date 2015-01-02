package com.logginghub.utils;

import java.util.Random;

import com.logginghub.utils.logging.Logger;

/**
 * Generates random numbers between a min and max using momement tracking to create trends.
 * 
 * @author James
 * 
 */
public class RandomWithAcceleration {

    private static Logger logger = Logger.getLoggerFor(RandomWithAcceleration.class);

    private LERP lerp = LERP.gausian();

    public Function<Double, Double> function = null;

    private int movementThroughTheResultSpacePerCall;
    private int currentMovementThroughTheResultSpace;

    private DoubleValueGenerator trendGenerator;
    private DoubleValueGenerator valueGenerator;

    private int currentTrendLength = 0;

    private double currentVelocity = 0;
    private double currentAcceleration = 0;
    private double currentValue = 0;
    private double targetValue;

    private int trendPosition = 0;

    public static final class RandomGenerator implements DoubleValueGenerator {
        private double min;
        private double max;
        private Random random;

        public RandomGenerator(double min, double max) {
            this.min = min;
            this.max = max;
            this.random = new Random();
        }

        public double next() {
            return min + (random.nextDouble() * (max - min));
        }
    }

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
    public RandomWithAcceleration(DoubleValueGenerator trendGenerator, DoubleValueGenerator valueGenerator) {
        this.trendGenerator = trendGenerator;
        this.valueGenerator = valueGenerator;

        // Pick a starting value
        currentValue = valueGenerator.next();

        generateNewTarget();

        logger.fine("Starting setup : currentValue {} targetValue {} trendLength {} calls", currentValue, targetValue, currentTrendLength);
    }

    private void generateNewTarget() {
        targetValue = valueGenerator.next();

        currentTrendLength = (int) trendGenerator.next();

        currentVelocity = 0;

        // The extra 2 * is because we want to accelerate up to the midpoint, then decelerate down,
        // and still reach our target value
        currentAcceleration = 2 * 2 * (targetValue - currentValue) / (currentTrendLength * currentTrendLength);

        trendPosition = 0;
        logger.fine("Generating new target : currentValue {} targetValue {} trendLength {} calls [current acceleration {}]",
                    currentValue,
                    targetValue,
                    currentTrendLength,
                    currentAcceleration);
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public double next() {

        if (trendPosition == currentTrendLength) {
            generateNewTarget();
        }

        currentVelocity += currentAcceleration;
        currentValue += currentVelocity;
        trendPosition++;

        if (trendPosition == currentTrendLength / 2) {
            currentAcceleration *= -1;
        }

        // if (function != null) {
        // nextValue = function.apply(nextValue);
        // }

        logger.finer("Next value : {} (current velocity {})", currentValue, currentVelocity);

        return currentValue;
    }

    public double getFactorThroughTrendSpace() {
        double factorThroughTrendSpace = (trendPosition / (double) currentTrendLength);
        return factorThroughTrendSpace;
    }

    public void setFunction(Function<Double, Double> function) {
        this.function = function;
    }

}
