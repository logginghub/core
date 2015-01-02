package com.logginghub.utils;

import java.util.Random;

public class RandomRange {

    private Random random = new Random();
    private double low;
    private double high;

    public RandomRange(double low, double high) {
        setRange(low, high);        
    }

    public void setRandom(Random random) {
        this.random = random;
    }
    
    public double getRandomValue() { 
        return low + (random.nextDouble() * (high - low));
    }

    public void setRange(double low, double high) {
        this.low = low;
        this.high = high;
    }

}
