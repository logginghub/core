package com.logginghub.logging.frontend.analysis;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.filters.MessageIsFilter;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;
import com.logginghub.utils.filter.Filter;

public class SimpleMatcher implements LogEventListener {
    private List<ValueStripper2ResultListener> listeners = new CopyOnWriteArrayList<ValueStripper2ResultListener>();
    private Filter<LogEvent> filter;

    private String name;

    public SimpleMatcher(String pattern, String name) {
        this.name = name;

        filter = new MessageIsFilter(pattern);
    }

    public void onNewLogEvent(LogEvent entry) {
        if (filter == null || filter.passes(entry)) {
            fireNewResult(name, true, "1", entry);
        }
    }

    public void addResultListener(ValueStripper2ResultListener listener) {
        listeners.add(listener);
    }

    public void removeResultListener(ValueStripper2ResultListener listener) {
        listeners.remove(listener);
    }

    private void fireNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        for (ValueStripper2ResultListener listener : listeners) {
            listener.onNewResult(label, isNumeric, value, entry);
        }
    }
}
