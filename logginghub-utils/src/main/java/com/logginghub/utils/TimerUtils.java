package com.logginghub.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.logginghub.utils.ExceptionPolicy.Policy;

public class TimerUtils {

    public static Timer everySecond(String name, final Runnable runnable) {
        return every(name, 1, TimeUnit.SECONDS, runnable);
    }

    public static Timer every(String name, long amount, TimeUnit unit, final Runnable runnable) {
        return every(name, amount, unit, runnable, false);
    }

    public static Timer nowAndEvery(String name, long amount, TimeUnit unit, final Runnable runnable) {
        return nowAndEvery(name, amount, unit, runnable, false);
    }

    public static Timer every(String name, long amount, TimeUnit unit, final Runnable runnable, boolean daemon) {
        final ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.SystemErr);
        Timer timer = new Timer(name, daemon);
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    runnable.run();
                }
                catch (Throwable t) {
                    exceptionPolicy.handle(t, "Exception caught running timed runnable");
                    if (t instanceof Error) {
                        Error error = (Error) t;
                        throw error;                        
                    }
                }
            }
        };
        timer.schedule(task, unit.toMillis(amount), unit.toMillis(amount));
        return timer;
    }

    public static Timer nowAndEvery(String name, long amount, TimeUnit unit, final Runnable runnable, boolean daemon) {
        Timer timer = new Timer(name, daemon);
        TimerTask task = new TimerTask() {
            public void run() {
                runnable.run();
            }
        };
        timer.schedule(task, 0, unit.toMillis(amount));
        return timer;
    }

    public static Timer untilItDoesntThrow(String name, long pauseBetweenFailures, final Runnable runnable, final ExceptionHandler handler) {
        final Timer timer = new Timer(name, true);
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    runnable.run();
                    timer.cancel();
                }
                catch (Exception e) {
                    // Keep on plugging away
                    handler.handleException("Exception caught executing runnable", e);
                }
            }
        };
        timer.schedule(task, pauseBetweenFailures, pauseBetweenFailures);
        return timer;
    }

    public static Timer once(long amount, TimeUnit unit, final Runnable runnable) {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                runnable.run();
            }
        };
        timer.schedule(task, unit.toMillis(amount));
        return timer;
    }

    public static void killJVM(long amount, TimeUnit units) {
        once(amount, units, new Runnable() {
            public void run() {
                Logger.getAnonymousLogger().warning("Kill JVM timer has fired");
                System.exit(0);
            }
        });
    }

    public static void repeatFor(int amount, TimeUnit units, Runnable runnable) {
        long startTimeMillis = System.currentTimeMillis();
        long endTimeMillis = startTimeMillis + units.toMillis(amount);
        while (System.currentTimeMillis() < endTimeMillis) {
            runnable.run();
        }
    }

    public static Timer every(String string, Timeout timeout, Runnable runnable) {
        return every(string, timeout.getMillis(), TimeUnit.MILLISECONDS, runnable);
    }
}
