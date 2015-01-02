package com.logginghub.utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Benchmarker {

    private static int warmupIterations = 12000;
    
    
    private static List<Result> results = new ArrayList<Benchmarker.Result>();

    public static abstract class Approach {
        public void before() throws Exception {}

        public abstract void iterate() throws Exception;

        public void after() throws Exception {}
    }

    public static void benchmark(long duration, Approach... approaches) {
        for (Approach approach : approaches) {
            warmup(warmupIterations, approach);
            executeDuration(duration, approach);
        }
    }

    public static class Result {
        String name;
        long elapsedMS;
        long totalNS;
        long count;
    }

    private static void executeDuration(long millis, Approach approach) {

//        System.out.println("Start : " + approach.getClass().getSimpleName());
        int count = 0;
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();

        long totalDurationNanos = 0;

        while (end - start < millis) {
            try {
                approach.before();
                long startNS = System.nanoTime();
                approach.iterate();
                long endNS = System.nanoTime();
                totalDurationNanos += endNS - startNS;
                approach.after();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            count++;
            end = System.currentTimeMillis();
        }

        long elapsedMS = end - start;
//        System.out.println("End : " + approach.getClass().getSimpleName());
        report(approach, count, totalDurationNanos, elapsedMS);
    }

    private static void executeIterations(int iterations, Approach approach) {

        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();

        long totalDurationNanos = 0;

        int count = 0;
        while (count < iterations) {
            try {
                approach.before();
                long startNS = System.nanoTime();
                approach.iterate();
                long endNS = System.nanoTime();
                totalDurationNanos += endNS - startNS;
                approach.after();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            count++;
            end = System.currentTimeMillis();
        }

        long elapsedMS = end - start;
        report(approach, count, totalDurationNanos, elapsedMS);
    }
    
    private static void warmup(int iterations, Approach approach) {

        int count = 0;
        while (count < iterations) {
            try {
                approach.before();
                approach.iterate();
                approach.after();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    private static void report(Approach approach, int count, long totalDurationNanos, long elapsedMS) {
        // if(elapsedMS == 0) {
        // elapsedMS = 1;
        // }
        NumberFormat nf = NumberFormat.getInstance();
        System.out.println(StringUtils.format("Approach {} did {} iterations in {} ms = {} i/s. Mean time per interation {} ns, rate {} /is",
                                              approach.getClass().getSimpleName(),
                                              nf.format(count),
                                              elapsedMS,
                                              nf.format(count / ((elapsedMS) / 1000d)),
                                              nf.format(totalDurationNanos / (double) count),
                                              nf.format(1e9 / (totalDurationNanos / (double) count))));

        Result result = new Result();
        result.count = count;
        result.elapsedMS = elapsedMS;
        result.totalNS = totalDurationNanos;
        result.name = approach.getClass().getSimpleName();
        results.add(result);
    }

    public static void benchmark(int duration, Class<?> clazz) {

        List<Approach> approaches = buildList(clazz);

        for (Approach approach : approaches) {
            warmup(warmupIterations, approach);
            executeDuration(duration, approach);
            
            System.gc();
            System.gc();
            System.gc();
        }

        outputSorted();
    }

    public static void benchmark(long duration, Class<?>... clazz) {
        List<Approach> approaches = new ArrayList<Benchmarker.Approach>();
        for (Class<?> class1 : clazz) {
            approaches.add((Approach) ReflectionUtils.instantiate(class1));
        }

        for (Approach approach : approaches) {
            executeDuration(duration, approach);
        }

        outputSorted();
    }

    private static List<Approach> buildList(Class<?> clazz) {
        List<Approach> approaches = new ArrayList<Approach>();

        Class[] classes = clazz.getClasses();
        for (Class class1 : classes) {

            if (Approach.class.isAssignableFrom(class1)) {
                Approach approach = (Approach)ReflectionUtils.instantiate(class1);
                approaches.add(approach);
            }
        }
        return approaches;
    }

    public static void benchmarkIterations(int i, Class<?> clazz) {

        List<Approach> approaches = buildList(clazz);

        for (Approach approach : approaches) {
            warmup(warmupIterations, approach);
            executeIterations(i, approach);
        }

        outputSorted();
    }

    private static void outputSorted() {
        System.out.println("=========================================================================================================================================");
        Collections.sort(results, new Comparator<Result>() {
            public int compare(Result o1, Result o2) {
                return CompareUtils.compareDoubles(o1.totalNS / (double) o1.count, o2.totalNS / (double) o2.count);
            }
        });

        for (Result result : results) {
            NumberFormat nf = NumberFormat.getInstance();
            System.out.println(StringUtils.format("Approach {} did {} iterations in {} ms = {} i/s. Mean time per interation {} ns, rate {} /is",
                                                  result.name,
                                                  nf.format(result.count),
                                                  result.elapsedMS,
                                                  nf.format(result.count / ((result.elapsedMS) / 1000d)),
                                                  nf.format(result.totalNS / (double) result.count),
                                                  nf.format(1e9 / (result.totalNS / (double) result.count))));
        }

        System.out.println("=========================================================================================================================================");

        for (Result result : results) {
            NumberFormat nf = NumberFormat.getInstance();
            System.out.println(StringUtils.format("{},{},{},{},{},{}",
                                                  result.name,
                                                  (result.count),
                                                  result.elapsedMS,
                                                  (result.count / ((result.elapsedMS) / 1000d)),
                                                  (result.totalNS / (double) result.count),
                                                  (1e9 / (result.totalNS / (double) result.count))));
        }

    }

    public static void setWarmupIterations(int _warmupIterations) {
        warmupIterations = _warmupIterations;
    }
    
}
