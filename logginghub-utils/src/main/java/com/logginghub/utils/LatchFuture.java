package com.logginghub.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LatchFuture<T> implements Future<T> {

    private volatile boolean done = false;
    private CountDownLatch latch;
    private volatile T result;

    public LatchFuture() {
        this.latch = new CountDownLatch(1);
    }

    public void trigger(T result) {
        done = true;
        this.result = result;
        latch.countDown();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return done;
    }

    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        return result;
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return result;
    }

    public T getWithDefaultTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);
        return result;
    }

    public T get(Timeout timeout) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout.getMillis(), timeout.getUnits());
        return result;
    }

}
