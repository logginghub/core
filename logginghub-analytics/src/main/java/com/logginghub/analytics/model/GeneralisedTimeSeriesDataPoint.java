package com.logginghub.analytics.model;

import java.util.Arrays;
import java.util.Date;

import com.logginghub.utils.StringUtils;

/**
 * This is similar to the standard {@link TimeSeriesDataPoint} but has been
 * generalised so it can contain any kind of data, not just doubles. In order to
 * save space it also assumes that the key definitions are the same for the
 * whole series so doesn't re-store them on each point.
 * 
 * @author James
 * 
 */
public class GeneralisedTimeSeriesDataPoint {
    private long time;
    private Object[] values;

    public GeneralisedTimeSeriesDataPoint() {

    }

    public GeneralisedTimeSeriesDataPoint(long time, Object values[]) {
        this.time = time;
        this.values = values;
    }

    public long getTime() {
        return time;
    }

    public Object[] getValues() {
        return values;
    }

    public GeneralisedTimeSeriesDataPoint copy() {
        Object[] copyOfValues = Arrays.copyOf(values, values.length);
        GeneralisedTimeSeriesDataPoint timeSeriesDataPoint = new GeneralisedTimeSeriesDataPoint(time, copyOfValues);
        return timeSeriesDataPoint;
    }

    @Override public String toString() {
        return StringUtils.format("GeneralisedTimeSeriesDataPoint time={} ({}) keys={} values={}", new Date(time), time, Arrays.toString(values));
    }
}
