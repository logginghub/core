package com.logginghub.utils;

public class IteratingRunnableWorkerThread extends WorkerThread {

    private final IteratingRunnable runnable;

    public IteratingRunnableWorkerThread(String threadName, IteratingRunnable runnable) {
        super(threadName);
        this.runnable = runnable;
    }

    @Override protected void beforeStart() {
        super.beforeStart();
        runnable.beforeFirst();
    }

    @Override protected void onRun() throws Throwable {
        runnable.iterate();
    }

    @Override protected void beforeStop() {
        super.beforeStop();
        runnable.afterLast();
    }

}
