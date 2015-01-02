package com.logginghub.utils;

import java.util.concurrent.TimeUnit;

import com.logginghub.utils.logging.Logger;

public class Timeout {

    private long time = 5;
    private TimeUnit units = TimeUnit.SECONDS;

    public Timeout(long time, TimeUnit units) {
        super();
        this.time = time;
        this.units = units;
    }

    public static Timeout defaultTimeout = new Timeout(5, TimeUnit.SECONDS);

    public long getTime() {
        return time;
    }

    public TimeUnit getUnits() {
        return units;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUnits(TimeUnit units) {
        this.units = units;
    }

    public void setTimeout(long time, TimeUnit units) {
        this.time = time;
        this.units = units;
    }

    public void debugMode() {
        debugMode(false);
    }

    public void debugMode(boolean changeLogLevel) {
        Logger.root().warn("==========================================================");
        Logger.root().warn("==                                                      ==");
        Logger.root().warn("==             TIMEOUT IN DEBUG MODE                    ==");
        Logger.root().warn("==                                                      ==");
        Logger.root().warn("==========================================================");
        
        setTimeout(10000, TimeUnit.SECONDS);

        if (changeLogLevel) {
            Logger.setRootLevel(Logger.debug);
        }
    }

    public void traceMode() {
        Logger.root().warn("==========================================================");
        Logger.root().warn("==                                                      ==");
        Logger.root().warn("==             TIMEOUT IN TRACE MODE                    ==");
        Logger.root().warn("==                                                      ==");
        Logger.root().warn("==========================================================");
        Logger.setRootLevel(Logger.trace);
        setTimeout(10000, TimeUnit.SECONDS);
    }

    public long getMillis() {
        return units.toMillis(time);

    }

    public void setTime(String interval) {
        setTime(TimeUtils.parseInterval(interval));
    }
}
