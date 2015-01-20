package com.logginghub.logging;

import java.util.Timer;
import java.util.TimerTask;

import com.logginghub.utils.MemorySnapshot;

public abstract class HeapLogger {
    private Timer timer;
    private long interval = 1000;
    private MemorySnapshot snapshot = MemorySnapshot.createSnapshot();

    public HeapLogger() {

    }

    public void start() {
        timer = new Timer("HeapLoggingTimer", true);

        TimerTask task = new TimerTask() {
            @Override public void run() {
                checkHeapStats();
            }
        };

        timer.schedule(task, interval, interval);
    }

    protected void checkHeapStats() {
        snapshot.refreshSnapshot();

        String format = format(snapshot.getUsed(),
                               snapshot.getAvailable(),
                               snapshot.getFreeMemory(),
                               snapshot.getTotalMemory(),
                               snapshot.getMaxMemory());
        log(format);
    }

    protected abstract void log(String message);


    public static String format(long used, long available, long freeMemory, long totalMemory, long maxMemory) {
        return String.format("Heap status : used memory %d, available memory %d, free memory %d, total memory %d, max memory %d",
                             used,
                             available,
                             freeMemory,
                             totalMemory,
                             maxMemory);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
