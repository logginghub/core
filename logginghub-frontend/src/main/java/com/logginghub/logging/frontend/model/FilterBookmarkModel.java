package com.logginghub.logging.frontend.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class FilterBookmarkModel extends Observable {

    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableList<FilterBookmarkValueModel> values = createListProperty("values", FilterBookmarkValueModel.class);

    public ObservableList<FilterBookmarkValueModel> getValues() {
        return values;
    }

    public ObservableProperty<String> getName() {
        return name;
    }
}
