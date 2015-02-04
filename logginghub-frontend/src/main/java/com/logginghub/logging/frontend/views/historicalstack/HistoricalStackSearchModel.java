package com.logginghub.logging.frontend.views.historicalstack;

import com.logginghub.logging.frontend.charting.model.TimeSelectionModel;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.utils.Stream;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 29/01/15.
 */
public class HistoricalStackSearchModel extends Observable {

    private ObservableInteger levelFilter = createIntProperty("levelFilter", Logger.info);

    private ObservableProperty<String> keywordFilter = createStringProperty("keywordFilter", "");

    private ObservableProperty<Boolean> searchInProgress = createBooleanProperty("searchInProgress", false);
    private ObservableProperty<Boolean> searchKilled = createBooleanProperty("searchKilled", false);

    private final TimeSelectionModel timeSelectionModel = new TimeSelectionModel();

    private Stream<StackSnapshot> resultsStream = new Stream<StackSnapshot>();

    private LogEventContainerController events = new LogEventContainerController();

    public TimeSelectionModel getTimeSelectionModel() {
        return timeSelectionModel;
    }

    public LogEventContainerController getEvents() {
        return events;
    }

    public Stream<StackSnapshot> getResultsStream() {
        return resultsStream;
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

    public ObservableProperty<Boolean> getSearchKilled() {
        return searchKilled;
    }
}

