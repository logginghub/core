package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

/**
 * Generates random numbers between a min and max using momement tracking to create trends.
 * 
 * @author James
 * 
 */
public class RandomWithLerp {

    private static Logger logger = Logger.getLoggerFor(RandomWithLerp.class);

    private LERP lerp = LERP.gausian();

    public Function<Double, Double> function = null;

    private int movementThroughTheResultSpacePerCall;
    private int currentMovementThroughTheResultSpace;

    private DoubleValueGenerator trendGenerator;
    private DoubleValueGenerator valueGenerator;

    private int currentTrendLength = 0;

    private double currentValue = 0;
    private double targetValue;

    private int trendPosition = 0;


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
    public RandomWithLerp(DoubleValueGenerator trendGenerator, DoubleValueGenerator valueGenerator) {
        this.trendGenerator = trendGenerator;
        this.valueGenerator = valueGenerator;

        // Pick a starting value
        targetValue = valueGenerator.next();
        
        generateNewTarget();

        logger.fine("Starting setup : currentValue {} targetValue {} trendLength {} calls", currentValue, targetValue, currentTrendLength);
    }

    private void generateNewTarget() {
        currentValue = targetValue;
        targetValue = valueGenerator.next();
        currentTrendLength = (int) trendGenerator.next();
        trendPosition = 0;
        logger.fine("Generating new target : currentValue {} targetValue {} trendLength {} calls", currentValue, targetValue, currentTrendLength);
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public double next() {

        if(trendPosition == currentTrendLength) {
            generateNewTarget();
        }
        
        double factorThroughTrendSpace = getFactorThroughTrendSpace();
        double valueDelta = (targetValue - currentValue);
        double nextValue = currentValue + (valueDelta * factorThroughTrendSpace);

        trendPosition++;
        
        if(function != null) {
            nextValue = function.apply(nextValue);
        }
        
        logger.finer("Next value : {} (factor {} delta {})", nextValue, factorThroughTrendSpace, valueDelta);
        
        return nextValue;
    }

    public double getFactorThroughTrendSpace() {
        double factorThroughTrendSpace = (trendPosition / (double)currentTrendLength);
        return factorThroughTrendSpace;
    }

    public void setFunction(Function<Double, Double> function) {
        this.function = function;
        }
    
}
