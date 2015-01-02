package com.logginghub.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger threadIndex = new AtomicInteger();
    private final String prefix;
    private boolean daemon = false;

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public String getPrefix() {
        return prefix;
    }
    
    protected int getAndIncrement() {
        return threadIndex.getAndIncrement();
    }
    
    public boolean isDaemon() {
        return daemon;
    }

    public Thread newThread(Runnable runnable) {
        String threadName = getThreadName(runnable);
        final Thread t = new Thread(runnable, threadName);
        t.setDaemon(daemon);
        return t;
    }
    
    public String getThreadName(Runnable runnable) {
        String threadName = String.format("%s%d", prefix, threadIndex.getAndIncrement());
        return threadName;
    }
}
