package com.logginghub.logging.filters;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A composite OR filter. Add filters to it and it'll pass if ANY of the added filters pass the
 * event.
 * 
 * @author admin
 * 
 */
public class CompositeOrFilter implements Filter<LogEvent> {
    private List<Filter<LogEvent>> filters = new CopyOnWriteArrayList<Filter<LogEvent>>();

    public void addFilter(Filter<LogEvent> filter) {
        filters.add(filter);
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        for (Filter<LogEvent> filter : filters) {
            passes |= filter.passes(event);

            if (passes) {
                break;
            }
        }

        // jshaw - this must return false if there are no filters or things will break!!
        return passes;
    }
}
