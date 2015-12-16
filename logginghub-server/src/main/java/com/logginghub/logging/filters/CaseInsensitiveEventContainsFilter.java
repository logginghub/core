package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which contain the phrase in any of the string fields of the event.
 *
 * @author James
 */
public class CaseInsensitiveEventContainsFilter implements Filter<LogEvent> {

    private String value;

    public CaseInsensitiveEventContainsFilter(String value) {
        this.value = value;
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        if (value.isEmpty()) {
            passes = true;
        } else {
            final String message = event.getMessage();
            if (!passes && message != null) {
                passes = StringUtils.indexOfIgnoreCase(message, value) != -1;
            }

            final String threadName = event.getThreadName();
            if (!passes && threadName != null) {
                passes = StringUtils.indexOfIgnoreCase(threadName, value) != -1;
            }

            final String sourceApplication = event.getSourceApplication();
            if (!passes && sourceApplication != null) {
                passes = StringUtils.indexOfIgnoreCase(sourceApplication, value) != -1;
            }

            final String channel = event.getChannel();
            if (!passes && channel != null) {
                passes = StringUtils.indexOfIgnoreCase(channel, value) != -1;
            }

            final String sourceMethodName = event.getSourceMethodName();
            if (!passes && sourceMethodName != null) {
                passes = StringUtils.indexOfIgnoreCase(sourceMethodName, value) != -1;
            }

            final String sourceHost = event.getSourceHost();
            if (!passes && sourceHost != null) {
                passes = StringUtils.indexOfIgnoreCase(sourceHost, value) != -1;
            }

            final String sourceClassName = event.getSourceClassName();
            if (!passes && sourceClassName != null) {
                passes = StringUtils.indexOfIgnoreCase(sourceClassName, value) != -1;
            }

            final String loggerName = event.getLoggerName();
            if (!passes && loggerName != null) {
                passes = StringUtils.indexOfIgnoreCase(loggerName, value) != -1;
            }

            final String levelDescription = event.getLevelDescription();
            if (!passes && levelDescription != null) {
                passes = StringUtils.indexOfIgnoreCase(levelDescription, value) != -1;
            }

            final String formattedException = event.getFormattedException();
            if (!passes && formattedException != null) {
                passes = StringUtils.indexOfIgnoreCase(formattedException, value) != -1;
            }

            if (!passes && event.getFormattedObject() != null) {
                String[] formattedObject = event.getFormattedObject();
                for (int i = 0; i < formattedObject.length && !passes; i++) {
                    String string = formattedObject[i];
                    if (string != null) {
                        passes = StringUtils.indexOfIgnoreCase(string, value) != -1;
                    }
                }
            }
        }

        return passes;
    }

    public void setEventContainsString(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EventContainsFilter [value=" + value + "]";
    }

}
