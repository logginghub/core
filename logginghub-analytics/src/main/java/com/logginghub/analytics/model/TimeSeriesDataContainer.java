package com.logginghub.analytics.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TimeSeriesDataContainer {

    private Map<String, TimeSeriesData> series = new HashMap<String, TimeSeriesData>();
    
    public Set<String> getSeriesNames() {
        return series.keySet();
    }
    
    public int getSeriesCount(){
        return series.size();
    }
    
    public void add(String seriesName, TimeSeriesData data){
        series.put(seriesName, data);
    }
    
    public TimeSeriesData getSeries(String seriesName){
        return series.get(seriesName);
    }
    
    public Collection<TimeSeriesData> getSeries(){
        return series.values();
    }

    public int size() {
        return series.size();
         
    }
}
