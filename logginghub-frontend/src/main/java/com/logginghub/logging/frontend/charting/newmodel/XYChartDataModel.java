package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

/**
 * Created by james on 31/01/15.
 */
public class XYChartDataModel extends Observable {

    private ObservableList<XYSeriesModel> series = createListProperty("series", XYSeriesModel.class);

    public ObservableList<XYSeriesModel> getSeries() {
        return series;
    }
}
