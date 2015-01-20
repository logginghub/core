package com.logginghub.logging.frontend.charting.historical;

public class TimeSeriesDataPoint {
    private long time;
    private double value;

    public TimeSeriesDataPoint() {}

    public TimeSeriesDataPoint(long time, double value) {
        super();
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
