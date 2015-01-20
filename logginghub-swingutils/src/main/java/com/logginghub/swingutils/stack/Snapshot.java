package com.logginghub.swingutils.stack;

import java.lang.Thread.State;

public class Snapshot {

    private String uniqueID;
    private long time;
    private long interval;
    private State state;
    private String method;
    
    public long getInterval() {
        return interval;
    }
    
    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override public String toString() {
        return "Snapshot [uniqueID=" + uniqueID + ", time=" + time + ", interval=" + interval + ", state=" + state + ", method=" + method + "]";
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }

    
    
}
