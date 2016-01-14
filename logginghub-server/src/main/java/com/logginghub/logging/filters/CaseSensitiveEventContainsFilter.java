package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.filter.Filter;

/**
 * A Filter<LogEvent> that passes events which contain the phrase in any of the string fields of the
 * event.
 * 
 * @author James
 */
public class CaseSensitiveEventContainsFilter implements Filter<LogEvent> {

    private String value;

    public CaseSensitiveEventContainsFilter(String value) {
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

            final String message = event.getMessage();
            if (message != null) {
                passes = message.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String threadName = event.getThreadName();
            if (threadName != null) {
                passes = threadName.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String sourceApplication = event.getSourceApplication();
            if (sourceApplication != null) {
                passes = sourceApplication.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String channel = event.getChannel();
            if (channel != null) {
                passes = channel.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String sourceMethodName = event.getSourceMethodName();
            if (sourceMethodName != null) {
                passes = sourceMethodName.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String sourceHost = event.getSourceHost();
            if (sourceHost != null) {
                passes = sourceHost.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String sourceClassName = event.getSourceClassName();
            if (sourceClassName != null) {
                passes = sourceClassName.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String loggerName = event.getLoggerName();
            if (loggerName != null) {
                passes = loggerName.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String levelDescription = event.getLevelDescription();
            if (levelDescription != null) {
                passes = levelDescription.contains(value);
                if(passes) {
                    return passes;
                }
            }

            final String formattedException = event.getFormattedException();
            if (formattedException != null) {
                passes = event.getFormattedException().contains(value);
                if(passes) {
                    return passes;
                }
            }

            if (event.getFormattedObject() != null) {
                String[] formattedObject = event.getFormattedObject();
                for (int i = 0; i < formattedObject.length && !passes; i++) {
                    String string = formattedObject[i];
                    if (string != null) {
                        passes = string.contains(value);
                        if(passes) {
                            return passes;
                        }
                    }
                }
            }

            if (!passes && event.getMetadata() != null) {
                for (String string : event.getMetadata().values()) {
                    if (string != null) {
                        passes = StringUtils.indexOfIgnoreCase(string, value) != -1;
                        if(passes) {
                            break;
                        }
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
