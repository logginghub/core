package com.logginghub.utils;

public class IntegerStat implements Stat {

    private String name;
    private int value;
    private int lastValue = -1;

    // Incremental stats are important if they are greater than zero - for
    // example the number of new events in the hub. Non-incremental stats are
    // for things like connection counts, where you are interested in them if
    // they have changed.
    private boolean isIncremental = false;

    public IntegerStat(String name, int value) {
        super();
        this.name = name;
        this.value = value;
    }

    public IntegerStat() {}
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getLastValue() {
        return lastValue;
    }

    public void setLastValue(int lastValue) {
        this.lastValue = lastValue;
    }

    public void increment(int value) {
        this.value += value;
    }

    public void reset() {
        lastValue = value;
        if (isIncremental) {
            value = 0;
        }
    }

    public boolean hasChanged() {
        if (isIncremental) {
            return value > 0;
        }
        else {
            return value != lastValue;
        }
    }

    public void increment() {
        increment(1);
    }

    public void decrement() {
        decrement(1);
    }

    private void decrement(int value) {
        this.value -= value;
    }

    public void setIncremental(boolean isIncremental) {
        this.isIncremental = isIncremental;
    }

    public int getDeltaValue() {
        int delta = value - lastValue;
        value = lastValue;
        return delta;
    }

}
