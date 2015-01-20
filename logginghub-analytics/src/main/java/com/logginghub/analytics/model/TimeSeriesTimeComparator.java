package com.logginghub.analytics.model;

import java.util.Comparator;

import com.logginghub.utils.CompareUtils;

public class TimeSeriesTimeComparator implements Comparator<TimeSeriesDataPoint>{
    public int compare(TimeSeriesDataPoint o1, TimeSeriesDataPoint o2) {        
         return CompareUtils.compare(o1.getTime(), o2.getTime());
    }
}
