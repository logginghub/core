package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

public class AggregationConfiguration extends Observable {

    private ObservableProperty<String> streamID = createStringProperty("streamID", null);
    private ObservableProperty<Boolean> rawData = createBooleanProperty("rawData", false);
    private ObservableProperty<String> interval = createStringProperty("interval", "1 second");
    private ObservableProperty<AggregationType> type = createProperty("type", AggregationType.class, AggregationType.Mean);

    public ObservableProperty<String> getStreamID() {
        return streamID;
    }

    public ObservableProperty<Boolean> getRawData() {
        return rawData;
    }

    public ObservableProperty<String> getInterval() {
        return interval;
    }

    public ObservableProperty<AggregationType> getType() {
        return type;
    }

}
