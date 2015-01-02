package com.logginghub.utils.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.utils.FormattedRuntimeException;

public class Bag {
    private Map<String, Object> contents = new HashMap<String, Object>();
    private Object value;
    private boolean hasValue;

    public void setValue(Object value) {
        this.value = value;
        this.hasValue = true;
    }

    public Object getValue() {
        return value;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public Object get(String key) {
        return contents.get(key);
    }

    public String getString(String string) {
        Object object = contents.get(string);
        if (object instanceof String) {
            return (String) object;
        }
        else {
            return object.toString();
        }
    }

    public Integer getInteger(String string) {
        Object object = contents.get(string);
        if (object instanceof Integer) {
            return (Integer) object;
        }
        else if (object instanceof Number) {
            Number number = (Number) object;
            return number.intValue();
        }
        else {
            String stringValue = getString(string);
            return Integer.parseInt(stringValue);
        }
    }

    public String[] getStringArray(String string) {
        Object object = contents.get(string);
        if (object instanceof String[]) {
            return (String[]) object;
        }
        else {
            throw new FormattedRuntimeException("Bag element '{}' couldn't be converted into a string array - its value was '{}'", string, object);
        }
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contents == null) ? 0 : contents.hashCode());
        result = prime * result + (hasValue ? 1231 : 1237);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Bag other = (Bag) obj;

        if (contents == null) {
            if (other.contents != null) {
                return false;
            }
        }        
        
        if(!contents.equals(other.contents)) {
            return false;
        }
        
        if (hasValue != other.hasValue) {
            return false;
        }
        
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        }
        
        if (value.getClass().isArray() && other.value.getClass().isArray()) {
            return Arrays.equals((Object[])value, (Object[])other.value);
        }else{
            return value.equals(other.value);
        }
    }

    @Override public String toString() {
        return "Bag [value=" + value + ", hasValue=" + hasValue + ", contents=" + contents.toString() + "]";
    }

    public Set<String> keySet() {
        return contents.keySet();
    }

    public void put(String key, Object value) {
        contents.put(key, value);
    }
    
    public Map<String, Object> getContents() {
        return contents;
    }

    public List<String> getSortedKeyList() {        
        List<String> keys = new ArrayList<String>(keySet());
        Collections.sort(keys);
        return keys;         
    }

}
