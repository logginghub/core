package com.logginghub.utils;

import java.util.concurrent.CountDownLatch;

/**
 * A resuable latch that returns true if the latch was opened, false if it
 * stayed close, or throws a runtime exception if interupted. The latch is reset
 * after the wait... which makes it completely unsafe (it wont be clear which
 * instance is actually being used by different threads) to use it with more
 * than one thread waiting, so be careful!
 * 
 * @author James
 * 
 */
public class QuietLatch {

    private Timeout timeout = Timeout.defaultTimeout;
    private CountDownLatch latch;
    private int locks;

    public QuietLatch(int locks) {
        this.locks = locks;
        resetLatch();
    }

    public void resetLatch() {
        this.latch = new CountDownLatch(locks);
    }

    public void setLocks(int locks) {
        this.locks = locks;
        resetLatch();
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public boolean await() {
        try {
            if (latch.await(timeout.getTime(), timeout.getUnits())) {
                // All good
                resetLatch();
                return true;
            }
            else {
                resetLatch();
                return false;
            }

        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void countDown() {
        latch.countDown();
    }

}
