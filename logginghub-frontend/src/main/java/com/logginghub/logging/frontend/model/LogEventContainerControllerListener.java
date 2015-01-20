package com.logginghub.logging.frontend.model;

import com.logginghub.logging.LogEvent;

public interface LogEventContainerControllerListener {
    void onAdded(LogEvent event, boolean playing, boolean passedFilter);
    void onRemoved(LogEvent event);
    void onPlayed(LogEventContainer pausedEventsThatFailedFilter);
}
