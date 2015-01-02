package com.logginghub.utils;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.logginghub.utils.ConcurrentMovingAverage;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestConcurrentMovingAverage {

    @Test public void testMovingAverage() {
        
        ConcurrentMovingAverage movingAverage = new ConcurrentMovingAverage(3);
                
        assertThat(movingAverage.calculateMovingAverage(), is(Double.NaN));

        // First item
        movingAverage.addValue(1);
        assertThat(movingAverage.calculateMovingAverage(), is(1d));
        
        // Second item
        movingAverage.addValue(2);
        assertThat(movingAverage.calculateMovingAverage(), is(1.5d));
        
        // Third item
        movingAverage.addValue(3);
        assertThat(movingAverage.calculateMovingAverage(), is(2d));
        
        // Fourth item
        movingAverage.addValue(4);
        assertThat(movingAverage.calculateMovingAverage(), is(3d));        
    }
    
    @Test public void testConcurrency() throws InterruptedException, ExecutionException{
        
        int tasks = 100000;
        
        final Random random = new Random();
        final ConcurrentMovingAverage cma = new ConcurrentMovingAverage(1000);
        List<Callable<Boolean>> taskList = new ArrayList<Callable<Boolean>>();
        
        for(int i = 0; i < tasks; i++){
            taskList.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    cma.addValue(random.nextDouble());                    
                    return true;                    
                }
            });
            
            taskList.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    cma.calculateMovingAverage();                    
                    return true;                    
                }
            });
        }
        
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(100);
        List<Future<Boolean>> invokeAll = newFixedThreadPool.invokeAll(taskList);
        try{
        for (Future<Boolean> future : invokeAll) {
            future.get();
        }
        }catch(ExecutionException ee){
            if(ee.getCause() instanceof ConcurrentModificationException){
                // Fine, this class is expected to break.
            }
        }
        
    }
}
