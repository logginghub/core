package com.logginghub.utils;

public class Pointer<T> {
    public T value;

    public Pointer(T intialValue) {
        value = intialValue;
    }
    
    public String toString() {
        if (value == null) {
            return "null";
        }
        else {
            return value.toString();
        }
    }
}
