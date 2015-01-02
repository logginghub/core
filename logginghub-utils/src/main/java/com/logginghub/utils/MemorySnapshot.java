package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

public class MemorySnapshot {
    private long freeMemory;
    private long totalMemory;
    private long maxMemory;
    private long available;
    private long used;

    private static final Logger logger = Logger.getLoggerFor(MemorySnapshot.class);

    public static MemorySnapshot createSnapshot() {
        MemorySnapshot snapshot = new MemorySnapshot();
        snapshot.refreshSnapshot();
        return snapshot;
    }

    public void refreshSnapshot() {
        Runtime runtime = Runtime.getRuntime();

        freeMemory = runtime.freeMemory();
        totalMemory = runtime.totalMemory();
        maxMemory = runtime.maxMemory();

        available = (maxMemory - totalMemory) + freeMemory;
        used = totalMemory - freeMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getAvailable() {
        return available;
    }

    public long getUsed() {
        return used;
    }

    public float getAvailabeMemoryMb() {
        return getAvailable() / 1024f / 1024f;
    }

    public float getAvailableMemoryPercentage() {
        return 100f * ((float) getUsed() / (float) getMaxMemory());
    }

    @Override public String toString() {
        return StringUtils.format("free {} total {} max {} available {} {}%", freeMemory, totalMemory, maxMemory, getAvailabeMemoryMb(), getAvailableMemoryPercentage());
    }

    public static WorkerThread runMonitor() {
        return runMonitor(90);
    }

    public static WorkerThread runMonitor(final int threshold) {
        return WorkerThread.everySecond("Memory monitor", new Runnable() {
            public void run() {
                MemorySnapshot createSnapshot = MemorySnapshot.createSnapshot();
                if (createSnapshot.getAvailableMemoryPercentage() >= threshold) {
                    System.out.println(createSnapshot);
                }
            }
        });
    }

    public static void runMonitorToLogging() {
        runMonitorToLogging(90, null);
    }

    public static WorkerThread runMonitorToLogging(final int threshold) {
        return runMonitorToLogging(threshold, null);
    }

    public static WorkerThread runMonitorToLogging(final int threshold, final LowMemoryNotificationHandler handler) {
        WorkerThread thread = WorkerThread.everySecond("Memory monitor", new Runnable() {
            int consecutive = 0;

            public void run() {
                MemorySnapshot createSnapshot = MemorySnapshot.createSnapshot();

                if (createSnapshot.getAvailableMemoryPercentage() >= threshold) {
                    if (handler != null) {
                        handler.onLowMemory(createSnapshot.getAvailableMemoryPercentage(), consecutive);
                    }
                    logger.warn("Heap has reached critical level : {} % free ({} consecutive)", Logger.format(createSnapshot.getAvailableMemoryPercentage()), consecutive);
                    consecutive++;
                }
                else {
                    if (consecutive > 0) {
                        logger.info("Heap has returned below critical level : {} % free", Logger.format(createSnapshot.getAvailableMemoryPercentage()));
                        consecutive = 0;
                    }
                }
            }
        });
        return thread;
    }

    public interface LowMemoryNotificationHandler {
        void onLowMemory(float percentage, int consecutive);
    }

}
