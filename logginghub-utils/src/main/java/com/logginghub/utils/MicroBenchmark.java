package com.logginghub.utils;

public abstract class MicroBenchmark
{
    public void test()
    {
        int iterations = getIterations();
        int sampleSize = getSampleSize();
        long[] results = new long[iterations];

        for (int i = 0; i < iterations; i++)
        {
            long start = System.nanoTime();
            for (int j = 0; j < sampleSize; j++)
            {
                execute();
            }
            long end = System.nanoTime();
            results[i] = end - start;
        }

        Statistics statistics = new Statistics();
        for (long l : results)
        {
            statistics.addValue((float) l / 1e6f);
        }

        System.out.println(String.format("Per sample : %.0f results : mean %.9f ms (std dev %.9f ms)",
                                         statistics.getCount(),
                                         statistics.calculateMean(),
                                         statistics.calculateAverageAbsoluteDeviationFromTheMean()));

        System.out.println(String.format("Per execution : %.0f results : mean %.9f ms (std dev %.9f ms)",
                                         statistics.getCount() * sampleSize,
                                         statistics.calculateMean() / sampleSize,
                                         statistics.calculateAverageAbsoluteDeviationFromTheMean() / sampleSize));
    }

    protected int getSampleSize()
    {
        return 1000;
    }

    protected int getIterations()
    {
        return 1000;
    }

    protected abstract void execute();

    public static void runAndDump(final Runnable runnable)
    {
        MicroBenchmark microBenchmark = new MicroBenchmark()
        {
            @Override protected void execute()
            {
                runnable.run();
            }
        };
        
        microBenchmark.test();
    }
}
