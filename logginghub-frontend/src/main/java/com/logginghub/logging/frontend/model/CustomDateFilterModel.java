package com.logginghub.logging.frontend.model;

import com.logginghub.logging.filters.TimeFieldFilter;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class CustomDateFilterModel extends Observable {

    private ObservableProperty<String> label = createStringProperty("label", "");
    private ObservableProperty<String> type = createStringProperty("type", "");
    private ObservableLong value =  createLongProperty("value", TimeFieldFilter.ACCEPT_ALL);
    private ObservableProperty<String> field =  createStringProperty("field","");
    private ObservableInteger width =  createIntProperty("width", 100);

    public ObservableInteger getWidth() {
        return width;
    }

    public ObservableProperty<String> getField() {
        return field;
    }

    public ObservableProperty<String> getType() {
        return type;
    }

    public ObservableLong getValue() {
        return value;
    }

    public ObservableProperty<String> getLabel() {
        return label;
    }
}

