package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 08/07/2016.
 */
public interface DataSourceInterface {
    ObservableProperty<String> getEventParts();

    ObservableList<ChartSeriesFilterModel> getFilters();

    ObservableProperty<Boolean> getGenerateEmptyTicks();

    ObservableProperty<String> getGroupBy();

    ObservableInteger getLabelIndex();

    ObservableInteger getPatternID();
}
