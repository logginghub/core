package com.logginghub.logging.frontend.views.logeventdetail.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.modules.Indexifier;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class TimeModel extends Observable {

    private static final Logger logger = Logger.getLoggerFor(TimeModel.class);

    private ObservableLong currentTime = createLongProperty("currentTime", System.currentTimeMillis());

    private ObservableLong viewStart = createLongProperty("start", 0);

    // jshaw - not 100% happy with this being in the model, it means its directly coupled to a
    // single view. Its more a view-model than a pure-model.
    private ObservableInteger viewWidth = new ObservableInteger(0);

    private ObservableLong clickedTime = createLongProperty("clickedTime", Long.MIN_VALUE);
    private ObservableLong selectionStart = createLongProperty("selectionStart", Long.MAX_VALUE);
    private ObservableLong selectionEnd = createLongProperty("selectionEnd", Long.MAX_VALUE);

    private ObservableInteger selectionChanged = new ObservableInteger(0);

    private ObservableInteger selectionCleared = new ObservableInteger(0);

    private ObservableLong interval = createLongProperty("period", TimeUtils.seconds);

    private ObservableProperty<Boolean> autoscroll = createBooleanProperty("autoscroll", true);

    private Indexifier indexifier;

    private ObservableInteger changes = new ObservableInteger(0);

    public TimeModel() {
        indexifier = new Indexifier(1000);
    }

    public TimeModel(long period) {
        indexifier = new Indexifier(period);
    }

    public ObservableInteger getChanges() {
        return changes;
    }

    public void notifyChanged() {
        changes.increment();
    }

    public Indexifier getIndexifier() {
        return indexifier;
    }

    public void setIndexifier(Indexifier indexifier) {
        this.indexifier = indexifier;
    }

    public ObservableLong getViewStart() {
        return viewStart;
    }

    public void resetSelection() {
        selectionStart.set(Long.MAX_VALUE);
        selectionEnd.set(Long.MAX_VALUE);
        selectionChanged.increment();
    }

    public ObservableInteger getSelectionChanged() {
        return selectionChanged;
    }

    public ObservableLong getInterval() {
        return interval;
    }

    public ObservableLong getSelectionStart() {
        return selectionStart;
    }

    public ObservableLong getSelectionEnd() {
        return selectionEnd;
    }

    public void removeEvent(LogEvent event) {
        indexifier.removeEvent(event);
    }

    private Throttler modelUpdateThrottle = new Throttler(50, TimeUnit.MILLISECONDS);

    public void addEvent(LogEvent event) {
        indexifier.addEvent(event);

        if (modelUpdateThrottle.isOkToFire()) {
            changes.increment();
        }
    }

    public Map<Long, HistoricalIndexElement> getCounts() {
        return indexifier.getCounts();
    }

    public HistoricalIndexElement getMaximumCount() {
        return indexifier.getMaximumCount();
    }

    public ObservableLong getClickedTime() {
        return clickedTime;
    }

    public void clearEvents() {
        indexifier.clearEvents();
    }

    public ObservableInteger getSelectionCleared() {
        return selectionCleared;
    }

    public void process(HistoricalIndexResponse response) {
        indexifier.process(response);
    }

    public void process(HistoricalIndexElement element) {
        indexifier.process(element);
    }

    public void moveToEarliestTime(long buffer) {

        long earliestTime = indexifier.getEarliestTime();
        logger.info("Moving to time '{}'", Logger.toDateString(earliestTime));
        viewStart.set(earliestTime - buffer);
    }

    public void processUpdate(HistoricalIndexResponse element) {
        indexifier.processUpdate(element);
    }

    public ObservableProperty<Boolean> getAutoscroll() {
        return autoscroll;
    }

    public ObservableLong getCurrentTime() {
        return currentTime;
    }
    
    public ObservableInteger getViewWidth() {
        return viewWidth;
    }
}
