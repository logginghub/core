package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which contain the phrase in the sourceApplication field
 * 
 * @author James
 */
public class SourceContainsFilter implements Filter<LogEvent> {
    private String value;

    public SourceContainsFilter(String value) {
        this.value = value;
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        if (!passes && event.getSourceApplication() != null) {
            passes = event.getSourceApplication().contains(value);
        }

        return passes;
    }

}
