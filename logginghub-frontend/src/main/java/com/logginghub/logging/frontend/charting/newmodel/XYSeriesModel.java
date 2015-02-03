package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 31/01/15.
 */
public class XYSeriesModel extends Observable {

    private ObservableProperty<String> label = createStringProperty("label", "Label");

    private ObservableList<XYValue> values = createListProperty("values", XYValue.class);

    public ObservableProperty<String> getLabel() {
        return label;
    }

    public ObservableList<XYValue> getValues() {
        return values;
    }
}
