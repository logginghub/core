package com.logginghub.logging.frontend.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

public class CustomQuickFilterModel extends Observable {

    private ObservableProperty<String> label = createStringProperty("label", "");
    private ObservableProperty<String> type = createStringProperty("type", "");
    private ObservableProperty<String> value =  createStringProperty("value","");
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

    public ObservableProperty<String> getValue() {
        return value;
    }

    public ObservableProperty<String> getLabel() {
        return label;
    }
}

