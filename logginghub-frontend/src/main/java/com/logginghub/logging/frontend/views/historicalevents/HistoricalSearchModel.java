package com.logginghub.logging.frontend.views.historicalevents;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.model.TimeSelectionModel;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.utils.Stream;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 29/01/15.
 */
public class HistoricalSearchModel extends Observable {

    private ObservableInteger levelFilter = createIntProperty("levelFilter", Logger.info);

    private ObservableProperty<String> keywordFilter = createStringProperty("keywordFilter", "");

    private ObservableProperty<Boolean> searchInProgress = createBooleanProperty("searchInProgress", false);

    private final TimeSelectionModel timeSelectionModel = new TimeSelectionModel();

    private Stream<LogEvent> logEventStream = new Stream<LogEvent>();

    private LogEventContainerController events = new LogEventContainerController();

    public TimeSelectionModel getTimeSelectionModel() {
        return timeSelectionModel;
    }

    public LogEventContainerController getEvents() {
        return events;
    }

    public Stream<LogEvent> getLogEventStream() {
        return logEventStream;
    }

    public ObservableInteger getLevelFilter() {
        return levelFilter;
    }

    public ObservableProperty<Boolean> getSearchInProgress() {
        return searchInProgress;
    }

    public ObservableProperty<String> getKeywordFilter() {
        return keywordFilter;
    }
}
