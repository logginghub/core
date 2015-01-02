package com.logginghub.utils.filter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A composite OR filter. Add filters to it and it'll pass if ANY of the added filters pass the
 * event.
 * 
 * @author admin
 * 
 */
public class CompositeOrFilter<T> implements Filter<T> {
    private List<Filter<T>> filters = new CopyOnWriteArrayList<Filter<T>>();

    public CompositeOrFilter() {

    }

    public CompositeOrFilter(Filter<T>... filters) {
        for (Filter<T> filter : filters) {
            addFilter(filter);
        }
    }

    public void addFilter(Filter<T> filter) {
        filters.add(filter);
    }

    public boolean passes(T event) {
        boolean passes = false;

        for (Filter<T> filter : filters) {
            passes |= filter.passes(event);

            if (passes) {
                break;
            }
        }

        return passes;
    }

    public void removeFilter(Filter<T> filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public List<Filter<T>> getFilters() {
        return filters;
    }

    @Override public String toString() {
        return "CompositeOrFilter [filters=" + filters + "]";
    }

}
