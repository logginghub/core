package com.logginghub.utils;

/**
 * A mutable integer, with a string to tell you what the hell it represents.
 * 
 * @author James
 * 
 */
public class MutableIntegerValue implements Comparable<MutableIntegerValue> {
    public String key;
    public int value;

    public MutableIntegerValue(String key, int initialValue) {
        this.key = key;
        this.value = initialValue;
    }

    public void increment(int increment) {
        this.value += increment;
    }

    public int compareTo(MutableIntegerValue o) {
        return CompareUtils.compare(this.value, o.value);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + value;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MutableIntegerValue other = (MutableIntegerValue) obj;
        if (key == null) {
            if (other.key != null) return false;
        }
        else if (!key.equals(other.key)) return false;
        if (value != other.value) return false;
        return true;
    }

    @Override public String toString() {
        return "MutableIntegerValue [key=" + key + ", value=" + value + "]";
    }

}
