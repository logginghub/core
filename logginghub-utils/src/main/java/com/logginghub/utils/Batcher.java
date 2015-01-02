package com.logginghub.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Batcher<T> implements Destination<T> {

    private List<T> pendingItems = new ArrayList<T>();
    private int threshold = 1000;

    private Multiplexer<List<T>> destinations = new Multiplexer<List<T>>();
    private Timeout timeout;
    private Timer timer;
    private static final int TIME_BASED = -1;

    public Batcher(int threshold) {
        this.threshold = threshold;
    }

    public Batcher(Timeout timeout) {
        this.timeout = timeout;
        threshold = TIME_BASED;
    }

    public void start() {
        stop();
        if (timeout != null) {
            timer = TimerUtils.every("batcherTimeout", timeout, new Runnable() {
                public void run() {
                    timeout();
                }
            });
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setItemThreshold(int itemThreshold) {
        this.threshold = itemThreshold;
    }

    public void dispatchRemaining() {
        synchronized (pendingItems) {
            if (pendingItems.size() > 0) {
                destinations.send(pendingItems);
                pendingItems.clear();
            }
        }
    }

    protected void timeout() {
        dispatchRemaining();
    }

    public void addDestination(Destination<List<T>> listener) {
        destinations.addDestination(listener);
    }

    public void removeDestination(Destination<T> listener) {
        destinations.removeDestination(destinations);
    }

    public void send(T t) {
        synchronized (pendingItems) {
            pendingItems.add(t);
            if (threshold != TIME_BASED && pendingItems.size() > threshold) {
                dispatchRemaining();
            }
        }
    }

}
