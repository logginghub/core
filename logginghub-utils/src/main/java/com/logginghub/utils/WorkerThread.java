package com.logginghub.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classic worker thread wrapper to make extending Thread a little simpler. WorkerThreads work iteratively, and can be stopped after each iteration.
 * <p/>
 * If you want something like Runnable that can be plugged into multiple WorkerThreads, see {@link IteratingRunnable} and {@link
 * IteratingRunnableWorkerThread}.
 * <p/>
 * Just instantiate anonymously and implement the onRun() method to provide a single iteration of the thread.
 *
 * @author James
 */
public abstract class WorkerThread {
    private static Logger logger = Logger.getLogger(WorkerThread.class.getName());
    private Thread thread;
    private volatile boolean keepRunning = false;
    private ExceptionHandler exceptionHandler = new SystemErrExceptionHandler();
    private List<WorkerThreadListener> listeners = new CopyOnWriteArrayList<WorkerThreadListener>();
    private Object lock = new Object();
    private volatile long iterationDelay = 0;
    private volatile long preIterationDelay = 0;
    // private long iterationElapsedTimeOverrideNanos;
    private String threadName;
    private Timeout joinTimeout = Timeout.defaultTimeout;
    private boolean keepRunningOnExceptions = true;
    private int priority = Thread.NORM_PRIORITY;

    public WorkerThread(String threadName) {
        this.threadName = threadName;
        thread = new Thread(new Runnable() {
            public void run() {
                runInternal();
            }
        }, threadName);
    }

    private void runInternal() {

        beforeStart();

        while (isRunning()) {

            if (preIterationDelay > 0) {
                delay(preIterationDelay, 0);
            }

            if (!Thread.currentThread().isInterrupted()) {
                long elapsed;
                long start = System.nanoTime();
                // iterationElapsedTimeOverrideNanos = 0;
                try {
                    onRun();
                } catch (Throwable t) {
                    if ((t instanceof InterruptedException || (t.getCause() != null && (t.getCause() instanceof InterruptedException))) &&
                        !keepRunning()) {
                        // Blocking worker threads will quite often throw this when
                        // they are shutting down so if keepRunning is false ignore
                        // it
                    } else {
                        if (t instanceof StopRunningException) {
                            keepRunning = false;
                        } else {
                            // TODO : do we keep running on errors?
                            handleException(t);
                            if (keepRunningOnExceptions) {

                            } else {
                                keepRunning = false;
                            }
                        }
                    }
                } finally {
                    elapsed = System.nanoTime() - start;
                }

                if (isRunning() && iterationDelay > 0) {
                    delay(iterationDelay, elapsed - 1000);
                }
            } else {
                Thread.interrupted();
            }
        }

        beforeStop();
        fireStopped();
    }

    protected void beforeStart() {
    }

    public boolean isRunning() {
        synchronized (lock) {
            return keepRunning;
        }
    }

    private void delay(long delay, long elapsed) {
        try {
            // If the delay is greater than 16 milliseconds, we'll use
            // the rubbishy standard sleep to save CPU
            long timeToDelay = delay - elapsed;

            if (timeToDelay > 16000000) {
                Thread.sleep(timeToDelay / 1000000);
            } else {
                ThreadUtils.sleepNanos(timeToDelay);
            }
            //
        } catch (InterruptedException e) {
//            logger.info(StringUtils.format("Worker thread '{}' interupted during delay - keepRunning = {}", this, keepRunning));
            // Fine, we might need to be interrupted to kill the thread
            // so this isn't the end of the world.
            Thread.interrupted();
        }
    }

    protected abstract void onRun() throws Throwable;

    protected boolean keepRunning() {
        return keepRunning;
    }

    protected void handleException(Throwable t) {
        if (exceptionHandler != null) {
            exceptionHandler.handleException("Worker thread exception", t);
        } else {
            logger.log(Level.WARNING,
                       String.format("Exception caught from worker thread %s, and no exception handler was set, so we are logging a warning",
                                     thread.getName()),
                       t);
        }
    }

    protected void beforeStop() {
    }

    private void fireStopped() {
        for (WorkerThreadListener workerThreadListener : listeners) {
            workerThreadListener.onStopped();
        }
    }

    public static WorkerThread every(String name, long interval, TimeUnit units, Runnable runnable) {
        return executeOngoingPre(name, units.toMillis(interval), runnable);
    }

    public static WorkerThread every(String name, String interval, Runnable runnable) {
        return executeOngoingPre(name, TimeUtils.parseInterval(interval), runnable);
    }

    public static WorkerThread everyNow(String name, long interval, TimeUnit units, Runnable runnable) {
        return executeOngoing(name, units.toMillis(interval), runnable);
    }

    public static WorkerThread executeOngoing(String name, long iterationDelay, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.setIterationDelay(iterationDelay);
        wt.start();
        return wt;
    }

    public static WorkerThread everyNowDaemon(String name, long interval, TimeUnit units, Runnable runnable) {
        return executeDaemonOngoing(name, units.toMillis(interval), runnable);
    }

    public static WorkerThread executeDaemonOngoing(String name, long iterationDelay, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.setDaemon(true);
        wt.setIterationDelay(iterationDelay);
        wt.start();
        return wt;
    }

    public static WorkerThread everySecond(String name, Runnable runnable) {
        return executeOngoingPre(name, 1000, runnable);
    }

    public static WorkerThread executeOngoingPre(String name, long iterationDelay, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.setPreIterationDelay(iterationDelay);
        wt.start();
        return wt;
    }

    public static WorkerThread everySecondDaemon(String name, Runnable runnable) {
        return executeDaemonOngoingPre(name, 1000, runnable);
    }

    public static WorkerThread executeDaemonOngoingPre(String name, long iterationDelay, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.setDaemon(true);
        wt.setPreIterationDelay(iterationDelay);
        wt.start();
        return wt;
    }

    public static WorkerThread execute(String name, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                try {
                    runnable.run();
                } finally {
                    finished();
                }
            }
        };
        wt.start();
        return wt;
    }

    public static WorkerThread executeDaemon(String name, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                try {
                    runnable.run();
                } finally {
                    finished();
                }
            }
        };
        wt.setDaemon(true);
        wt.start();
        return wt;
    }

    /**
     * Call this method only from the wrapped thread - it indicates a natural end of life for the thread, so no need to join or do any of the other
     * bits.
     */
    protected void finished() {
        keepRunning = false;
    }

    public void setDaemon(boolean b) {
        thread.setDaemon(b);
    }

    public static WorkerThread executeDaemonOngoing(String name, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.setDaemon(true);
        wt.start();
        return wt;
    }

    public static WorkerThread executeFixedRate(String name, long opsPerSecond, final Runnable runnable) {

        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };

        long iterationDelayNanos = (long) (1e9 / opsPerSecond);

        wt.setIterationDelayNanos(iterationDelayNanos);
        wt.start();
        return wt;

    }

    public static WorkerThread executeIn(String name, int amount, TimeUnit units, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                try {
                    if (isRunning()) {
                        runnable.run();
                    }
                } finally {
                    finished();
                }
            }
        };
        wt.setPreIterationDelay(units.toMillis(amount));
        wt.start();
        return wt;
    }

    ;

    public static WorkerThread executeOngoing(String name, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
            }
        };
        wt.start();
        return wt;
    }

    public static WorkerThread executeOngoing(String name, long initialDelay, long iterationDelay, final Runnable runnable) {
        WorkerThread wt = new WorkerThread(name) {
            @Override
            protected void onRun() throws Throwable {
                runnable.run();
                setPreIterationDelay(0);
            }
        };
        wt.setIterationDelay(iterationDelay);
        wt.setPreIterationDelay(initialDelay);
        wt.start();
        return wt;
    }

    public void addListener(WorkerThreadListener listener) {
        listeners.add(listener);
    }

    ;

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    // public void setIterationElapsedTimeOverrideNanos(long
    // iterationElapsedTimeOverrideNanos) {
    // this.iterationElapsedTimeOverrideNanos =
    // iterationElapsedTimeOverrideNanos;
    // }

    public long getIterationDelay() {
        return iterationDelay / 1000000;
    }

    /**
     * Normally the WorkerThread will iterate as fast as possible, but if you set an iteration delay the thread will sleep for this long in between
     * iterations. Setting this to zero will turn off the sleep again. Note this will only take affect during the next iteration.
     *
     * @param iterationDelay
     */
    public void setIterationDelay(long iterationDelay) {
        this.iterationDelay = iterationDelay * 1000000;
    }

    public long getIterationDelayNanos() {
        return iterationDelay;
    }

    public void setIterationDelayNanos(long nanos) {
        this.iterationDelay = nanos;
    }

    public Timeout getJoinTimeout() {
        return joinTimeout;
    }

    public void setJoinTimeout(Timeout joinTimeout) {
        this.joinTimeout = joinTimeout;
    }

    public long getPreIterationDelay() {
        return preIterationDelay / 1000000;
    }

    public void setPreIterationDelay(long iterationDelay) {
        this.preIterationDelay = iterationDelay * 1000000;
    }

    public long getPreIterationDelayNanos() {
        return preIterationDelay;
    }

    public void setPreIterationDelayNanos(long nanos) {
        this.preIterationDelay = nanos;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        if (this.thread != null) {
            this.thread.setPriority(priority);
        }
    }

    public String getThreadName() {
        return threadName;
    }

    public void interupt() {
        if (thread.isAlive()) {
            thread.interrupt();
        } else {
            throw new RuntimeException("Can't interupt the thread, it isn't alive");
        }
    }

    public boolean isKeepRunningOnExceptions() {
        return keepRunningOnExceptions;
    }

    public void setKeepRunningOnExceptions(boolean keepRunningOnExceptions) {
        this.keepRunningOnExceptions = keepRunningOnExceptions;
    }

    public void join() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread " + Thread.currentThread().getName() + " was interupted whilst joined against the worker thread",
                                           e);
            }
        }
    }

    public void removeListener(WorkerThreadListener listener) {
        listeners.remove(listener);
    }

    public void setName(String name) {
        thread.setName(name);
    }

    public void setThreadContextClassLoader(ClassLoader classLoader) {
        thread.setContextClassLoader(classLoader);
    }

    public void sleep(long time, TimeUnit units) {
        try {
            units.sleep(time);
        } catch (InterruptedException e) {
            if (isRunning()) {
                throw new RuntimeException(String.format("Interupted whilst sleeping"), e);
            }
        }
    }

    public void startDaemon() {
        thread.setDaemon(true);
        start();
    }

    public synchronized void start() {
        keepRunning = true;
        thread.setPriority(priority);
        thread.start();
    }

    public void stop() {
        stop(null);
    }

    /**
     * Stop this thread and execute the stopTask provided. This avoids potential race conditions where you maybe while(...) on something else that
     * needs to be controlled during the stop process. Here is an example: <ul> <li>In your worker thread run() method you open a server socket</li>
     * <li>You have a while(socket!=null) accept() loop in there</li> <li>If for whatever reason the socket is closed would loop around and
     * rebind</li> <li>In your stop/close method you call serverSocket.close, and then thread.stop</li> <li>Trouble is, the socket closes and throws
     * an exception in the worker thread</li> <li>That thread then shoots back into worker thread and checks the kill value - which still hasn't been
     * set to true</li> <li>The worker thread loop resumes, binding back to the socket and waiting for another connection</li> <li>The first thread
     * gets another slice and calls workerThread.stop and then join</li> <li>This deadlocks as the worker thread is blocked indefinitely on the accept
     * call again</li> </ul>
     * <p/>
     * By providing a runnable instance the worker thread can set the kill flag, call the runnable and then join the thread - this ensures everything
     * is cleared up in a nice atomic fashion. The other option to using this approach is to override beforeStop(), and put your close down calls in
     * there.
     *
     * @param stopTask
     */
    public void stop(Runnable stopTask) {
        dontRunAgain();

        if (stopTask != null) {
            try {
                stopTask.run();
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, String.format("Failed to execute stop task '%s' whilst shutting down thread '%s'", stopTask, this), e);
            }
        }

        if (Thread.currentThread() != thread) {
            try {
                thread.interrupt();
                thread.join(joinTimeout.getMillis());
                if (thread.isAlive()) {
                    StackTraceElement[] stackTrace = thread.getStackTrace();
                    for (StackTraceElement stackTraceElement : stackTrace) {
                        System.out.println(stackTraceElement);
                    }
                    throw new FormattedRuntimeException("Thread failed to die within the join timeout ({} ms); thread {} is still running",
                                                        joinTimeout.getMillis(),
                                                        thread.getName());
                }
            } catch (InterruptedException e) {
                // Ignore this, it means the thread has died already.
            }
        }
    }

    public void dontRunAgain() {
        synchronized (lock) {
            keepRunning = false;
        }
    }

    public static class StopRunningException extends RuntimeException {
    }

}
