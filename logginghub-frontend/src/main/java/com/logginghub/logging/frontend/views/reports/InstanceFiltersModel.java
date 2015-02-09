package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 04/02/15.
 */
public class InstanceFiltersModel extends Observable {

    private ObservableProperty<Boolean> andMatch = createBooleanProperty("andMatch", true);
    private ObservableList<InstanceFilterModel> filters = createListProperty("filters", InstanceFilterModel.class);

    public ObservableList<InstanceFilterModel> getFilters() {
        return filters;
    }

    public ObservableProperty<Boolean> getAndMatch() {
        return andMatch;
    }

    public boolean passesFilter(InstanceKey key) {

        boolean andMatch = getAndMatch().get();
        boolean passes = andMatch;

        for (InstanceFilterModel filterPanel : filters) {
            if (andMatch) {
                passes &= filterPanel.passes(key);
            }else{
                passes |= filterPanel.passes(key);
            }
        }

        return passes;
    }
}
