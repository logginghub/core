package com.logginghub.utils;

public class MovingAverageFactoryMap extends FactoryMap<String, MovingAverage>{
    private static final long serialVersionUID = 1L;
    
    private int dataPoints;
    
    public MovingAverageFactoryMap(int dataPoints) {
        super();
        this.dataPoints = dataPoints;
    }

    @Override protected MovingAverage createEmptyValue(String key) {
        return new MovingAverage(dataPoints);
    }

}
