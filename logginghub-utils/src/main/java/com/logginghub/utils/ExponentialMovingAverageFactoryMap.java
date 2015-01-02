package com.logginghub.utils;

public class ExponentialMovingAverageFactoryMap extends FactoryMap<String, ExponentialMovingAverage>{
    private static final long serialVersionUID = 1L;
    
    private int dataPoints;
    
    public ExponentialMovingAverageFactoryMap(int dataPoints) {
        super();
        this.dataPoints = dataPoints;
    }

    @Override protected ExponentialMovingAverage createEmptyValue(String key) {
        return new ExponentialMovingAverage(dataPoints);
    }

}
