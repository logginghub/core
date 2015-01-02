package com.logginghub.utils.filter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A composite AND filter. Add filters to it and it'll only pass if ALL of the
 * added filters pass the event.
 * 
 * @author admin
 * 
 */
public class CompositeAndFilter<T> implements Filter<T> {
    
    
    private List<Filter<T>> filters = new CopyOnWriteArrayList<Filter<T>>();

    public CompositeAndFilter() {
        
    }
    
    public CompositeAndFilter(Filter<T>... filters) {
        for (Filter<T> filter : filters) {
            addFilter(filter);
        }
    }

    public void addFilter(Filter<T> filter) {
        filters.add(filter);
    }

    public void removeFilter(Filter<T> filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public boolean passes(T event) {
        boolean passes = true;

        for (Filter<T> filter : filters) {
            passes &= filter.passes(event);

            if (!passes) {
                break;
            }
        }

        return passes;
    }

    public List<Filter<T>> getFilters() {
        return filters;
    }

    @Override public String toString() {
        return "CompositeAndFilter [filters=" + filters + "]";
    }

    
    
}
