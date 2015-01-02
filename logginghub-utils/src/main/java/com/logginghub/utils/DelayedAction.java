package com.logginghub.utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.logginghub.utils.logging.Logger;

public class DelayedAction {

    private Timer activeTimer = null;
    private TimerTask activeTask = null;
    private long interval;
    private ExceptionHandler exceptionHandler = null;

    private static final Logger logger = Logger.getLoggerFor(DelayedAction.class);

    public DelayedAction(long amount, TimeUnit units) {
        interval = units.toMillis(amount);
    }

    public synchronized void execute(final Runnable runnable) {
        if (activeTimer == null) {
            activeTimer = new Timer("DelayedActionTimer", true);
        }

        // Cancel the existing task if there is one in progress
        if (activeTask != null) {
            activeTask.cancel();
        }

        activeTask = new TimerTask() {
            @Override public void run() {
                try {
                    runnable.run();
                }
                catch (RuntimeException re) {
                    logger.warn(re, "Failed to execute delayed runnable");
                    if(exceptionHandler != null) {
                        exceptionHandler.handleException("Executing delayed action runnable", re);
                    }
                }
            }
        };

        activeTimer.schedule(activeTask, interval);
    }
    
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

}
