package com.logginghub.utils;

public abstract class BaseStat implements Stat {

    private String name;
    private int lastValue = -1;

    private boolean isIncremental = false;

    public BaseStat(String name) {
        super();
        this.name = name;
    }

    public BaseStat() {}
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract int getValue();

    public int getLastValue() {
        return lastValue;
    }

    public void setLastValue(int lastValue) {
        this.lastValue = lastValue;
    }

    public void reset() {
    
    }

    public boolean hasChanged() {
        if (isIncremental) {
            return getValue() > 0;
        }
        else {
            return getValue() != lastValue;
        }
    }

    public void setIncremental(boolean isIncremental) {
        this.isIncremental = isIncremental;
    }



}
