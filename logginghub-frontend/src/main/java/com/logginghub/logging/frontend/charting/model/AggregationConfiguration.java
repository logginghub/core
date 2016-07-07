package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class AggregationConfiguration extends Observable {

    private ObservableInteger aggregationId = createIntProperty("aggregationID", -1);
    private ObservableProperty<String> streamID = createStringProperty("streamID", null);
    private ObservableProperty<String> name = createStringProperty("name", null);
    private ObservableProperty<Boolean> rawData = createBooleanProperty("rawData", false);
    private ObservableProperty<String> interval = createStringProperty("interval", "1 second");
    private ObservableProperty<AggregationType> type = createProperty("type", AggregationType.class, AggregationType.Mean);
    private ObservableInteger patternID  = createIntProperty("patternID", -1);
    private ObservableInteger labelIndex = createIntProperty("labelIndex", -1);
    private ObservableProperty<String> groupBy = createStringProperty("groupBy", null);

    private ObservableList<ChartSeriesFilterModel> filters = createListProperty("filters", ChartSeriesFilterModel.class);

    public ObservableList<ChartSeriesFilterModel> getFilters() {
        return filters;
    }

    public ObservableProperty<String> getGroupBy() {

        return groupBy;
    }

    public ObservableInteger getLabelIndex() {
        return labelIndex;
    }

    public ObservableProperty<String> getName() {
        return name;
    }

    public ObservableInteger getAggregationId() {
        return aggregationId;
    }

    public ObservableInteger getPatternID() {
        return patternID;
    }

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
