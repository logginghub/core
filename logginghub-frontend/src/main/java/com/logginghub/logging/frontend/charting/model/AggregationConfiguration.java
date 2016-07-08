package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class AggregationConfiguration extends Observable implements DataSourceInterface {

    private ObservableInteger aggregationId = createIntProperty("aggregationID", -1);
    private ObservableProperty<String> streamID = createStringProperty("streamID", null);
    private ObservableProperty<String> name = createStringProperty("name", null);
    private ObservableProperty<Boolean> rawData = createBooleanProperty("rawData", false);
    private ObservableLong interval = createLongProperty("interval", 1000);
    private ObservableProperty<String> type = createStringProperty("type", AggregationType.Mean.name());
    private ObservableInteger patternID  = createIntProperty("patternID", -1);
    private ObservableProperty<Boolean> generateEmptyTicks = createBooleanProperty("generateEmptyTicks", false);
    private ObservableInteger labelIndex = createIntProperty("labelIndex", -1);
    private ObservableProperty<String> groupBy = createStringProperty("groupBy", null);
    private ObservableProperty<String> eventParts = createStringProperty("eventParts", null);

    private ObservableList<ChartSeriesFilterModel> filters = createListProperty("filters", ChartSeriesFilterModel.class);

    @Override
    public ObservableProperty<String> getEventParts() {
        return eventParts;
    }

    public ObservableList<ChartSeriesFilterModel> getFilters() {
        return filters;
    }

    @Override
    public ObservableProperty<Boolean> getGenerateEmptyTicks() {
        return generateEmptyTicks;
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

    public ObservableLong getInterval() {
        return interval;
    }

    public ObservableProperty<String> getType() {
        return type;
    }

}
