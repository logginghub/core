package com.logginghub.utils;

import java.util.Random;

public class LongRandomRange {

    private Random random = new Random();
    private long low;
    private long high;

    public LongRandomRange(long low, long high) {
        this.low = low;
        this.high = high;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
    
    public long getRandomValue() { 
        return (low + ((long)(random.nextDouble() * (high - low))));
    }

}
