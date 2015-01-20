package com.logginghub.logging.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.filter.Filter;

public class BasicEventStore implements EventStore {

    private List<LogEvent> allEvents = new ArrayList<LogEvent>();
    private List<LogEvent> visibleEvents = new ArrayList<LogEvent>();

    private Filter<LogEvent> filter;
    private int limit;

    public BasicEventStore(int limit) {
        this.limit = limit;
    }

    @Override public void add(LogEvent event) {
        allEvents.add(event);

        if (filter != null && filter.passes(event)) {
            visibleEvents.add(event);
        }

        if (allEvents.size() == limit) {
            LogEvent removed = allEvents.remove(0);

            // If this event is visible, it'll be the first item in the visible events list too
            if (visibleEvents.get(0) == removed) {
                visibleEvents.remove(0);
            }
        }

    }

    @Override public LogEvent get(int i) {
        return visibleEvents.get(i);

    }

    @Override public int getCount() {
        return visibleEvents.size();

    }

    @Override public void stream(Destination<LogEvent> destination) {
        for (LogEvent logEvent : visibleEvents) {
            destination.send(logEvent);
        }
    }

    @Override public List<LogEvent> getAll() {
        return Collections.unmodifiableList(visibleEvents);
    }

    @Override public void applyFilter(Filter<LogEvent> filter) {
        this.filter = filter;
        refilter();
    }

    private void refilter() {
        visibleEvents.clear();
        
        for (LogEvent event : allEvents) {
            if (filter == null || filter.passes(event)) {
                visibleEvents.add(event);
            }            
        }
    }

    @Override public void clearFilter() {
        this.filter = null;
        refilter();
    }

}
