package com.logginghub.utils;


public abstract class BaseStat<ReturnType extends Number> implements Stat<ReturnType> {

    private String name;
    private ReturnType lastValue = null;

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

    public abstract ReturnType getValue();

    public ReturnType getLastValue() {
        return lastValue;
    }

    public void setLastValue(ReturnType lastValue) {
        this.lastValue = lastValue;
    }

    public void reset() {
    
    }

    public boolean hasChanged() {
        if (isIncremental) {
            return getValue().doubleValue() > 0;
        }
        else {
            return !getValue().equals(lastValue);
        }
    }

    public void setIncremental(boolean isIncremental) {
        this.isIncremental = isIncremental;
    }



}
