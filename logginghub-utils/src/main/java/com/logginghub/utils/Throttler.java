package com.logginghub.utils;

import java.util.concurrent.TimeUnit;

public class Throttler {

    private long millis;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private long previous;

    public Throttler(long amount, TimeUnit units) {
        this.millis = units.toMillis(amount);
        reset();
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public boolean isOkToFire() {
        long now = timeProvider.getTime();
        long elapsed = now - previous;
        boolean ok;
        if(elapsed >= millis)
        {
            ok = true;
            previous = now;
        }else{
            ok = false;
        }
        
        return ok;
    }

    public void reset() {
        this.previous = -millis;
    }
}
