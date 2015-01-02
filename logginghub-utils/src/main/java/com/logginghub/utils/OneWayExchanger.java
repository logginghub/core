package com.logginghub.utils;


/**
 * A bit like the concurrency exchanger, but this works one way only.
 */

// TODO : get rid of daft classes like this - I think resusable latch is inherently broken.
public class OneWayExchanger<T> {
    private ReusableLatch latch = new ReusableLatch(1);
    private T object;
    private Throwable exception = null;

    public T get() throws InterruptedException {
        latch.await();

        if (exception != null) {
            throw new ExchangerException(exception);
        }

        return object;
    }

    public void set(T t) {
        object = t;
        latch.countDown();
    }

    public void failed(Throwable exception) {
        this.exception = exception;
        latch.countDown();
    }
}
