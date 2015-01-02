package com.logginghub.utils;

import com.logginghub.utils.Stopwatch;

import junit.framework.TestCase;

public class TestStopwatch extends TestCase
{
    public void test()
    {
        Stopwatch stopwatch = new Stopwatch("Testing this stuff");
        stopwatch.start();
        stopwatch.stop();
        
        // This is a crap test, sometimes it does take a while to kick in!
        long nanos = stopwatch.getDurationNanos();
        assertTrue(nanos > 0);
        
        float micro = stopwatch.getDurationMicro();
        assertTrue(micro > 0);
        
        float millis = stopwatch.getDurationMillis();
        assertTrue(millis > 0);
        
        float seconds = stopwatch.getDurationSeconds();
        assertTrue(seconds > 0);
        
        String logger = stopwatch.toString();        
        System.out.println(logger);
    }
}
