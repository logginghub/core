package com.logginghub.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Used to compare the list of running threads between two points to check for thread leaks.
 */
public class ThreadUtilsSnapshot {

    private int threadsBefore;
    private Thread[] threadsBeforeArray;

    public void capture() {
        threadsBefore = Thread.activeCount();
        threadsBeforeArray = new Thread[threadsBefore];
        Thread.enumerate(threadsBeforeArray);
    }

    public WorkerThread startThreadMonitor() {
        final Set<Thread> threadsBefore = new HashSet<Thread>();

        WorkerThread thread = WorkerThread.everyNowDaemon("ThreadMonitor", 10, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                int threadsAfter = Thread.activeCount();
                Thread[] threadsAfterArray = new Thread[threadsAfter];
                Thread.enumerate(threadsAfterArray);

                HashSet<Thread> nowThreads = new HashSet<Thread>();
                for (int i = 0; i < threadsAfterArray.length; i++) {
                    Thread threadAfter = threadsAfterArray[i];
                    if (threadAfter != null) {
                        nowThreads.add(threadAfter);
                    }
                }

                StringUtils.StringUtilsBuilder stringBuilder = new StringUtils.StringUtilsBuilder();

                for (Thread thread : nowThreads) {
                    if (!threadsBefore.contains(thread)) {
                        System.out.println(StringUtils.format(" | Thread started : {} | daemon={}",
                                thread.getName(),
                                thread.isDaemon()));
                    }
                }

                for (Thread thread : threadsBefore) {
                    if (!nowThreads.contains(thread)) {
                        System.out.println(StringUtils.format(" | Thread died    : {} | daemon={}",
                                thread.getName(),
                                thread.isDaemon()));
                    }
                }

                threadsBefore.clear();
                threadsBefore.addAll(nowThreads);

            }
        });

        return thread;
    }

    public Result<String> compareVsSnapshot() {

        waitForThreadDeath();

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        HashSet<Thread> threadsBefore = new HashSet<Thread>();
        for (int i = 0; i < threadsBeforeArray.length; i++) {
            threadsBefore.add(threadsAfterArray[i]);
        }

        HashSet<Thread> nonDaemonAfterThreads = new HashSet<Thread>();
        for (int i = 0; i < threadsAfterArray.length; i++) {
            Thread threadAfter = threadsAfterArray[i];
            if (!threadAfter.isDaemon()) {
                nonDaemonAfterThreads.add(threadAfter);
            }
        }

        Result<String> result;

        if (this.threadsBefore != threadsAfter) {
            StringUtils.StringUtilsBuilder stringBuilder = new StringUtils.StringUtilsBuilder();
            stringBuilder.appendLine("Threads before {} threads after {}", this.threadsBefore, threadsAfter);

            boolean hasFailureOccured = false;
            Iterator<Thread> iterator = nonDaemonAfterThreads.iterator();

            while (iterator.hasNext()) {
                Thread afterThread = (Thread) iterator.next();

                // jshaw - special check in for running with a lot of TestNG worker threads
                if (!threadsBefore.contains(afterThread) && !afterThread.getName().equals("TestNG")) {
                    stringBuilder.appendLine("Thread: '" + afterThread.getName() + "'");
                    hasFailureOccured = true;
                }
            }

            if(hasFailureOccured) {
                result = new Result<String>(stringBuilder.toString());
                result.setState(Result.State.Unsuccessful);
            }else{
                result = new Result<String>("Success");
                result.setState(Result.State.Successful);
            }
        }
        else{
            result = new Result<String>("Wrong number of threads");
            result.setState(Result.State.Unsuccessful);
        }

        return result;

    }

    public void waitForThreadDeath() {
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return haveThreadsDied();
            }
        });
    }


    public boolean haveThreadsDied() {

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        HashSet<Thread> threadsBefore = new HashSet<Thread>();
        for (int i = 0; i < threadsBeforeArray.length; i++) {
            threadsBefore.add(threadsAfterArray[i]);
        }

        HashSet<Thread> nonDaemonAfterThreads = new HashSet<Thread>();
        for (int i = 0; i < threadsAfterArray.length; i++) {
            Thread threadAfter = threadsAfterArray[i];
            if (!threadAfter.isDaemon()) {
                nonDaemonAfterThreads.add(threadAfter);
            }
        }

        boolean threadsOk = true;

        if (this.threadsBefore != threadsAfter) {
            Iterator<Thread> iterator = nonDaemonAfterThreads.iterator();

            while (iterator.hasNext()) {
                Thread afterThread = (Thread) iterator.next();

                // jshaw - special check in for running with a lot of TestNG worker threads
                if (!threadsBefore.contains(afterThread) && !afterThread.getName().equals("TestNG")) {
                    Out.err("WARNING : Thread still running: '{}'", afterThread.getName());
                    threadsOk = false;
                }
            }
        }

        return threadsOk;

    }

}
