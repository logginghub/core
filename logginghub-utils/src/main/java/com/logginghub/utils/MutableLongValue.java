package com.logginghub.utils;

/**
 * A mutable integer, with a string to tell you what the hell it represents.
 * 
 * @author James
 * 
 */
public class MutableLongValue implements Comparable<MutableLongValue> {
    public String key;
    public long value;

    public MutableLongValue(String key, long initialValue) {
        this.key = key;
        this.value = initialValue;
    }

    public void increment(long increment) {
        this.value += increment;
    }

    public int compareTo(MutableLongValue o) {
        return CompareUtils.compare(this.value, o.value);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MutableLongValue other = (MutableLongValue) obj;
        if (key == null) {
            if (other.key != null) return false;
        }
        else if (!key.equals(other.key)) return false;
        if (value != other.value) return false;
        return true;
    }

    @Override public String toString() {
        return "MutableLongValue [key=" + key + ", value=" + value + "]";
    }

}
