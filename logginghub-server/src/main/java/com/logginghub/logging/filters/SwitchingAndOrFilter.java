package com.logginghub.logging.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

/**
 * Composite filter that can be easily switched from AND to OR mode.
 */
public class SwitchingAndOrFilter implements Filter<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(SwitchingAndOrFilter.class);

    private List<Filter<LogEvent>> filters = new CopyOnWriteArrayList<Filter<LogEvent>>();
    private boolean applyAndLogic = true;

    public SwitchingAndOrFilter() {

    }

    public SwitchingAndOrFilter(Filter<LogEvent>... filters) {
        for (Filter<LogEvent> filter : filters) {
            addFilter(filter);
        }
    }

    public void setApplyAndLogic(boolean applyAndLogic) {
        this.applyAndLogic = applyAndLogic;
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

        boolean passes;
        if (applyAndLogic) {
            passes = true;
        }
        else {
            passes = false;
        }

        for (Filter<LogEvent> filter : filters) {
            if (applyAndLogic) {
                passes &= filter.passes(event);
            }
            else {
                passes |= filter.passes(event);
            }
            logger.trace("Checked filter [{}] '{}' against event '{}'", passes, filter, event);

            if (applyAndLogic && !passes) {
                break;
            }
            else if (!applyAndLogic && passes) {
                break;
            }
        }

        return passes;
    }

    public List<Filter<LogEvent>> getFilters() {
        return filters;
    }

    @Override public String toString() {
        return "SwitchingAndOrFilter [filters=" + filters + ", applyAndLogic=" + applyAndLogic + "]";
    }

}
