package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which occurred on or after the time provided.
 * 
 * @author James
 */
public class EventsAfterFilter implements Filter<LogEvent> {
    private long time;

    public EventsAfterFilter(long time) {
        this.time= time;
    }

    public boolean passes(LogEvent event) {
        long time = event.getOriginTime();
        boolean passes = time >= this.time;
        return passes;
    }

    public void setTime(long newValue) {
        this.time = newValue;
    }

}
