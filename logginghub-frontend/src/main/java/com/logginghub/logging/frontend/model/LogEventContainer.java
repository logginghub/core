package com.logginghub.logging.frontend.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

/**
 * Encapsulates a single collection of events. It provides one extra feature on
 * top of the ArrayList it wraps - it will throw away old entries once a
 * threshold has been reached.
 * 
 * @author James
 * 
 */
public class LogEventContainer implements Iterable<LogEvent>, LogEventListener {
    
    private static final Logger logger = Logger.getLoggerFor(LogEventContainer.class);
    private static final int fieldCount = 13;
    private static final int sizeOfPointer = 4;
    private static final int defaultArraySize = 10000;

    private final List<LogEventContainerListener> logEventContainerListeners = new CopyOnWriteArrayList<LogEventContainerListener>();
    private CircularArrayList<LogEvent> events = new CircularArrayList<LogEvent>(defaultArraySize);

    private long threshold = Long.MAX_VALUE;
    private long currentLevel = 0;

    public LogEventContainer() {}

    public LogEventContainer(LogEventContainer... toCopy) {
        for (LogEventContainer logEventContainer : toCopy) {
            events.addAll(logEventContainer.events);
        }
    }

    public final void addLogEventContainerListener(LogEventContainerListener listener) {
        logEventContainerListeners.add(listener);
    }

  
    public int size() {
        return events.size();
    }

    public long getCurrentLevel() {
        return currentLevel;
    }

    public void add(LogEvent... events) {
        for (LogEvent logEvent : events) {

            long size = LoggingUtils.sizeof(logEvent);

            long newSize = currentLevel + size;
            while (newSize > threshold) {
                LogEvent removed = this.events.remove();
                fireRemoved(logEvent);
                long removedSize = LoggingUtils.sizeof(removed);
                newSize -= removedSize;
            }

            fireAdded(logEvent);
            currentLevel = newSize;
            this.events.add(logEvent);
        }
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public LogEvent get(int i) {
        return events.get(i);
    }

    public void applyFilter(Filter<LogEvent> filter, LogEventContainer pass, LogEventContainer excluded) {
//        Is.swingEventThread();
        for (LogEvent logEvent : events) {
            logger.trace("Checking event against filters : '{}'", logEvent);
            if (filter.passes(logEvent)) {
                pass.add(logEvent);
            }
            else {
                excluded.add(logEvent);
            }
        }
    }

    public int indexOf(LogEvent event) {
        return events.indexOf(event);

    }

    public void clear() {
        events.clear();
        fireCleared();
    }

    public LogEvent remove() {
        return events.remove();
    } 

    @Override public Iterator<LogEvent> iterator() {
        return events.iterator();
    }

    public void add(LogEventContainer other) {
        this.events.addAll(other.events);
        if(logEventContainerListeners.size() > 0) {
            for (LogEvent logEvent : other) {
                fireAdded(logEvent);
            }
        }
    }

    public LogEvent removeAt(int index) {
        return events.remove(index);
    }

    public void sort() {
        Collections.sort(events, new Comparator<LogEvent>() {
            @Override public int compare(LogEvent a, LogEvent b) {
                return CompareUtils.compareLongs(a.getSequenceNumber(), b.getSequenceNumber());
            }
        });
    }

    private void fireAdded(LogEvent event) {
        for (LogEventContainerListener listener : logEventContainerListeners){
            listener.onAdded(event);
        }
    }

    private void fireRemoved(LogEvent event) {
        for (LogEventContainerListener listener : logEventContainerListeners){
            listener.onRemoved(event);
        }
    }
    
    private void fireCleared() {
        for (LogEventContainerListener listener : logEventContainerListeners){
            listener.onCleared();
        }
    }

    @Override public void onNewLogEvent(LogEvent event) {
        add(event);
    }
}
