package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which contain the phrase in any of the string fields of the
 * event.
 * 
 * @author James
 */
public class EventContainsFilter implements Filter<LogEvent> {
    private String value;

    public EventContainsFilter(String value) {
        this.value = value;
    }

    public void setEventContainsString(String value) {
        this.value = value;
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        if (value.isEmpty()) {
            passes = true;
        }
        else {
            if (!passes && event.getMessage() != null) {
                passes = event.getMessage().contains(value);
            }

            if (!passes && event.getThreadName() != null) {
                passes = event.getThreadName().contains(value);
            }

            if (!passes && event.getSourceApplication() != null) {
                passes = event.getSourceApplication().contains(value);
            }

            if (!passes && event.getChannel() != null) {
                passes = event.getChannel().contains(value);
            }

            if (!passes && event.getSourceMethodName() != null) {
                passes = event.getSourceMethodName().contains(value);
            }

            if (!passes && event.getSourceHost() != null) {
                passes = event.getSourceHost().toString().contains(value);
            }

            if (!passes && event.getSourceClassName() != null) {
                passes = event.getSourceClassName().contains(value);
            }

            if (!passes && event.getLoggerName() != null) {
                passes = event.getLoggerName().contains(value);
            }

            if (!passes && event.getLevelDescription() != null) {
                passes = event.getLevelDescription().contains(value);
            }

            if (!passes && event.getFormattedException() != null) {
                passes = event.getFormattedException().contains(value);
            }

            if (!passes && event.getFormattedObject() != null) {
                String[] formattedObject = event.getFormattedObject();
                for (int i = 0; i < formattedObject.length && !passes; i++) {
                    String string = formattedObject[i];
                    if (string != null) {
                        passes = string.contains(value);
                    }
                }
            }
        }

        return passes;
    }

    @Override public String toString() {
        return "EventContainsFilter [value=" + value + "]";
    }

}
