package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which have a level greater than or equal to the filter value.
 * 
 * @author James
 */
public class LogEventLevelFilter implements Filter<LogEvent>{
    private int level;

    public LogEventLevelFilter(int level) {
        setLevel(level);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean passes(LogEvent event) {
        int level = event.getLevel();
        boolean passes = level >= this.level;
        return passes;
    }

    @Override public String toString() {
        return "LevelFilter [level=" + level + "]";
    }

}
