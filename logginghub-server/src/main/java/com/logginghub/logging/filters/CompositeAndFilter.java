package com.logginghub.logging.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

/**
 * A composite AND filter. Add filters to it and it'll only pass if ALL of the
 * added filters pass the event.
 * 
 * @author admin
 * 
 */
public class CompositeAndFilter implements Filter<LogEvent> {
    
    
    private static final Logger logger = Logger.getLoggerFor(CompositeAndFilter.class);
    
    private List<Filter<LogEvent>> filters = new CopyOnWriteArrayList<Filter<LogEvent>>();

    public CompositeAndFilter() {
        
    }
    
    public CompositeAndFilter(Filter<LogEvent>... filters) {
        for (Filter<LogEvent> filter : filters) {
            addFilter(filter);
        }
    }

    public void addFilter(Filter<LogEvent> filter) {
        filters.add(filter);
    }

    public void removeFilter(Filter<LogEvent> filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public boolean passes(LogEvent event) {
        boolean passes = true;

        for (Filter<LogEvent> filter : filters) {
            passes &= filter.passes(event);
            logger.trace("Checked filter [{}] '{}' against event '{}'", passes, filter, event);

            if (!passes) {
                break;
            }
        }

        return passes;
    }

    public List<Filter<LogEvent>> getFilters() {
        return filters;
    }

    @Override public String toString() {
        return "CompositeAndFilter [filters=" + filters + "]";
    }

    
    
    
}
