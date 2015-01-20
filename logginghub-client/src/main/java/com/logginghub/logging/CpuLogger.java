package com.logginghub.logging;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.logginghub.utils.MemorySnapshot;

public abstract class CpuLogger {
    // private Logger logger = Logger.getLogger(HeapLogger.class);
    private Timer timer;
    private long interval = 1000;
    private MemorySnapshot snapshot = MemorySnapshot.createSnapshot();
    // private ThreadTimes times;
    public long totalUserTime;
    public long totalCPUTime;

    public long totalCPUTimeDelta;
    public long totalUserTimeDelta;

    // private final long threadId;
    private final HashMap<Long, Times> history = new HashMap<Long, Times>();
    private long lastTime;
    private long totalProcessTime;
    private long totalProcessTimeDelta;

    private NumberFormat numberFormat = NumberFormat.getNumberInstance();

    private int avilableProcessors = Runtime.getRuntime().availableProcessors();

    // private static Logger logger = Logger.getLogger(CpuLogger.class);
    // private static Logger preThreadLogger = Logger.getLogger(CpuLogger.class.getName() +
    // "-perThread");

    public CpuLogger() {

    }

    public void start() {
        // times = new ThreadTimes(1000);
        // times.start();

        timer = new Timer("CpuLoggingTimer", true);

        TimerTask task = new TimerTask() {
            @Override public void run() {
                doLogging();
            }
        };

        timer.schedule(task, interval, interval);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void doLogging() {
        update();
    }

    public static float toMilliseconds(long nanos) {
        return nanos / 1000000f;
    }

    private class Times {
        public long id;
        public long lastCpuTime;
        public long lastUserTime;
    }

    // /** Create a polling thread to track times. */
    // public ThreadTimes(final long interval)
    // {
    // super("Thread time monitor");
    // this.interval = interval;
    // threadId = getId();
    // setDaemon(true);

    // final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    // boolean threadCpuTimeEnabled = bean.isThreadCpuTimeEnabled();
    // boolean threadCpuTimeSupported = bean.isThreadCpuTimeSupported();
    // System.out.println(threadCpuTimeEnabled);
    // System.out.println(threadCpuTimeSupported);
    // }

    // /** Run the thread until interrupted. */
    // public void run()
    // {
    // while (!isInterrupted())
    // {
    //
    // try
    // {
    // sleep(interval);
    // }
    // catch (InterruptedException e)
    // {
    // break;
    // }
    // }
    // }

    private Map<String, String> cleanThreadNames = new HashMap<String, String>();
    private boolean displayPerThreadDetails = false;

    /** Update the hash table of thread times. */
    private void update() {
        // System.out.println("---------------------------");
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        final long[] ids = bean.getAllThreadIds();

        StringBuilder builder = new StringBuilder();
        // builder.append("Per thread CPU times: ");

        long tempTotalOtherTime = 0;
        long tempTotalUserTime = 0;

        for (long id : ids) {
            // if (id == threadId) continue; // Exclude polling thread
            final long c = bean.getThreadCpuTime(id);
            final long u = bean.getThreadUserTime(id);

            tempTotalOtherTime += c;
            tempTotalUserTime += u;

            if (displayPerThreadDetails) {
                if (c == -1 || u == -1) continue; // Thread died

                Times times = history.get(id);
                if (times == null) {
                    times = new Times();
                    times.id = id;
                    history.put(id, times);
                }
                else {
                    long cpuDelta = c - times.lastCpuTime;
                    long userDelta = u - times.lastUserTime;

                    float cpuDeltaMS = CpuLogger.toMilliseconds(cpuDelta);
                    float userDeltaMS = CpuLogger.toMilliseconds(userDelta);

                    String threadName = bean.getThreadInfo(id).getThreadName();

                    String cleanThreadName = cleanThreadNames.get(threadName);
                    if (cleanThreadName == null) {
                        cleanThreadName = threadName;
                        // Clean up the name so it doesn't have any of our
                        // parsing
                        // chars in it
                        cleanThreadName = cleanThreadName.replace("[", "(");
                        cleanThreadName = cleanThreadName.replace("]", ")");
                        cleanThreadName = cleanThreadName.replace(",", " ");
                        cleanThreadNames.put(threadName, cleanThreadName);
                    }

                    builder.append('[')
                           .append(cleanThreadName)
                           .append(',')
                           .append(numberFormat.format(cpuDeltaMS))
                           .append(',')
                           .append(numberFormat.format(userDeltaMS))
                           .append(']');
                }

                times.lastCpuTime = c;
                times.lastUserTime = u;
            }
        }

        long jvmCpuTime = getJVMCpuTime();

        totalCPUTimeDelta = tempTotalOtherTime - this.totalCPUTime;
        totalUserTimeDelta = tempTotalUserTime - this.totalUserTime;
        totalProcessTimeDelta = jvmCpuTime - this.totalProcessTime;

        totalCPUTime = tempTotalOtherTime;
        totalUserTime = tempTotalUserTime;
        totalProcessTime = jvmCpuTime;

        long timeNow = System.currentTimeMillis();
        if (lastTime > 0) {
            long elapsed = timeNow - lastTime;

            float totalMS = elapsed * avilableProcessors;

            float percentageCPU = 100f * CpuLogger.toMilliseconds(totalCPUTimeDelta) / totalMS;
            float percentageUser = 100f * CpuLogger.toMilliseconds(totalUserTimeDelta) / totalMS;

            float percentageDodgyProcessTime = 100f * CpuLogger.toMilliseconds(totalProcessTimeDelta) / totalMS;

            // jshaw - there is a weird but where these numbers come out
            // negative sometimes
            if (percentageCPU >= 0 && percentageUser >= 0) {
                log(String.format("Summary Cpu stats : %.2f %% (%.2f %% user %.2f %% system) JVM process %.2f %%",
                                  percentageCPU,
                                  percentageUser,
                                  percentageCPU - percentageUser,
                                  percentageDodgyProcessTime));

                if (displayPerThreadDetails) {
                    log("Per thread Cpu stats : " + builder.toString());
                }
            }
        }

        lastTime = timeNow;
    }

    protected abstract void log(String message);

    public long getJVMCpuTime() {
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (!(bean instanceof com.sun.management.OperatingSystemMXBean)) return 0L;
        return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuTime();
    }

    public void setDisplayPerThreadDetails(boolean displayPerThreadDetails) {
        this.displayPerThreadDetails = displayPerThreadDetails;
    }

    // /** Get total CPU time so far in nanoseconds. */
    // public long getTotalCpuTime()
    // {
    // final Collection<Times> hist = history.values();
    // long time = 0L;
    // for (Times times : hist)
    // time += times.endCpuTime - times.startCpuTime;
    // return time;
    // }
    //
    // /** Get total user time so far in nanoseconds. */
    // public long getTotalUserTime()
    // {
    // final Collection<Times> hist = history.values();
    // long time = 0L;
    // for (Times times : hist)
    // time += times.endUserTime - times.startUserTime;
    // return time;
    // }
    //
    // /** Get total system time so far in nanoseconds. */
    // public long getTotalSystemTime()
    // {
    // return getTotalCpuTime() - getTotalUserTime();
    // }
}
