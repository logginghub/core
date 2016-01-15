package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import java.util.Map;

/**
 * Created by james on 15/01/2016.
 */
public class FieldFilter implements Filter<LogEvent> {

    private Field field;
    private Type type;
    private String value;
    private String metdataField;
    private boolean caseSensitive;

    public FieldFilter(Field field, Type type, String value, boolean caseSensitive) {
        this.field = field;
        this.type = type;
        this.value = value;
        this.caseSensitive = caseSensitive;
    }

    public FieldFilter(String metdataField, Type type, String value, boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        this.field = Field.Metadata;
        this.metdataField = metdataField;
        this.type = type;
        this.value = value;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getMetdataField() {
        return metdataField;
    }

    public void setMetdataField(String metdataField) {
        this.metdataField = metdataField;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean passes(LogEvent logEvent) {

        if(value.isEmpty()) {
            return true;
        }

        switch (field) {
            case Channel: {
                return passes(logEvent.getChannel());
            }
            case Flavour: {
                return passes(logEvent.getFlavour());
            }
            case FormattedException: {
                return passes(logEvent.getFormattedException());
            }
            case FormattedObject: {
                return passes(logEvent.getFormattedObject());
            }
            case HubTime: {
                return passesTime(logEvent.getHubTime());
            }
            case Level: {
                return passesLevel(logEvent.getLevel());
            }
            case LoggerName: {
                return passes(logEvent.getLoggerName());
            }
            case Message: {
                return passes(logEvent.getMessage());
            }
            case Metadata: {
                Map<String, String> metadata = logEvent.getMetadata();
                if (metadata != null) {
                    String metadataValue = metadata.get(metdataField);
                        return passes(metadataValue);
                } else {
                    return false;
                }
            }
            case OriginTime: {
                return passesTime(logEvent.getOriginTime());
            }
            case PID: {
                return passes(Integer.toString(logEvent.getPid()));
            }
            case SequenceNumber: {
                return passes(Long.toString(logEvent.getSequenceNumber()));
            }
            case SourceAddress: {
                return passes(logEvent.getSourceAddress());
            }
            case SourceApplication: {
                return passes(logEvent.getSourceApplication());
            }
            case SourceClass: {
                return passes(logEvent.getSourceClassName());
            }
            case SourceMethod: {
                return passes(logEvent.getSourceMethodName());
            }
            case SourceHost: {
                return passes(logEvent.getSourceHost());
            }
            case ThreadName: {
                return passes(logEvent.getThreadName());
            }
        }

        return false;
    }

    private boolean passes(String target) {
        if (target == null) {
            return false;
        }

        switch (type) {
            case Contains: {
                if (caseSensitive) {
                    return target.contains(value);
                } else {
                    return target.toLowerCase().contains(value.toLowerCase());
                }
            }
            case EndsWith: {
                if (caseSensitive) {
                    return target.endsWith(value);
                } else {
                    return target.toLowerCase().endsWith(value.toLowerCase());
                }
            }
            case Equals: {
                if (caseSensitive) {
                    return target.equals(value);
                } else {
                    return target.equalsIgnoreCase(value.toLowerCase());
                }
            }
            case Regex: {
                return StringUtils.matches(target, value);
            }
            case StartsWith: {
                if (caseSensitive) {
                    return target.startsWith(value);
                } else {
                    return target.toLowerCase().startsWith(value.toLowerCase());
                }
            }
            default: {
                throw new FormattedRuntimeException("Unsupported comparison type '{}'", type);
            }
        }
    }

    private boolean passes(String[] target) {
        if (target == null) {
            return false;
        }

        for (String s : target) {
            if (passes(s)) {
                return true;
            }
        }

        return false;
    }

    private boolean passesTime(long time) {

        String number = Long.toString(time);
        if (passes(number)) {
            return true;
        }

        String asDate = Logger.toDateString(time).toString();
        if (passes(asDate)) {
            return true;
        }

        return false;
    }

    private boolean passesLevel(int level) {

        String number = Integer.toString(level);
        if (passes(number)) {
            return true;
        }

        String asJavaLevel = Logger.getLevelName(level, false);
        if (passes(asJavaLevel)) {
            return true;
        }

        return false;

    }

    public enum Type {
        Contains, Equals, StartsWith, EndsWith, Regex
    }

    public enum Field {
        Channel, Flavour, FormattedException, FormattedObject, HubTime, Level, LoggerName, Message, Metadata, OriginTime, PID, SequenceNumber, SourceAddress, SourceApplication, SourceClass, SourceMethod, SourceHost, ThreadName
    }
}
