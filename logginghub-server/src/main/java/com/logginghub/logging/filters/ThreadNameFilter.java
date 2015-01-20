package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

public class ThreadNameFilter implements Filter<LogEvent> {
    private String value;

    public ThreadNameFilter(String value) {
        this.value = value;
    }

    public boolean passes(LogEvent event) {
        String threadName = event.getThreadName();
        boolean passes;

        if (threadName.contains(value)) {
            passes = true;
        }
        else {
            passes = false;
        }

        return passes;
    }

}
