package com.logginghub.logging.generators;

public interface TimeBasedGenerator
{
    public long getInterval();
    public void onTimerFired();
}
