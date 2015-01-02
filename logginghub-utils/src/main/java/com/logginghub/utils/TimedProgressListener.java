package com.logginghub.utils;

import java.util.Timer;
import java.util.logging.Logger;

public class TimedProgressListener implements Runnable, ProgressListener
{
    private Timer timer;
    private Snapshot snapshot;
    private static Logger logger = Logger.getLogger(TimedProgressListener.class.getName());
    private long target;
    
    public TimedProgressListener(long target)
    {
        this.target = target;
    }

    public void start()
    {
        timer = TimerUtils.everySecond("TimedProgresListener", this);
        snapshot = new Snapshot();
        snapshot.setTarget(target);
    }

    public void stop()
    {
        timer.cancel();
    }

    public void run()
    {
        snapshot.snapshot();
        logger.info(String.format("%s transfered at a rate of %s/sec, %.2f%% complete, %.2f seconds remaining",
                                  StringUtils.formatBytes(snapshot.getTotal()),
                                  StringUtils.formatBytes(snapshot.getRate()),
                                  snapshot.getPercentageComplete(),
                                  snapshot.getSecondsRemaining()));
    }

    public void onProgress(int bytesTransfered)
    {
        snapshot.increment(bytesTransfered);
    }
}
