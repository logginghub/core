package com.logginghub.logging.frontend.model;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.Is;
import com.logginghub.utils.Pair;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.VisualStopwatchController;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Whilst figuring out the new discard strategy for incoming events, it became clear there are four buckets: <ol> <li>1. live events that pass
 * filter</li> <li>2. live events that failed filter</li> <li>3. paused events that pass filter</li> <li>4. paused events that failed filter</li>
 * </ol>
 * <p/>
 * The new strategy is based around knowing exactly how many bytes we've allocated to events, so someone needs to keep a track and decide which bucket
 * is going get the bad news.
 *
 * @author James
 */
public class LogEventContainerController {

    private static final Logger logger = Logger.getLoggerFor(LogEventContainerController.class);
    private final List<LogEventContainerListener> logEventContainerListeners = new CopyOnWriteArrayList<LogEventContainerListener>();
    private final List<LogEventContainerControllerListener> controllerListeners = new CopyOnWriteArrayList<LogEventContainerControllerListener>();
    private long currentLevel = 0;
    private LogEventContainer liveEventsThatFailedFilter = new LogEventContainer();
    private LogEventContainer liveEventsThatPassFilter = new LogEventContainer();
    private LogEventContainer pausedEventsThatFailedFilter = new LogEventContainer();
    private LogEventContainer pausedEventsThatPassFilter = new LogEventContainer();
    private long threshold = (long) (Runtime.getRuntime().maxMemory() * 0.2f);

    public Pair<Boolean, Boolean> add(LogEvent event, Filter<LogEvent> filter, boolean isPlaying) {

        logger.fine("Log event '{}' added to log event container controller", event);
        // allEvents.add(event);

        // TODO : refactor this nasty double return struct...
        boolean isVisibile;
        boolean removedVisible = false;

        long size = LoggingUtils.sizeof(event);

        Stopwatch sw1 = Stopwatch.start("Filtering");
        boolean passes = filter.passes(event);

        VisualStopwatchController.getInstance().add(sw1);

        Stopwatch sw2 = Stopwatch.start("Adding");
        if (isPlaying) {
            if (passes) {
                Is.swingEventThread();
                liveEventsThatPassFilter.add(event);
                isVisibile = true;
            } else {
                liveEventsThatFailedFilter.add(event);
                isVisibile = false;
            }
        } else {
            if (passes) {
                pausedEventsThatPassFilter.add(event);
                isVisibile = false;

            } else {
                pausedEventsThatFailedFilter.add(event);
                isVisibile = false;
            }
        }
        VisualStopwatchController.getInstance().add(sw2);

        Stopwatch start = Stopwatch.start("Throwing away");
        long newSize = currentLevel + size;
        while (newSize > threshold) {
            LogEvent removed = null;
            if (pausedEventsThatFailedFilter.size() > 0) {
                removed = pausedEventsThatFailedFilter.remove();
            } else if (pausedEventsThatPassFilter.size() > 0) {
                removed = pausedEventsThatPassFilter.remove();
            } else if (liveEventsThatFailedFilter.size() > 0) {
                removed = liveEventsThatFailedFilter.remove();
            } else if (liveEventsThatPassFilter.size() > 0) {
                Is.swingEventThread();
                removed = liveEventsThatPassFilter.remove();
                removedVisible = true;
            } else {
                // Crap, everything was empty, so how can we be over the
                // threshold? Must have been the biggest log event ever we just
                // added, or a daftly small threshold. Ignore it.
                break;
            }

            if (removed != null) {
                newSize -= LoggingUtils.sizeof(removed);
                fireRemoved(removed);
            }
        }

        fireAdded(event);
        fireAdded(event, isPlaying, passes);
        if (passes) {
            firePassedFilter(event);
        }

        currentLevel = newSize;
        start.stop();
        VisualStopwatchController.getInstance().add(start);

        return new Pair<Boolean, Boolean>(isVisibile, removedVisible);

    }

    private void fireRemoved(LogEvent event) {
        for (LogEventContainerListener listener : logEventContainerListeners) {
            listener.onRemoved(event);
        }

        for (LogEventContainerControllerListener listener : controllerListeners) {
            listener.onRemoved(event);
        }
    }

    private void fireAdded(LogEvent event) {
        for (LogEventContainerListener listener : logEventContainerListeners) {
            listener.onAdded(event);
        }
    }

    private void fireAdded(LogEvent event, boolean playing, boolean passesFilter) {
        for (LogEventContainerControllerListener listener : controllerListeners) {
            listener.onAdded(event, playing, passesFilter);
        }
    }

    private void firePassedFilter(LogEvent event) {
        for (LogEventContainerListener listener : logEventContainerListeners) {
            listener.onPassedFilter(event);
        }
    }

    public final void addLogEventContainerListener(LogEventContainerListener listener) {
        logEventContainerListeners.add(listener);
    }

    public final void addLogEventContainerListener(LogEventContainerControllerListener listener) {
        controllerListeners.add(listener);
    }

    public void clear() {

        Is.swingEventThread();
        currentLevel = 0;
        Iterator<LogEvent> iterator = liveEventsThatPassFilter.iterator();
        while (iterator.hasNext()) {
            LogEvent logEvent = iterator.next();
            if (logEvent instanceof DefaultLogEvent) {
                DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEvent;
                if (defaultLogEvent.getMetadata().getBoolean("locked", false)) {
                    // Item is locked, dont remove it
                    currentLevel += LoggingUtils.sizeof(logEvent);
                } else {
                    iterator.remove();
                    fireRemoved(logEvent);
                }
            }
        }

        removeAll(liveEventsThatFailedFilter);
        removeAll(pausedEventsThatFailedFilter);
        removeAll(pausedEventsThatPassFilter);

        fireCleared();
    }

    private void removeAll(LogEventContainer container) {
        Iterator<LogEvent> iterator = container.iterator();
        while (iterator.hasNext()) {
            LogEvent logEvent = iterator.next();
            iterator.remove();
            fireRemoved(logEvent);
        }
    }

    private void fireCleared() {
        for (LogEventContainerListener listener : logEventContainerListeners) {
            listener.onCleared();
        }
    }

    public long getCurrentLevel() {
        return currentLevel;
    }

    public int getLiveEventsSize() {
        return liveEventsThatFailedFilter.size() + liveEventsThatPassFilter.size();
    }

    public LogEventContainer getLiveEventsThatFailedFilter() {
        return liveEventsThatFailedFilter;
    }

    public LogEventContainer getLiveEventsThatPassFilter() {
        return liveEventsThatPassFilter;
    }

    public int getPausedEventsSize() {
        return pausedEventsThatFailedFilter.size() + pausedEventsThatFailedFilter.size();

    }

    public LogEventContainer getPausedEventsThatFailedFilter() {
        return pausedEventsThatFailedFilter;
    }

    public LogEventContainer getPausedEventsThatPassFilter() {
        return pausedEventsThatPassFilter;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public double getUsedPercentage() {
        return 100d * currentLevel / (double) threshold;
    }

    public void play() {
        Is.swingEventThread();
        liveEventsThatPassFilter.add(pausedEventsThatPassFilter);

        for (LogEventContainerControllerListener listener : controllerListeners) {
            listener.onPlayed(pausedEventsThatPassFilter);
        }

        pausedEventsThatPassFilter.clear();
    }

    public void refilter(Filter<LogEvent> filters) {
        // Bug - there was a defect here as we were just throwing the events
        // back in the wrong order. We have to ensure they go back in the order
        // they arrived in, otherwise events will look like they are jumping
        // around all over the place
        // liveEventsThatPassFilter.clear();
        // liveEventsThatFailedFilter.clear();
        // allEvents.applyFilter(filters, liveEventsThatPassFilter,
        // liveEventsThatFailedFilter);

        // This might be a good way of avoiding having to have the all events
        // collection... maybe.
        LogEventContainer allLive = new LogEventContainer(liveEventsThatPassFilter, liveEventsThatFailedFilter);
        allLive.sort();

        // TODO : there is a major optimisition to be made here; if we can tell that the filter is more specific, we only need to search in the events that have already passed

        Is.swingEventThread();
        liveEventsThatPassFilter.clear();
        liveEventsThatFailedFilter.clear();
        allLive.applyFilter(filters, liveEventsThatPassFilter, liveEventsThatFailedFilter);
    }

    public LogEvent removeLiveEvent(int rowIndex) {
        Is.swingEventThread();
        LogEvent removeAt = liveEventsThatPassFilter.removeAt(rowIndex);
        fireRemoved(removeAt);
        return removeAt;
    }

    public final void removeLogEventContainerListener(LogEventContainerControllerListener listener) {
        controllerListeners.remove(listener);
    }

    public final void removeLogEventContainerListener(LogEventContainerListener listener) {
        logEventContainerListeners.remove(listener);
    }

}
