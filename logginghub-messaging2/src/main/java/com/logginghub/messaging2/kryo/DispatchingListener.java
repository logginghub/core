package com.logginghub.messaging2.kryo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.QueuedListener;
import com.logginghub.utils.NamedThreadFactory;

public class DispatchingListener extends QueuedListener {
    protected final ExecutorService threadPool;

    /**
     * Creates a single thread to process notification events.
     */
    public DispatchingListener(Listener listener, String threadPrefix) {
        this(listener, Executors.newCachedThreadPool(new NamedThreadFactory(threadPrefix + "-dispatchingListenerThread-")));
    }

    /**
     * Uses the specified threadPool to process notification events.
     */
    public DispatchingListener(Listener listener, ExecutorService threadPool) {
        super(listener);
        if (threadPool == null) throw new IllegalArgumentException("threadPool cannot be null.");
        this.threadPool = threadPool;
    }

    public void queue(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * Stop the thread pool, waiting at most 30 seconds for current dispatches
     * to complete.
     */
    public void stop() {
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {}
    }
}