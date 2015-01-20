package com.logginghub.logging.frontend.modules;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class VMPauseDetectorModule implements Asynchronous {

    private WorkerThread thread;
    private long interval = 500;
    private long threshold = 1;

    private static final Logger logger = Logger.getLoggerFor(VMPauseDetectorModule.class);

    @Override public void start() {
        stop();

        this.thread = WorkerThread.executeDaemonOngoing("LoggingHub-PauseDetector", interval, new Runnable() {
            long lastTime = 0;

            @Override public void run() {
                long now = System.currentTimeMillis();
                if (lastTime != 0) {
                    long elapsed = now - lastTime;
                    if (elapsed > (interval + threshold)) {
                        logger.info("PauseChecker waited {} ms - {} ms greater than the target delay of {} ms", elapsed, elapsed - interval, interval);
                    }
                }

                lastTime = now;

            }
        });
    }

    @Override public void stop() {
        if (thread != null) {
            thread.stop();
        }
    }

}
