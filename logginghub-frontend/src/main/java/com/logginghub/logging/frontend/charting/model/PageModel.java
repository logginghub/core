package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class PageModel extends Observable {

    private ObservableProperty<String> name = createStringProperty("name", "");

    private ObservableList<LineChartModel> chartingModels = createListProperty("chartingModels", LineChartModel.class);
    private ObservableList<PieChartModel> pieChartModels = createListProperty("pieChartModels", PieChartModel.class);

    // private ObservableList<BarChartModel> barChartingModels
    // =createListProperty("barChartingModels", BarChartModel.class);

    public ObservableProperty<String> getName() {
        return name;
    }

    public ObservableList<LineChartModel> getChartingModels() {
        return chartingModels;
    }
    
    public ObservableList<PieChartModel> getPieChartModels() {
        return pieChartModels;
    }

    // public ObservableList<BarChartModel> getBarChartingModels() {
    // return barChartingModels;
    // }
}
