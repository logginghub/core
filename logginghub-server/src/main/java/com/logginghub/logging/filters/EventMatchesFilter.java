package com.logginghub.logging.filters;

import java.util.regex.Pattern;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which match a regular expression against
 * any of the string fields of the event.
 * 
 * @author James
 */
public class EventMatchesFilter implements Filter<LogEvent> {
    private String value;
    private Pattern pattern;

    public EventMatchesFilter(String regex) {
        setEventContainsString(regex);
    }

    public void setEventContainsString(String regex) {
        value = regex;
        pattern = Pattern.compile(regex);
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        if (!passes && event.getMessage() != null) {
            passes = pattern.matcher(event.getMessage()).find();
        }

        if (!passes && event.getThreadName() != null) {
            passes = pattern.matcher(event.getThreadName()).find();
        }

        if (!passes && event.getSourceApplication() != null) {
            passes = pattern.matcher(event.getSourceApplication()).find();
        }

        if (!passes && event.getSourceMethodName() != null) {
            passes = pattern.matcher(event.getSourceMethodName()).find();
        }

        if (!passes && event.getSourceHost() != null) {
            passes = pattern.matcher(event.getSourceHost()).find();
        }

        if (!passes && event.getSourceClassName() != null) {
            passes = pattern.matcher(event.getSourceClassName()).find();
        }

        if (!passes && event.getLoggerName() != null) {
            passes = pattern.matcher(event.getLoggerName()).find();
        }

        if (!passes && event.getLevelDescription() != null) {
            passes = pattern.matcher(event.getLevelDescription()).find();
        }

        if (!passes && event.getFormattedException() != null) {
            passes = pattern.matcher(event.getFormattedException()).find();
        }

        if (!passes && event.getFormattedObject() != null) {
            String[] formattedObject = event.getFormattedObject();
            for (int i = 0; i < formattedObject.length && !passes; i++) {
                passes = pattern.matcher(formattedObject[i]).find();
            }
        }

        return passes;
    }

    @Override public String toString() {
        return "EventMatchesFilter [value=" + value + ", pattern=" + pattern + "]";
    }
    
    

}
