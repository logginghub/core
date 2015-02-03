package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.LogEvent;

public interface RowHighlighter {
    boolean isInterested(int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event);
    void updateSettings(HighlightSettings currentSettings, int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event);
}
