package com.logginghub.utils;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExchangerFuture<T> implements Future<T> {

    private volatile boolean done = false;
    private Exchanger<T> exchanger;

    public ExchangerFuture(Exchanger<T> exchanger) {
        this.exchanger = exchanger;
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
        T exchanged = exchanger.exchange(null);
        done = true;
        return exchanged;
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        T exchanged = exchanger.exchange(null, timeout, unit);
        done = true;
        return exchanged;
    }
    
    public T getWithDefaultTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        T exchanged = exchanger.exchange(null, Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);
        done = true;
        return exchanged;
    }
    
    public T get(Timeout timeout) throws InterruptedException, ExecutionException, TimeoutException {
        T exchanged = exchanger.exchange(null, timeout.getMillis(), timeout.getUnits());
        done = true;
        return exchanged;
    }

}
