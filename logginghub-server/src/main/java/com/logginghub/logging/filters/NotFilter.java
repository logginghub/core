package com.logginghub.logging.filters;

import com.logginghub.utils.filter.Filter;

/**
 * A basic NOT filter. Takes the passesFilter value from the decorated filter and returns the
 * opposite.
 * 
 * @author admin
 * 
 */
public class NotFilter<T> implements Filter<T> {
    private final Filter<T> filter;

    public NotFilter(Filter<T> filter) {
        this.filter = filter;
    }

    public boolean passes(T event) {
        boolean passes = !filter.passes(event);
        return passes;
    }

    public Filter<T> getFilter() {
        return filter;
    }
}
