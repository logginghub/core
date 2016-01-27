package com.logginghub.logging.frontend.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

public class FilterBookmarkValueModel extends Observable {
    private ObservableProperty<String> label = createStringProperty("label", "");
    private ObservableProperty<String> value = createStringProperty("value", "");

    public ObservableProperty<String> getLabel() {
        return label;
    }

    public ObservableProperty<String> getValue() {
        return value;
    }
}
