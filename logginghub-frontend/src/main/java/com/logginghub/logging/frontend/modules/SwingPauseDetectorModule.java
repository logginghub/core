package com.logginghub.logging.frontend.modules;

import javax.swing.SwingUtilities;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class SwingPauseDetectorModule implements Asynchronous {

    private WorkerThread thread;
    private long interval = 500;
    private long threshold = 500;

    private static final Logger logger = Logger.getLoggerFor(SwingPauseDetectorModule.class);

    @Override public void start() {
        stop();

        this.thread = WorkerThread.executeDaemonOngoing("LoggingHub-SwingPauseDetector", interval, new Runnable() {
            @Override public void run() {

                final long pre = System.currentTimeMillis();

                SwingUtilities.invokeLater(new Runnable() {

                    @Override public void run() {

                        long now = System.currentTimeMillis();
                        long elapsed = now - pre;

                        if (elapsed > threshold) {
                            logger.info("SwingPauseChecker waited {} ms - {} ms greater than the acceptable swing delay of {} ms",
                                        elapsed,
                                        elapsed - interval,
                                        threshold);
                        }

                    }
                });
            }
        });
    }

    @Override public void stop() {
        if (thread != null) {
            thread.stop();
        }
    }

}
