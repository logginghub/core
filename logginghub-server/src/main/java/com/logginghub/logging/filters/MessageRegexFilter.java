package com.logginghub.logging.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which matches a regular expression
 * against the message field of the event. The regex must match the whole string
 * completely for it to pass.
 * 
 * @author James
 */
public class MessageRegexFilter implements Filter<LogEvent> {
    private Pattern pattern;
    private Matcher matcher;

    public MessageRegexFilter(String regex) {
        setMessageMatchesRegex(regex);
    }

    public void setMessageMatchesRegex(String regex) {
        pattern = Pattern.compile(regex);
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        if (!passes && event.getMessage() != null) {
            matcher = pattern.matcher(event.getMessage());
            passes = matcher.matches();
        }

        return passes;
    }

    public Matcher getLastMatcher() {
        return matcher;
    }

}
