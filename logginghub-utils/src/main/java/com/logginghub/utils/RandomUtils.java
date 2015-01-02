package com.logginghub.utils;

import java.util.Random;

public class RandomUtils {

    private static Random random = new Random();
    
    public static long between(long min, long max) {
        long value = (min + (long) ((max - min)* random.nextDouble()));
        return value;
    }
    
}
