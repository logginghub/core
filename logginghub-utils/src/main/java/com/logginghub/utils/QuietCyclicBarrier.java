package com.logginghub.utils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QuietCyclicBarrier {

    private long timeout = 5;
    private TimeUnit units = TimeUnit.SECONDS;
    private CyclicBarrier barrier;

    public QuietCyclicBarrier(int parties) {
        this.barrier = new CyclicBarrier(parties);
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    public void setUnits(TimeUnit units) {
        this.units = units;
    }

    public void await() {
        try {
            barrier.await(timeout, units);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
