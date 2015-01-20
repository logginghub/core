package com.logginghub.logging;

import java.text.NumberFormat;

import com.logginghub.logging.utils.LapCountingStopwatch;
import com.logginghub.utils.Stopwatch;

public class LoadTestRunner
{
    public static void runFor5Seconds(Runnable test, int elementsPerWorkRun)
    {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        
        long counter = 0;
        while(stopwatch.getDurationSeconds() < 5)
        {
            System.out.println(stopwatch.getDurationSeconds());
            test.run();
            counter++;
        }
        
        stopwatch.stop();
        
        float duration = stopwatch.getDurationSeconds();
        float perSecond = elementsPerWorkRun * counter / duration;
        System.out.println(String.format("Rate is %s operations per second (%d operations containing %s units of work in %.2f seconds)", 
                                         NumberFormat.getInstance().format(perSecond), 
                                         counter, 
                                         NumberFormat.getInstance().format(elementsPerWorkRun), 
                                         duration));
    }
    
    public static void run(Runnable test,
                           int repeats,
                           int warmups,
                           long elementsWithinTheWork,
                           String description)
    {
        LapCountingStopwatch stopwatch = new LapCountingStopwatch();

        for(int i = 0; i < repeats + warmups; i++)
        {
            if(i == warmups)
            {
                stopwatch.start();
            }

            test.run();
            
            if(i >= warmups)
            {
                stopwatch.lap();
            }
        }

        System.out.println(stopwatch.dump());
        System.out.println("Average time for " + elementsWithinTheWork +
                           " " +
                           description +
                           " = " +
                           stopwatch.getFormattedAverageLapTime());
    }
}
