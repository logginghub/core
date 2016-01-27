package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.filter.Filter;

import java.util.Map;

public class TimeFieldFilter implements Filter<LogEvent> {

    private Field field;
    private Type type;
    private long value = ACCEPT_ALL;
    private String metdataField;

    public final static long ACCEPT_ALL = -1;

    public TimeFieldFilter(Field field, Type type, long value) {
        this.field = field;
        this.type = type;
        this.value = value;
    }

    public TimeFieldFilter(String metdataField, Type type, long value) {
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public boolean passes(LogEvent logEvent) {

        long comparisonValue;

        if(value == ACCEPT_ALL) {
            return true;
        }

        switch (field) {
            case HubTime: {
                comparisonValue = logEvent.getHubTime();
                break;
            }
            case Metadata: {
                Map<String, String> metadata = logEvent.getMetadata();
                if (metadata != null) {
                    String metadataValue = metadata.get(metdataField);
                    comparisonValue = Long.parseLong(metadataValue);
                    break;
                } else {
                    // This is tricky - the event didn't have the metadata field - so do we display it or not? Lets try no.
                    return false;
                }
            }
            case OriginTime: {
                comparisonValue = logEvent.getOriginTime();
                break;
            }
            case SequenceNumber: {
                comparisonValue = logEvent.getSequenceNumber();
                break;
            }
            default: {
                throw new FormattedRuntimeException("Unsupported field '{}'", field);
            }
        }

        boolean passes;
        switch (type) {
            case GreaterThan: {
                passes = comparisonValue > value;
                break;
            }
            case GreaterThanOrEquals: {
                passes = comparisonValue >= value;
                break;
            }
            case LessThan: {
                passes = comparisonValue < value;
                break;
            }
            case LessThanOrEquals: {
                passes = comparisonValue <= value;
                break;
            }
            default: {
                throw new FormattedRuntimeException("Unsupported comparison type '{}'", type);
            }
        }

        return passes;
    }

    public enum Type {
        GreaterThan, LessThan, GreaterThanOrEquals, LessThanOrEquals
    }

    public enum Field {
        HubTime, Metadata, OriginTime, SequenceNumber
    }
}
