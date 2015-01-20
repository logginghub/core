package com.logginghub.analytics.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import com.logginghub.utils.StringUtils;

/**
 * Encapsulation of a single data point in a time series. It associates an array
 * of key data against a single value object, along with its sample time in
 * millisecond precision.
 * 
 * @author James
 */
public class TimeSeriesDataPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private long time;
    private String[] keys;
    private double[] values;

    public TimeSeriesDataPoint() {

    }

    public TimeSeriesDataPoint(long time, String[] keys, double values[]) {
        this.time = time;
        this.keys = keys;
        this.values = values;
    }

    public String[] getKeys() {
        return keys;
    }

    public long getTime() {
        return time;
    }

    public double[] getValues() {
        return values;
    }

    public TimeSeriesDataPoint copy() {
        String[] copyOfKeys = Arrays.copyOf(keys, keys.length);
        double[] copyOfValues = Arrays.copyOf(values, values.length);
        TimeSeriesDataPoint timeSeriesDataPoint = new TimeSeriesDataPoint(time, copyOfKeys, copyOfValues);
        return timeSeriesDataPoint;
    }

    @Override public String toString() {
        return StringUtils.format("TimeSeriesDataPoint time={} ({}) keys={} values={}", new Date(time), time, Arrays.toString(keys), Arrays.toString(values));
    }

}
