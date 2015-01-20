package com.logginghub.messaging;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import com.logginghub.utils.Timeout;

public class Notification implements AsycNotification {

    private CountDownLatch latch = new CountDownLatch(1);
    private Throwable reason;
    private Timeout timeout = Timeout.defaultTimeout;

    public void onSuccess() {
        latch.countDown();
    }

    public void onFailure(Throwable reason) {
        this.reason = reason;
        latch.countDown();
    }
    
    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void onTimeout() {
        // TODO : pass in some context so we know what has timed out
        reason = new TimeoutException("Request timed out");
        latch.countDown();
    }

    public void await() {
        try {
            boolean latchOpened = latch.await(timeout.getTime(), timeout.getUnits());
            if (latchOpened) {
                if (reason != null) {
                    throw new RuntimeException(reason);
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
            reset();
        }
    }

    private void reset() {
        reason = null;
        latch = new CountDownLatch(1);
    }

    public void awaitForever() {
        try {
            latch.await();
            if (reason != null) {
                throw new RuntimeException(reason);
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            reset();
        }
    }

}
