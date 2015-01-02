package com.logginghub.utils;

public class MutableLong {
    
    public long value;
    
    public MutableLong(long initialValue) {
        this.value = initialValue;
    }
    
    public MutableLong() {
     
    }

    public void min(long value) {
        this.value = Math.min(value, this.value);
    }
    
    public void max(long value) {
        this.value = Math.max(value, this.value);
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
