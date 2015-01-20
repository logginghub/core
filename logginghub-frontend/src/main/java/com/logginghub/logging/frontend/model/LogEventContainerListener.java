package com.logginghub.logging.frontend.model;

import com.logginghub.logging.LogEvent;

public interface LogEventContainerListener {
    void onAdded(LogEvent event);
    void onPassedFilter(LogEvent event);
    void onRemoved(LogEvent removed);
    void onCleared();
}
