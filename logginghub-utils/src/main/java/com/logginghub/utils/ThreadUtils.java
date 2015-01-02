package com.logginghub.utils;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadUtils {
    private static final long SLEEP_PRECISION = TimeUnit.MILLISECONDS.toNanos(2);
    private static final long SPIN_YIELD_PRECISION = TimeUnit.NANOSECONDS.toNanos(10000);

    private static Random random = new Random();

    public static void randomSleep(long greaterThatMillis, long lessThanMillis) {
        long time = greaterThatMillis + random.nextInt((int) (lessThanMillis - greaterThatMillis));
        sleep(time);
    }

    public static void sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
//                throw new RuntimeException("Interupted during sleep", e);
            }
        }
    }

    public static void sleep(int milliseconds, int nanoseconds) {
        try {
            Thread.sleep(milliseconds, nanoseconds);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
//            throw new RuntimeException("Interupted during sleep", e);
        }
    }

    // http://stackoverflow.com/questions/5274619/investigation-of-optimal-sleep-time-calculation-in-game-loop
    public static void sleepNanos(long nanoDuration) throws InterruptedException {
        final long end = System.nanoTime() + nanoDuration;
        long timeLeft = nanoDuration;
    
        do {

            if (timeLeft > SLEEP_PRECISION) {
                Thread.sleep(1);
            }
            else if (timeLeft > SPIN_YIELD_PRECISION) {
                Thread.sleep(0);
            }

            timeLeft = end - System.nanoTime();

            if (Thread.interrupted()) throw new InterruptedException();

        }
        while (timeLeft > 0);

    }

    /**
     * @deprecated Should be using worker threads for everything really
     * @param runnable
     * @return
     */
    @Deprecated
    public static StoppableThread runWithDelay(String string, final long delay, final Runnable runnable) {
        StoppableThread thread = new StoppableThread(new Runnable() {
            public void run() {
                while (true) {
                    runnable.run();
                    ThreadUtils.sleep(delay);
                }
            }
        }, string);
        thread.start();
        return thread;
    }

    /**
     * @deprecated Should be using worker threads for everything really
     * @param runnable
     * @return
     */
    @Deprecated
    public static Future<?> execute(String name, Runnable runnable) {
        final Thread thread = new Thread(runnable, name);

        Future<?> future = new Future<Void>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return !thread.isAlive();
            }

            public Void get() throws InterruptedException, ExecutionException {
                thread.join();
                return null;
            }

            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                thread.join(unit.toMillis(timeout));
                if (thread.isAlive()) {
                    throw new TimeoutException("Thread is still alive after timeout");
                }
                return null;

            }
        };

        thread.start();

        return future;
    }

    /**
     * @deprecated Should be using worker threads for everything really
     * @param runnable
     * @return
     */
    @Deprecated
    public static Future<?> executeForever(final Runnable runnable) {
        final AtomicBoolean keepRunning = new AtomicBoolean(true);
        final Future<?> future = Executors.newFixedThreadPool(1).submit(new Runnable() {
            public void run() {
                while (keepRunning.get()) {
                    runnable.run();
                }
            }
        });

        Future<?> realFuture = new Future<Object>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                keepRunning.set(false);
                return future.cancel(mayInterruptIfRunning);
            }

            public boolean isCancelled() {
                return future.isCancelled();
            }

            public boolean isDone() {
                return future.isDone();
            }

            public Object get() throws InterruptedException, ExecutionException {
                return future.get();
            }

            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get(timeout, unit);
            }

        };

        return realFuture;

    }

    /**
     * @deprecated Should be using worker threads for everything really
     * @param runnable
     * @return
     */
    @Deprecated
    public static <T> Future<T> execute(Callable<T> callable) {
        Future<T> future = Executors.newFixedThreadPool(1).submit(callable);
        return future;
    }

    public static void untilTrue(String message, long amount, TimeUnit units, Callable<Boolean> callable) {
        long maxDuration = units.toMillis(amount);
        long start = System.currentTimeMillis();

        boolean value = false;
        while (!value && System.currentTimeMillis() - start < maxDuration) {
            try {
                value = callable.call();
            }
            catch (Exception e) {
                throw new RuntimeException(message + " : callable threw an exception", e);
            }

            ThreadUtils.sleep(50);
        }

        if (!value) {
            throw new RuntimeException(message + " : timed out wait for function to return true");
        }
    }

    public static void untilTrue(int amount, TimeUnit units, Callable<Boolean> callable) {
        untilTrue("", amount, units, callable);
    }
    
    public static void untilTrue(Callable<Boolean> callable) {
        untilTrue("", Timeout.defaultTimeout.getTime(), Timeout.defaultTimeout.getUnits(), callable);
    }

    public static void repeatUntilTrue(Callable<Boolean> callable) {
        untilTrue(callable);
    }

    public static boolean isThreadDead(String name) {
        return !isThreadAlive(name);
    }

    public static boolean isThreadAlive(String name) {

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        boolean threadAlive = false;

        for (Thread thread : threadsAfterArray) {
            if(thread.getName().equals(name) && thread.isAlive()) {
                threadAlive= true;
                break;
            }
        }

        return threadAlive;
    }
}
