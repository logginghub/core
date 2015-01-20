package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which occurred between the start and end
 * times. The start time is inclusive, and the end time is exclusive.
 * 
 * @author James
 */
public class EventBetweenFilter implements Filter<LogEvent> {
    private long start;
    private long end;

    public EventBetweenFilter(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public boolean passes(LogEvent event) {
        long time = event.getOriginTime();
        boolean passes = time >= start && time < end;
        return passes;
    }

}
