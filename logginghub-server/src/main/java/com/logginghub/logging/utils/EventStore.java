package com.logginghub.logging.utils;

import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.filter.Filter;

public interface EventStore {

    void add(LogEvent event);
    
    LogEvent get(int i);
    
    int getCount();
    
    void stream(Destination<LogEvent> destination);
    
    List<LogEvent> getAll();
    
    void applyFilter(Filter<LogEvent> filter);    
    void clearFilter();
    
    
}
