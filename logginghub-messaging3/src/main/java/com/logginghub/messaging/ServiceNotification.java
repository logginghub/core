package com.logginghub.messaging;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import com.logginghub.utils.Timeout;

public class ServiceNotification<T> extends Notification implements ServiceListener<T> {

    private CountDownLatch responseLatch = new CountDownLatch(1);
    private T response;
    private Throwable reason;
    private Timeout timeout = Timeout.defaultTimeout;

    public T awaitService() {
        try {
            boolean latchOpened = responseLatch.await(timeout.getTime(), timeout.getUnits());
            if (latchOpened) {
                if (reason != null) {
                    throw new RuntimeException(reason);
                }
                else {
                    return response;
                }
            }
            else {
                throw new RuntimeException(new TimeoutException("Notification await timed out"));
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            resetResponse();
        }
    }
 
    public T awaitServiceForever() {
        try {
            responseLatch.await();
            if (reason != null) {
                throw new RuntimeException(reason);
            }
            else {
                return response;
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            resetResponse();
        }
    }

    private void resetResponse() {
        responseLatch = new CountDownLatch(1);
        reason = null;
    }

    public void onServiceAvailable(T response) {
        this.response = response;
        responseLatch.countDown();
    }

    public void onServiceTimeout() {
        // TODO : pass in some context so we know what has timed out
        reason = new TimeoutException("Request timed out");
        responseLatch.countDown();
    }

    public void onServiceFailure(Throwable reason) {
        this.reason = reason;
        responseLatch.countDown();
    }

}
