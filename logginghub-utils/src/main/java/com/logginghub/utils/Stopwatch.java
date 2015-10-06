package com.logginghub.utils;

import com.logginghub.utils.TimeUtils.TimeDetails;

import java.text.NumberFormat;

public class Stopwatch {
    private long startNanos = -1;
    private long stopNanos = -1;

    private String description;
    private static ThreadLocal<Stopwatch> threadStopwatches = new ThreadLocal<Stopwatch>();

    public static Stopwatch start(String description) {
        Stopwatch stopwatch = new Stopwatch(description);
        threadStopwatches.set(stopwatch);
        stopwatch.start();
        return stopwatch;
    }
    
    public String stopAndGetFormattedDurationMillis() {
        stop();
        return getFormattedDurationMillis();
    }
    
    public double stopAndGetDurationMillis() {
        stop();
        return getDurationMillis();
    }

    public String stopAndFormat() {
        stop();
        return toString();
    }

    public String stopAndFormatInterval() {
        stop();
        return toStringInterval();
    }

    public Stopwatch(String description) {
        this.description = description;
    }

    public Stopwatch() {
        this("");
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return description + " complete in " + getDurationMillis() + " ms";
    }

    public String toStringInterval() {
        return description + " complete in " + TimeUtils.formatIntervalMilliseconds(getDurationMillis()) + " ms";
    }

    public void start() {
        startNanos = System.nanoTime();
    }

    public void stop() {
        if (stopNanos == -1) {
            stopNanos = System.nanoTime();
        }
    }

    public long getDurationNanos() {
        long stop;

        if (stopNanos == -1) {
            stop = System.nanoTime();
        }
        else {
            stop = stopNanos;
        }

        return stop - startNanos;
    }

    public float getDurationMicro() {
        return getDurationNanos() / 1000f;
    }

    public float getDurationMillis() {
        return getDurationMicro() / 1000f;
    }

    public float getDurationSeconds() {
        return getDurationMillis() / 1000f;
    }

    public String getFormattedDuration() {
        float nanos = getDurationNanos();
        TimeDetails details = TimeUtils.makeNice(nanos);
        return String.format("%.2f %s", details.getValue(), details.getAbbriviatedUnits());
    }
    
    public String getFormattedDurationMillis() {
        float millis = getDurationMillis();
        return String.format("%.3f", millis);
    }

    public static void dump() {
        System.out.println(threadStopwatches.get().stopAndFormat());
    }
    
    public void stopAndDump() {
        System.out.println(stopAndFormat());
    }

    public void stopAndDumpInterval() {
        System.out.println(stopAndFormatInterval());
    }

    public static Stopwatch start(String format, Object... args) {
        return start(StringUtils.format(format, (Object[]) args));
    }

    public static void time(String operation, Runnable runnable) {
        Stopwatch sw = Stopwatch.start(operation);
        runnable.run();
        sw.stopAndDump();
    }

    public String getFormattedDurationMS() {
        NumberFormat instance = NumberFormat.getInstance();
        return instance.format(getDurationMillis()) + " ms";
    }

    public void forceElapsedMillis(double millis) {
        startNanos = 0;
        stopNanos = (long) (millis * 1000000);
    }

    
}
