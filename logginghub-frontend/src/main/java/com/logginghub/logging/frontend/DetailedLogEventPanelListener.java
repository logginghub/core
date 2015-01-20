package com.logginghub.logging.frontend;

import com.logginghub.logging.LogEvent;

public interface DetailedLogEventPanelListener {
    void onCreateNewChartForEvent(LogEvent event);
    void onLevelFilterChanged(int levelFilter);
}
