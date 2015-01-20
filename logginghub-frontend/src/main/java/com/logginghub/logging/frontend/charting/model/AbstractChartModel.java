package com.logginghub.logging.frontend.charting.model;

import java.util.TimeZone;

import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

public abstract class AbstractChartModel extends Observable {

    private ObservableProperty<String> title = createStringProperty("title", "Chart title");
    private ObservableProperty<String> layout = createStringProperty("layout", "");
    private ObservableProperty<String> resetAt = createStringProperty("resetAt", "");
    private ObservableInteger resets = createIntProperty("resets", 0);

    private ObservableList<ChartSeriesModel> matcherModels = createListProperty("matchers", ChartSeriesModel.class);

    private static final Logger logger = Logger.getLoggerFor(AbstractChartModel.class);

    private long resetAtTime = 0;

    private TimeZone timeZone = DateFormatFactory.local;

    public AbstractChartModel() {

        // Auto-parse the reset at string
        resetAt.addListener(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                // if (StringUtils.isNotNullOrEmpty(newValue)) {
                // try {
                // resetAtTime = TimeUtils.parseTime(newValue, timeZone);
                // }
                // catch (RuntimeException e) {
                // // Failed to parse
                // resetAtTime = -1;
                // }
                // }
                // else {
                // resetAtTime = -1;
                // }
                // lastResetCheck = -1;
            }
        });
    }

    public long getResetAtTime() {
        return resetAtTime;
    }
    
    public void setResetAtTime(long resetAtTime) {
        this.resetAtTime = resetAtTime;
    }

    // public long getLastResetCheck() {
    // return lastResetCheck;
    // }

    public ObservableProperty<String> getResetAt() {
        return resetAt;
    }

    public ObservableProperty<String> getTitle() {
        return title;
    }

    public ObservableProperty<String> getLayout() {
        return layout;
    }

    public boolean shouldReset(long time) {

        boolean shouldReset = time >= resetAtTime;

        logger.fine("Reset check : [{}] : time '{}' resetTime '{}' : shouldReset '{}'", 
                    getTitle().get(),
                    Logger.toLocalDateString(time),
                    Logger.toLocalDateString(resetAtTime),
                    shouldReset);

        return shouldReset;
    }

    public void reset() {
        resets.increment();
    }

    public ObservableInteger getResets() {
        return resets;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public ObservableList<ChartSeriesModel> getMatcherModels() {
        return matcherModels;
    }

}
