package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Encapsulates the information needed to extract appropriate data from the charting results stream
 * and group them into a chart series.
 * 
 * @author James
 * 
 */
public class ChartSeriesModel extends Observable {

    private ObservableProperty<AggregationConfiguration> existingAggregation = createProperty("existingAggregation", AggregationConfiguration.class, null);
    private ObservableInteger patternID = createIntProperty("patternID", -1);
    private ObservableInteger labelIndex = createIntProperty("labelIndex", -1);
    private ObservableProperty<String> groupBy = createStringProperty("groupBy", null);
    private ObservableProperty<String> type = createStringProperty("type", "Mean");
    private ObservableInteger interval = createIntProperty("interval", 1000);
    private ObservableProperty<String> eventParts = createStringProperty("eventParts", "");
    private ObservableProperty<Boolean> generateEmptyTicks = createBooleanProperty("generateEmptyTicks", false);

    private ObservableList<ChartSeriesFilterModel> filters = createListProperty("filters", ChartSeriesFilterModel.class);
    
    private LineChartModel parentChart;

    public ChartSeriesModel(int patternID) {
        getPatternID().set(patternID);
    }

    public ChartSeriesModel() {}

    public ObservableProperty<AggregationConfiguration> getExistingAggregation() {
        return existingAggregation;
    }

    public ObservableProperty<Boolean> getGenerateEmptyTicks() {
        return generateEmptyTicks;
    }
    
    public ObservableInteger getInterval() {
        return interval;
    }

    public ObservableList<ChartSeriesFilterModel> getFilters() {
        return filters;
    }
    
    public ObservableProperty<String> getType() {
        return type;
    }

    public ObservableInteger getLabelIndex() {
        return labelIndex;
    }

    public ObservableProperty<String> getGroupBy() {
        return groupBy;
    }

    public ObservableProperty<String> getEventParts() {
        return eventParts;
    }
    
    public ObservableInteger getPatternID() {
        return patternID;
    }
    
    @Override public String toString() {
        return "Pattern : " + patternID.asString();
    }

    public void setParentChart(LineChartModel lineChartModel) {
        this.parentChart = lineChartModel;
    }
    
    public LineChartModel getParentChart() {
        return parentChart;
    }


}
