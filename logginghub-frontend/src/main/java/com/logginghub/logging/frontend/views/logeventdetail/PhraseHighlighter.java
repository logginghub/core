package com.logginghub.logging.frontend.views.logeventdetail;

import java.awt.Color;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.filters.EventContainsFilter;
import com.logginghub.logging.frontend.configuration.RowFormatConfiguration;
import com.logginghub.logging.frontend.model.RowFormatModel;
import com.logginghub.utils.filter.Filter;

public class PhraseHighlighter implements RowHighlighter {
    private Filter<LogEvent> filter;
    private RowHighlighter decorated = new LevelHighlighter(RowFormatModel.fromConfiguration(new RowFormatConfiguration()));
    private Color highlightBackgroundColour = Color.magenta;
    private Color highlightForegroundColor = Color.black;

    public PhraseHighlighter(String phrase) {
        filter = new EventContainsFilter(phrase);
    }
    
    public PhraseHighlighter(Filter<LogEvent> filter) {
        this.filter = filter;
    }

    public void updateSettings(HighlightSettings settings, int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event) {
        if (filter.passes(event)) {
            settings.setBackground(highlightBackgroundColour);
            settings.setForeground(highlightForegroundColor);
        }
    }

    public Filter<LogEvent> getFilter() {
        return filter;
    }

    public boolean isInterested(int rowIndex, int colIndex, boolean isSelected, boolean isBookmarked, LogEvent event) {
        boolean interested = filter.passes(event);
        return interested;
    }

    public void setHighlightBackgroundColour(Color background) {
        highlightBackgroundColour = background;
    }

    public void setHighlightForegroundColour(Color foreground) {
        highlightForegroundColor = foreground;
    }
}
