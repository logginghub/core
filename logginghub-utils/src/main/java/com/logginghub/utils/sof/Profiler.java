package com.logginghub.utils.sof;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;

public class Profiler {

    // private static long current;

    public final static class Entry {
        long start;
        String key;
        int depth;

        public Entry(long start, String key, int depth) {
            super();
            this.start = start;
            this.key = key;
            this.depth = depth;
        }

    }

    public final static class Result {
        public Result(String key) {
            this.key = key;
        }

        String key;
        double total;
        long count;
        int depth;

        public void update(long elapsed, int depth) {
            this.depth = depth;
            count++;
            total += elapsed;
        }

        public double calculateMeanNanos() {
            return total / count;
        }

    }

    public static void dump() {

        NumberFormat nf = NumberFormat.getInstance();
        for (String string : outputOrder) {
            Result entry = entries.get(string);
            Out.out("{} | {} | {} | {}",
                    StringUtils.padRight(StringUtils.repeat("  ", entry.depth) + entry.key, 60),
                    StringUtils.padLeft(nf.format(entry.count), 20),
                    StringUtils.padLeft(nf.format(entry.total), 20),
                    StringUtils.padLeft(TimeUtils.formatIntervalNanoseconds(entry.calculateMeanNanos()), 20));
        }
    }

    private static FactoryMap<String, Result> entries = new FactoryMap<String, Result>() {
        @Override protected Result createEmptyValue(String key) {
            return new Result(key);
        }
    };

    private static Stack<Entry> entryStack = new Stack<Entry>();

    // private static String name;

    public static void end() {
        long endTime = System.nanoTime();
        
        StringBuilder keyBuilder = new StringBuilder();
        String div = "";
        for (Entry entry : entryStack) {
            keyBuilder.append(div).append(entry.key);
            div ="::";
        }
        
        Entry pop = entryStack.pop();
        
        String key = keyBuilder.toString();
        
        long elapsed = endTime - pop.start;
        entries.get(key).update(elapsed, pop.depth);
        
    }

    private static List<String> outputOrder = new ArrayList<String>();

    public static void start(String name) {

        StringBuilder keyBuilder = new StringBuilder();
        String div = "";
        for (Entry entry : entryStack) {
            keyBuilder.append(div).append(entry.key);
            div ="::";
        }
        keyBuilder.append(div).append(name);
        
        String key = keyBuilder.toString();
        
        if (!outputOrder.contains(key)) {
            outputOrder.add(key);
        }

        Entry entry = new Entry(0, name, entryStack.size());
        entryStack.push(entry);
        entry.start = System.nanoTime();
    }

}
