package com.logginghub.utils;

public class MutableBoolean {
    public volatile boolean value;
    
    public MutableBoolean(boolean value) {
        this.value = value;
    }

    public void setValue(boolean b) {
        this.value = b;
    }
    
    public boolean getValue() {
        return value;
    }

    public Boolean booleanValue() {
        return getValue();
    }
}
