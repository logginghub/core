package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.utils.MovingAverage;

public class TestMovingAverage {

    @Test public void testMovingAverage() {

        MovingAverage movingAverage = new MovingAverage(3);

        assertThat(movingAverage.calculateMovingAverage(), is(Double.NaN));

        // First item
        movingAverage.addValue(1);

        assertThat(movingAverage.getValues().size(), is(1));
        assertThat(movingAverage.getValues().get(0), is(1d));

        assertThat(movingAverage.calculateMovingAverage(), is(1d));

        // Second item
        movingAverage.addValue(2);

        assertThat(movingAverage.getValues().size(), is(2));
        assertThat(movingAverage.getValues().get(0), is(1d));
        assertThat(movingAverage.getValues().get(1), is(2d));

        assertThat(movingAverage.calculateMovingAverage(), is(1.5d));

        // Third item
        movingAverage.addValue(3);

        assertThat(movingAverage.getValues().size(), is(3));
        assertThat(movingAverage.getValues().get(0), is(1d));
        assertThat(movingAverage.getValues().get(1), is(2d));
        assertThat(movingAverage.getValues().get(2), is(3d));

        assertThat(movingAverage.calculateMovingAverage(), is(2d));

        // Fourth item
        movingAverage.addValue(4);

        assertThat(movingAverage.getValues().size(), is(3));
        assertThat(movingAverage.getValues().get(0), is(2d));
        assertThat(movingAverage.getValues().get(1), is(3d));
        assertThat(movingAverage.getValues().get(2), is(4d));

        assertThat(movingAverage.calculateMovingAverage(), is(3d));

    }

    @Ignore// Not a unit test
    @Test public void testConcurrency() throws InterruptedException, ExecutionException {

        int tasks = 100000;

        final Random random = new Random();
        final MovingAverage cma = new MovingAverage(1000);
        List<Callable<Boolean>> taskList = new ArrayList<Callable<Boolean>>();

        for (int i = 0; i < tasks; i++) {
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
        try {
            for (Future<Boolean> future : invokeAll) {
                future.get();
            }
        }
        catch (ExecutionException ee) {
            if (ee.getCause() instanceof ConcurrentModificationException) {
                // Fine, this class is expected to break.
            }
        }

    }

}
