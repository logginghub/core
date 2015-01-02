package com.logginghub.utils.observable;

import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.Xml.XmlEntry;
import com.logginghub.utils.logging.Logger;

public class ObservableProperty<T> extends AbstractObservableProperty<T> {

    private static final Logger logger = Logger.getLoggerFor(ObservableProperty.class);
    private T value;

    public ObservableProperty(T initialValue) {
        this.value = initialValue;
    }

    public ObservableProperty(T initialValue, Observable parent) {
        super(parent);
        this.value = initialValue;
    }

    public void set(T t) {

        if (logger.willLog(Logger.trace)) {
            if (getParent() != null) {
                logger.trace("Property of '{}' - '{}' changing to '{}'", getParent(), value, t);
            }
            else {
                logger.trace("Property '{}' changing tp '{}'", value, t);
            }
        }

        T old = this.value;
        this.value = t;
        if (old != null && old.equals(t)) {
            // The values were the same?
        }
        else {
            fireChanged(t, old);

            if (parent != null) {
                parent.onChildChanged(this);
            }
        }

    }

    public void setAndNotify(T t) {

        if (logger.willLog(Logger.trace)) {
            if (getParent() != null) {
                logger.trace("Property of '{}' - '{}' changing to '{}'", getParent(), value, t);
            }
            else {
                logger.trace("Property '{}' changing tp '{}'", value, t);
            }
        }

        T old = this.value;
        this.value = t;
        fireChanged(t, old);

        if (parent != null) {
            parent.onChildChanged(this);
        }

    }

    public void setQuietly(T t) {
        this.value = t;
    }

    public T get() {
        return value;
    }

    public String asString() {
        if (value == null) {
            return null;
        }
        else {
            return value.toString();
        }
    }

    public Long asLong() {
        if (value == null) {
            return 0L;
        }
        else {
            return (Long) value;
        }
    }

    public int asInt() {
        if (value == null) {
            return 0;
        }
        else {
            return (Integer) value;
        }
    }

    public double asDouble() {
        if (value == null) {
            return 0;
        }
        else {
            return (Double) value;
        }
    }

    /**
     * Best-efforts attempt to turn this value into a boolean. For boolean values its easy, for
     * strings 'true' (case insensitive) is the only thing that will return true. Numerical values
     * equal to zero will be false, everything else is true. Anything else will always return false.
     * 
     * @return
     */
    public boolean asBoolean() {
        boolean asBoolean;

        if (value != null) {
            if (value instanceof Boolean) {
                Boolean b = (Boolean) value;
                asBoolean = b.booleanValue();
            }
            else if (value instanceof String) {
                String string = (String) value;
                if (string.equalsIgnoreCase("true")) {
                    asBoolean = true;
                }
                else {
                    asBoolean = false;
                }
            }
            else if (value instanceof Number) {
                Number number = (Number) value;
                if (number.intValue() == 0) {
                    asBoolean = false;
                }
                else {
                    asBoolean = true;
                }
            }
            else {
                asBoolean = false;
            }
        }
        else {
            asBoolean = false;
        }

        return asBoolean;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        @SuppressWarnings("unchecked") ObservableProperty<T> other = (ObservableProperty<T>) obj;
        if (value == null) {
            if (other.value != null) return false;
        }
        else if (!value.equals(other.value)) return false;
        return true;
    }

    @Override public void fromXml(XmlEntry xml) {
        set((T) xml.getAttribute(getName()));
    }

    @Override public AbstractObservableProperty<?> duplicate() {

        AbstractObservableProperty<?> duplicate = null;

        Object cloneValue;

        if (value instanceof Cloneable) {
            Cloneable cloneable = (Cloneable) value;
            cloneValue = ReflectionUtils.invoke(value, "clone");
        }
        else if (value instanceof String) {
            String string = (String) value;
            cloneValue = new String(string);
        }
        else if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            cloneValue = b == Boolean.TRUE ? Boolean.TRUE : Boolean.FALSE;
        }
        else {
            throw new NotImplementedException("Dont know how to clone");
        }

        duplicate = new ObservableProperty<T>((T) cloneValue);
        duplicate.parent = null;
        duplicate.setName(getName());
        duplicate.setType(getType());
        
        return duplicate;
        
    }
}
