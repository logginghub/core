package com.logginghub.logging.frontend.views.detail.time;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.model.LogEventContainer;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.frontend.model.LogEventContainerControllerListener;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.modules.Indexifier;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

public class TimeController {

    private boolean firstEvent = true;
    private TimeModel model;

    private static final Logger logger = Logger.getLoggerFor(TimeController.class);

    private Filter<LogEvent> filter = new Filter<LogEvent>() {
        @Override public boolean passes(LogEvent event) {
            boolean passes;

            long start = model.getSelectionStart().longValue();
            long end = model.getSelectionEnd().longValue();
            if (start != Long.MAX_VALUE && end != Long.MAX_VALUE) {
                long eventTime = event.getOriginTime();
                passes = eventTime >= start && eventTime < end;
            }
            else {
                passes = true;
            }

            return passes;
        }
    };
    private LogEventContainerControllerListener controllerListener;
    private LogEventContainerController eventController;

    public TimeController(final TimeModel model, LogEventContainerController eventController) {
        this.model = model;
        this.eventController = eventController;

        controllerListener = new LogEventContainerControllerListener() {
            @Override public void onAdded(LogEvent event, boolean playing, boolean passedFilter) {
                if (playing) {
                    model.addEvent(event);
                }
            }

            @Override public void onRemoved(LogEvent event) {
                model.removeEvent(event);
            }

            @Override public void onPlayed(LogEventContainer pausedEventsThatFailedFilter) {
                for (LogEvent logEvent : pausedEventsThatFailedFilter) {
                    model.addEvent(logEvent);
                }
            }
        };
        eventController.addLogEventContainerListener(controllerListener);

    }

    public void detachControllerListener() {
        eventController.removeLogEventContainerListener(controllerListener);
    }

    public TimeModel getModel() {
        return model;
    }

    public Filter<LogEvent> getFilter() {
        return filter;
    }

    public void addEvent(LogEvent event) {
        model.addEvent(event);

        if (firstEvent) {

            model.getViewStart().set(event.getOriginTime());

            firstEvent = false;
        }
    }

    public void updateSelection(long filterStart, long filterEnd) {
        model.getAutoscroll().set(false);
        model.getSelectionStart().set(filterStart);
        model.getSelectionEnd().set(filterEnd);
        model.getSelectionChanged().increment();
    }

    public void clearSelection() {       
        long start = model.getSelectionStart().longValue();
        long end = model.getSelectionEnd().longValue();
        if (start != Long.MAX_VALUE && end != Long.MAX_VALUE) {
            model.getSelectionStart().set(Long.MAX_VALUE);
            model.getSelectionEnd().set(Long.MAX_VALUE);
            model.getSelectionChanged().increment();
        }
    }

    public void timeClicked(long time) {
        model.getClickedTime().set(time);
    }

    public void clearClickedTime() {
        model.getClickedTime().set(Long.MAX_VALUE);
    }

    public void clearAndPlay() {
        clearClickedTime();
        clearSelection();
        model.getAutoscroll().set(true);
        model.getSelectionCleared().increment();
    }

    public void processHistoricalIndex(HistoricalIndexResponse response) {
        model.process(response);
    }

    public void setPeriod(long interval) {

        long currentInterval = model.getInterval().longValue();

        model.getInterval().set(interval);

        logger.info("Changing interval from '{}' to '{}'",
                    TimeUtils.formatIntervalMilliseconds(currentInterval),
                    TimeUtils.formatIntervalMilliseconds(interval));

        if (interval > currentInterval) {
            // We can use the current data to build the next interval up

            Indexifier indexifier = new Indexifier(interval);

            Map<Long, HistoricalIndexElement> counts = model.getCounts();
            Set<Entry<Long, HistoricalIndexElement>> entrySet = counts.entrySet();
            for (Entry<Long, HistoricalIndexElement> entry : entrySet) {
                indexifier.add(entry.getKey(), entry.getValue());
            }

            // Replace the data
            model.setIndexifier(indexifier);

            // TODO : we should keep hold of the higher resolution raw data

        }
        else {
            // We either need to re-request the more detailed info, or fall back on a version of it
            // we have cached
            // throw new NotImplementedException();
        }

        // TODO : update the underlying data based on the new interval
        // updateModel();

        model.notifyChanged();
    }

    public void processUpdate(HistoricalIndexResponse response) {
        model.processUpdate(response);
    }

    public void moveToEarliestTime(long before) {
        model.moveToEarliestTime(before);
    }

    public void updateCurrentTime(long time) {
        model.getCurrentTime().set(time);
        
        if(model.getAutoscroll().asBoolean()) {
            // Keep the current time indicator slightly away from the right hand side
            
            long timeFromStart = time - model.getViewStart().longValue();
            long interval = model.getInterval().longValue();
            int pixels = (int) (timeFromStart / interval);
            
            int viewWidthPixels = model.getViewWidth().intValue();
            
            float currentWidthFactor = pixels / (float)viewWidthPixels;
            
            
            if(currentWidthFactor > 0.8f) {
             
                long happyWidthTime = (long) (0.8f * viewWidthPixels * interval);                
                long newStartTime = time - happyWidthTime;
                
                model.getViewStart().set(newStartTime);
                
            }            
            
        }
        
        
    }
}
