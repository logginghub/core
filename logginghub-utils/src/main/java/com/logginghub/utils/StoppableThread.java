package com.logginghub.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class StoppableThread extends Thread {

    private final Runnable runnable;
    private AtomicBoolean keepRunning = new AtomicBoolean(true);

    public StoppableThread(Runnable runnable, String name) {
        this.runnable = runnable;
        setName(name);
    }

    @Override public void run() {
        while (keepRunning.get()) {
            runnable.run();
        }
    }

    public void stopSafely(long waitTime) {
        keepRunning.set(false);
        this.interrupt();

        while (this.isAlive()) {
            try {
                this.join(waitTime);
                throw new RuntimeException(String.format("Timed out waiting for thread %s to die", this));
            }
            catch (InterruptedException e) {}
        }
    }

}
