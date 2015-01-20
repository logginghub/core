package com.logginghub.analytics.demo;

import com.logginghub.analytics.AggregatedDataKey;

public abstract class DataFunction {

    private final String name;
    private final AggregatedDataKey key;

    public DataFunction(String name, AggregatedDataKey key) {
        this.name = name;
        this.key = key;        
    }
    
    public AggregatedDataKey getKey() {
        return key;
    }
    
    public abstract double function(double value);

    public String getName() {
        return name;
         
    } 

}
