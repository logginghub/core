package com.logginghub.logging;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StringMatcher;
import com.logginghub.utils.StringMatcherFactory;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringMatcherFactory.MatcherType;
import com.logginghub.utils.filter.Filter;

public class LogEventFilterFactory {

    public enum LogField {
        SourceClassName,
        SourceMethodName,
        Message,
        ThreadName,
        LoggerName,
        SourceHost,
        SourceAddress,
        SourceApplication,
        FormattedException,
        FormattedObject,
        Channel
    }

    public static Filter<LogEvent> createFilterForField(LogField field, MatcherType type, String filterText) {

        final StringMatcher matcher = StringMatcherFactory.createMatcher(type, filterText);
        Filter<LogEvent> filter;

        switch (field) {
            case Channel: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getChannel());
                    }
                };
                break;
            }
            case FormattedException: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getFormattedException());
                    }
                };
                break;
            }
            case LoggerName: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getLoggerName());
                    }
                };
                break;
            }
            case Message: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getMessage());
                    }
                };
                break;
            }
            case SourceAddress: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getSourceAddress());
                    }
                };
                break;
            }
            case SourceApplication: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getSourceApplication());
                    }
                };
                break;
            }
            case SourceClassName: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getSourceClassName());
                    }
                };
                break;
            }
            case SourceMethodName: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getSourceMethodName());
                    }
                };
                break;
            }
            case ThreadName: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getThreadName());
                    }
                };
                break;
            }
            case SourceHost: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        return matcher.matches(event.getSourceHost());
                    }
                };
                break;
            }
            case FormattedObject: {
                filter = new Filter<LogEvent>() {
                    @Override public boolean passes(LogEvent event) {
                        String[] formattedObject = event.getFormattedObject();
                        boolean match = false;
                        if (formattedObject != null) {
                            for (String string : formattedObject) {
                                match = matcher.matches(string);
                                if (match) {
                                    break;
                                }
                            }
                        }
                        return match;
                    }
                };
                break;
            }

            default: {
                throw new IllegalArgumentException(StringUtils.format("Unknown field '{}'", field));
            }
        }

        return filter;

    }

}
