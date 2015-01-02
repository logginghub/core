package com.logginghub.utils;

import java.util.concurrent.CountDownLatch;

public class ReusableLatch {

    private Timeout timeout = Timeout.defaultTimeout;
    private CountDownLatch latch;
    private int locks;

    public ReusableLatch(int locks) {
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
                throw new RuntimeException("Latch timed out");
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
