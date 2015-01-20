package com.logginghub.logging.modules;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.filter.Filter;

public class PatternisedLogEventLevelFilter implements Filter<PatternisedLogEvent> {

    private int level;

    public PatternisedLogEventLevelFilter(int levelFilter) {
        this.level = levelFilter;
    }

    @Override public boolean passes(PatternisedLogEvent event) {
        int level = event.getLevel();
        boolean passes = level >= this.level;
        return passes;
    }

    @Override public String toString() {
        return "PatternisedLogEventLevelFilter [level=" + level + "]";
    }

    
}
