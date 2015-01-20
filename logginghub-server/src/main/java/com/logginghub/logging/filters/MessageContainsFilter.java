package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events whose message contains the string passed
 * in at construction time.
 * 
 * @author James
 */
public class MessageContainsFilter implements Filter<LogEvent> {
    private String value;
    private boolean ignoreCase = true;

    public MessageContainsFilter() {}
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    public MessageContainsFilter(String value) {
        setMessageContainsString(value);
    }

    public boolean passes(LogEvent event) {
        String message = event.getMessage();
        boolean passes;

        if (ignoreCase) {
            passes = message.toLowerCase().contains(value);
        }
        else {
            passes = message.contains(value);
        }

        return passes;
    }

    public void setMessageContainsString(String text) {
        if (ignoreCase) {
            value = text.toLowerCase();
        }
        else {
            value = text;
        }
    }

}
