package com.logginghub.utils;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A bit like the concurrency exchanger, but this works one way only.
 */
public class OneWayExchanger<T> implements Destination<T> {

    private CountDownLatch latch = new CountDownLatch(1);
    private T object;
    private Throwable exception = null;
    private Timeout timeout = Timeout.defaultTimeout;

    public void failed(Throwable exception) {
        this.exception = exception;
        latch.countDown();
    }

    public T get(long time, TimeUnit units) throws InterruptedException, TimeoutException {
        boolean successfullyCountedDown = latch.await(time, units);

        if (!successfullyCountedDown) {
            throw new TimeoutException(StringUtils.format("Timeout - we waited '{}' for the exchange to complete", TimeUtils.formatIntervalMilliseconds(units.toMillis(time))));
        }

        if (exception != null) {
            throw new ExchangerException(exception);
        }

        return object;
    }

    public T get() throws InterruptedException, TimeoutException {
        return get(timeout.getMillis(), TimeUnit.MILLISECONDS);
    }

    public void send(T t) {
        set(t);
    }

    public void set(T t) {
        object = t;
        latch.countDown();
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }
}
