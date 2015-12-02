package com.logginghub.utils;

public class LongStat implements Stat<Long> {

    private String name;
    private volatile long total;
    private volatile long value;
    private volatile long lastValue = -1;

    // Incremental stats are important if they are greater than zero - for
    // example the number of new events in the hub. Non-incremental stats are
    // for things like connection counts, where you are interested in them if
    // they have changed.
    private boolean isIncremental = false;

    public LongStat(String name, int value) {
        super();
        this.name = name;
        this.value = value;
    }

    public LongStat() {}
    
    public String getName() {
        return name;
    }

    public void set(long value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getLastValue() {
        return lastValue;
    }

    public void setLastValue(int lastValue) {
        this.lastValue = lastValue;
    }

    public void increment(int value) {
        this.value += value; this.total += value;
    }

    public void reset() {
        lastValue = value;
        if (isIncremental) {
            value = 0;
        }
    }

    public long getTotal() {
        return total;
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

    public long getDeltaValue() {
        long delta = value - lastValue;
        value = lastValue;
        return delta;
    }

}
