package com.logginghub.logging.frontend.views.stack;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 04/02/15.
 */
public class StackInstanceFiltersModel extends Observable {

    private ObservableProperty<Boolean> andMatch = createBooleanProperty("andMatch", true);
    private ObservableList<StackInstanceFilterModel> filters = createListProperty("filters", StackInstanceFilterModel.class);

    public ObservableList<StackInstanceFilterModel> getFilters() {
        return filters;
    }

    public ObservableProperty<Boolean> getAndMatch() {
        return andMatch;
    }
}
