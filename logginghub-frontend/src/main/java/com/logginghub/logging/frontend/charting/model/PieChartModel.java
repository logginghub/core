package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

public class PieChartModel extends AbstractChartModel {

    
    private ObservableProperty<String> type = createStringProperty("type", "");
    private ObservableProperty<String> xAxisLabel = createStringProperty("xAxisLabel", "Time");
    private ObservableProperty<String> yAxisLabel = createStringProperty("yAxisLabel", "y axis label");

    private ObservableDouble onlyShowValuesAbove = createDoubleProperty("onlyShowValuesAbove", Double.NaN);

    private ObservableDouble warningThreshold = createDoubleProperty("warningThreshold", Double.NaN);
    private ObservableDouble severeThreshold = createDoubleProperty("severeThreshold", Double.NaN);

    private ObservableDouble yAxisLock = createDoubleProperty("yAxisLock", Double.NaN);

    private ObservableInteger dataPoints = createIntProperty("dataPoints", 5 * 60);

    private ObservableProperty<Boolean> forceYZero = createBooleanProperty("forceYZero", true);
    private ObservableProperty<Boolean> showLegend = createBooleanProperty("showLegend", true);
    private ObservableProperty<Boolean> sideLegend = createBooleanProperty("sideLegend", false);
    
    private ObservableInteger top = createIntProperty("top", Integer.MAX_VALUE);
    private ObservableProperty<Boolean> showOtherSeries = createBooleanProperty("showOtherSeries", false);

    private PageModel parentPage;
    
    public void setParentPage(PageModel parentPage) {
        this.parentPage = parentPage;
    }
    
    public PageModel getParentPage() {
        return parentPage;
    }
    
    public ObservableProperty<Boolean> getShowOtherSeries() {
        return showOtherSeries;
    }
    
    public ObservableInteger getDataPoints() {
        return dataPoints;
    }

    public ObservableProperty<Boolean> getForceYZero() {
        return forceYZero;
    }

    public ObservableDouble getOnlyShowValuesAbove() {
        return onlyShowValuesAbove;
    }

    public ObservableDouble getSevereThreshold() {
        return severeThreshold;
    }

    public ObservableProperty<Boolean> getShowLegend() {
        return showLegend;
    }

    public ObservableProperty<Boolean> getSideLegend() {
        return sideLegend;
    }

    public ObservableProperty<String> getType() {
        return type;
    }

    public ObservableDouble getWarningThreshold() {
        return warningThreshold;
    }

    public ObservableProperty<String> getxAxisLabel() {
        return xAxisLabel;
    }

    public ObservableProperty<String> getyAxisLabel() {
        return yAxisLabel;
    }

    public ObservableDouble getyAxisLock() {
        return yAxisLock;
    }

    public ObservableInteger getTop() {
        return top;
    }
}
