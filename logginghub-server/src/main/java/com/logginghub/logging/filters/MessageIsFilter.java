package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events whose message matches exactly the string passed in at
 * construction time.
 * 
 * @author James
 */
public class MessageIsFilter implements Filter<LogEvent> {
    private String value;
    private boolean ignoreCase = true;

    public MessageIsFilter(String value) {
        setMessageString(value);
    }

    public boolean passes(LogEvent event) {
        String message = event.getMessage();
        boolean passes;

        if (ignoreCase) {
            passes = message.toLowerCase().equals(value);
        }
        else {
            passes = message.equals(value);
        }

        return passes;
    }

    public void setMessageString(String text) {
        if (ignoreCase) {
            value = text.toLowerCase();
        }
        else {
            value = text;
        }
    }

}
