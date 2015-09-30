package com.logginghub.utils;

/**
 * Created by james on 28/09/15.
 */
public class Delayer {

    private long lowestDelay;
    private long currentDelay;
    private long highestDelay;
    private double factor = 2;

    public Delayer(long lowestDelay, long highestDelay) {
        this.lowestDelay = lowestDelay;
        this.highestDelay = highestDelay;
        this.currentDelay = lowestDelay;
    }

    public void reset() {
        this.currentDelay = lowestDelay;
    }

    public void delay() {
        ThreadUtils.sleep(currentDelay);
        currentDelay *= factor;
        if(currentDelay > highestDelay) {
            currentDelay = highestDelay;
        }
    }

    public long getCurrentDelay() {
        return currentDelay;
    }

    public void setHighestDelay(long highestDelay) {
        this.highestDelay = highestDelay;
    }

    public void setLowestDelay(long lowestDelay) {
        this.lowestDelay = lowestDelay;
    }

    public long getHighestDelay() {
        return highestDelay;
    }

    public long getLowestDelay() {
        return lowestDelay;
    }
}
