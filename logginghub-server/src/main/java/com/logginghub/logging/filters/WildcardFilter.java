package com.logginghub.logging.filters;

import java.util.Date;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which contain a string including wild cards in one of the
 * fields of the event.
 * 
 * @author James
 */
public class WildcardFilter implements Filter<LogEvent> {
    private String value;
    private String[] split;
    private EventField field;
    private boolean endingWildcard;
    private boolean startingWildcard;

    public enum EventField {
        Message,
        Source,
        Time,
        Level,
        Method,
        Thread,
        Host,
        Class,
        Exception
    }

    public WildcardFilter(String value, EventField field) {
        setValue(value);
        setField(field);
    }

    public boolean passes(LogEvent event) {
        String toCheck;

        switch (field) {
            case Host:
                toCheck = event.getSourceAddress();
                break;
            case Level:
                toCheck = event.getLevelDescription();
                break;
            case Message:
                toCheck = event.getMessage();
                break;
            case Class:
                toCheck = event.getSourceClassName();
                break;
            case Method:
                toCheck = event.getSourceMethodName();
                break;
            case Source:
                toCheck = event.getSourceApplication();
                break;
            case Thread:
                toCheck = event.getThreadName();
                break;
            case Time:
                toCheck = new Date(event.getOriginTime()).toString();
                break;
            case Exception:
                toCheck = event.getFormattedException();
                break;
            default:
                throw new RuntimeException(String.format("Unsupported event field '%s'", field));
        }

        boolean passes = wildcardCheck(toCheck);
        return passes;
    }

    public boolean wildcardCheck(String toCheck) {
        // Iterate over the cards.
        for (int i = 0; i < split.length; i++) {
            String portion = split[i];

            int idx = toCheck.indexOf(portion);

            if (i == 0 && !startingWildcard && idx != 0) {
                // This means its the first portion, the value hasn't started
                // with a wildcard, and the string was found some way into the
                // text. So this isn't a match.
                return false;
            }

            if (i == split.length && !endingWildcard && (idx + portion.length() != toCheck.length())) {
                // Ok this one means we've reached the end of the things we
                // needed to check but haven't reached the end of the string
                // yet, and thelast token wasn't a wildcard. So we haven't
                // matched.
                return false;
            }

            // Card not detected in the text.
            if (idx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            toCheck = toCheck.substring(idx + portion.length());
        }

        if (!toCheck.isEmpty() && !endingWildcard) {
            // We've still got some stuff on the end, and there is no ending
            // wildcard :(
            return false;
        }

        return true;
    }

    public EventField getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.split = value.split("\\*");
        this.startingWildcard = value.startsWith("*");
        this.endingWildcard = value.endsWith("*");
    }

    public void setField(EventField field) {
        this.field = field;
    }
}
