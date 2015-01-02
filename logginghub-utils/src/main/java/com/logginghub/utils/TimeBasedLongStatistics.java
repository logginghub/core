package com.logginghub.utils;

import java.util.ArrayList;
import java.util.List;

public class TimeBasedLongStatistics {

    private long[] values;
    private long[] times;

    private int count = 0;

    public TimeBasedLongStatistics(int entries) {
        values = new long[entries];
        times = new long[entries];
    }

    public void add(long time, long value) {
        times[count] = time;
        values[count] = value;
        count++;
    }

    public long getDuration() {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (long l : times) {
            min = Math.min(min, l);
            max = Math.max(max, l);
        }

        return max - min;
    }

    public long getLatestTime() {
        long max = Long.MIN_VALUE;

        for (long l : times) {
            max = Math.max(max, l);
        }

        return max;
    }

    public long getEarliestTime() {
        long min = Long.MAX_VALUE;

        for (long l : times) {
            min = Math.min(min, l);
        }

        return min;
    }

    public int getCount() {
        return count;
    }

    public Pair<Long, SinglePassStatisticsLongPrecision>[] chunk(long interval) {

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (long l : times) {
            min = Math.min(min, l);
            max = Math.max(max, l);
        }

        int chunks = (int) Math.ceil(((max - min) / (double) interval));

        Pair<Long, SinglePassStatisticsLongPrecision>[] data = new Pair[chunks];

        int readPointer = 0;
        for (int chunk = 0; chunk < chunks; chunk++) {

            long chunkStartTime = min + (chunk * interval);
            long chunkEndTime = chunkStartTime + interval;

            data[chunk] = new Pair<Long, SinglePassStatisticsLongPrecision>();
            data[chunk].setA(chunkStartTime);
            data[chunk].setB(new SinglePassStatisticsLongPrecision());

            long time;
            do {
                time = times[readPointer];
                if (time >= chunkStartTime && time < chunkEndTime) {
                    data[chunk].getB().addValue(values[readPointer]);
                    readPointer++;

                }
            }
            while (time < chunkEndTime && readPointer < count);

            data[chunk].getB().doCalculations();
        }

        return data;
    }

    public static List<Pair<Long, Double>> countSeries(Pair<Long, SinglePassStatisticsLongPrecision>[] chunk) {
        List<Pair<Long, Double>> result = new ArrayList<Pair<Long, Double>>();
        for (Pair<Long, SinglePassStatisticsLongPrecision> pair : chunk) {
            result.add(new Pair<Long, Double>(pair.getA(), (double)pair.getB().getCount()));
        }
        return result;
    }
    
    public static List<Pair<Long, Double>> meanSeries(Pair<Long, SinglePassStatisticsLongPrecision>[] chunk) {
        List<Pair<Long, Double>> result = new ArrayList<Pair<Long, Double>>();
        for (Pair<Long, SinglePassStatisticsLongPrecision> pair : chunk) {
            result.add(new Pair<Long, Double>(pair.getA(), pair.getB().getMean()));
        }
        return result;
    }

    public static List<Pair<Long, Double>> scaleTimeSeries(List<Pair<Long, Double>> data, double scale) {
        for (Pair<Long, Double> pair : data) {
            pair.setA((long) (pair.getA() * scale));
        }
        return data;
    }

    public static List<Pair<Long, Double>> scaleValueSeries(List<Pair<Long, Double>> data, double scale) {
        for (Pair<Long, Double> pair : data) {
            pair.setB(pair.getB() * scale);
        }
        return data;
    }

    public SinglePassStatisticsLongPrecision getOverallStatistics() {
        SinglePassStatisticsLongPrecision stats = new SinglePassStatisticsLongPrecision();

        for (int i = 0; i < count; i++) {
            long value = values[i];
            stats.addValue(value);
        }

        stats.doCalculations();
        return stats;

    }

    public void reset() {
        for (int i = 0; i < times.length; i++) {
            values[i] = 0;
            times[i] = 0;
        }
        count = 0;
    }
}
