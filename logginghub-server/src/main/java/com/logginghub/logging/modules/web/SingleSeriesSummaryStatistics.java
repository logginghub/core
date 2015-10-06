package com.logginghub.logging.modules.web;

/**
 * Created by james on 02/10/15.
 */
public class SingleSeriesSummaryStatistics {
    private long time;
    private long intervalLength;
    private long count;

    public SingleSeriesSummaryStatistics(long time, long intervalLength, long count) {
        this.time = time;
        this.intervalLength = intervalLength;
        this.count = count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
