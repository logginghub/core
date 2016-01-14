package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.filter.Filter;

import java.lang.reflect.Field;

/**
 * A Filter<LogEvent> that passes events which contain the phrase in any of the string fields of the event.
 *
 * @author James
 */
public class CaseInsensitiveAsciiEventContainsFilter implements Filter<LogEvent> {

    private final char[] characters;
    private final Field accessField;
    private String value;

    public CaseInsensitiveAsciiEventContainsFilter(String value) {
        this.value = value;
        this.characters = ReflectionUtils.getFieldRuntime("value", value);
        accessField = ReflectionUtils.findField("value", String.class);
    }

    public boolean passes(LogEvent event) {
        boolean passes = false;

        try {

            if (value.isEmpty()) {
                passes = true;
            } else {
                final String message = event.getMessage();
                if (message != null) {
                    char[] characterData = (char[]) accessField.get(message);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String threadName = event.getThreadName();
                if (threadName != null) {
                    char[] characterData = (char[]) accessField.get(threadName);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String sourceApplication = event.getSourceApplication();
                if (sourceApplication != null) {
                    char[] characterData = (char[]) accessField.get(sourceApplication);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String channel = event.getChannel();
                if (channel != null) {
                    char[] characterData = (char[]) accessField.get(channel);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String sourceMethodName = event.getSourceMethodName();
                if (sourceMethodName != null) {
                    char[] characterData = (char[]) accessField.get(sourceMethodName);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String sourceHost = event.getSourceHost();
                if (sourceHost != null) {
                    char[] characterData = (char[]) accessField.get(sourceHost);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String sourceClassName = event.getSourceClassName();
                if (sourceClassName != null) {
                    char[] characterData = (char[]) accessField.get(sourceClassName);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String loggerName = event.getLoggerName();
                if (loggerName != null) {
                    char[] characterData = (char[]) accessField.get(loggerName);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String levelDescription = event.getLevelDescription();
                if (levelDescription != null) {
                    char[] characterData = (char[]) accessField.get(levelDescription);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                final String formattedException = event.getFormattedException();
                if (formattedException != null) {
                    char[] characterData = (char[]) accessField.get(formattedException);
                    passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                    if (passes) {
                        return passes;
                    }
                }

                if (event.getFormattedObject() != null) {
                    String[] formattedObject = event.getFormattedObject();
                    for (int i = 0; i < formattedObject.length && !passes; i++) {
                        String string = formattedObject[i];
                        if (string != null) {
                            char[] characterData = (char[]) accessField.get(string);
                            passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                            if (passes) {
                                return passes;
                            }
                        }
                    }
                }

                if (event.getMetadata() != null) {
                    for (String value : event.getMetadata().values()) {
                        char[] characterData = (char[]) accessField.get(value);
                        passes = StringUtils.indexOfIgnoreCase(characterData, characters) != -1;
                        if (passes) {
                            return passes;
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
