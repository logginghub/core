package com.logginghub.logging.valuegenerators;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.filter.Filter;

public abstract class FilteringValueGenerator<T> extends AbstractValueGenerator<T> implements LogEventListener {
    private Filter<LogEvent> filter;

    public FilteringValueGenerator(Filter<LogEvent> filter) {
        this.filter = filter;
    }

    public void onNewLogEvent(LogEvent event) {
        if (filter.passes(event)) {
            onNewFilteredValue(event);
        }
    }

    protected abstract void onNewFilteredValue(LogEvent event);
}
