package com.logginghub.utils;

/**
 * NOT THREAD SAFE.
 * 
 * @author James
 * 
 */
public class MutableInt {
    public volatile int value;

    public MutableInt(int value) {
        super();
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void increment() {
        value++;
    }

    public void increment(int amount) {
        value += amount;
    }

    @Override public String toString() {
        return Integer.toString(value);
    }

    public void reset() {
        value = 0;
    }

}
