package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which occurred before (but not on) the time provided.
 * 
 * @author James
 */
public class EventsBeforeFilter implements Filter<LogEvent> {
    private long time;

    public EventsBeforeFilter(long time) {
        this.time= time;
    }

    public boolean passes(LogEvent event) {
        long time = event.getOriginTime();
        boolean passes = time < this.time;
        return passes;        
    }
    
    public void setTime(long newValue) {
        this.time = newValue;
    }

}
