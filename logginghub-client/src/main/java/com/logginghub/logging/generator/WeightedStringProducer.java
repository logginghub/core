package com.logginghub.logging.generator;

import java.util.Random;

public class WeightedStringProducer implements StringProducer {

    private String[] strings;
    private Random random = new Random();
    private double[] weightings;

    private double totalWeighting;

    public WeightedStringProducer(String[] strings, double[] weightings) {
        this.strings = strings;
        this.weightings = weightings;

        totalWeighting = 0;
        for (double d : weightings) {
            totalWeighting += d;
        }

    }

    public String produce() {

        // Create the random number
        double nextDouble = random.nextDouble();

        // This is the distance through the weighting array to travel
        double distance = nextDouble * totalWeighting;

        String value = null;

        double travelled = 0;
        for (int i = 0; i < weightings.length; i++) {
            travelled += weightings[i];
            if (travelled > distance) {
                value = strings[i];
                break;
            }
        }
        
        if(value == null) {
            // Must be the last one
            value = strings[strings.length - 1];
        }

        return value;
    }

}
