package com.logginghub.logging.frontend.views.stack;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 04/02/15.
 */
public class StackInstanceFilterModel extends Observable {

    private ObservableProperty<String> threadFilter = createStringProperty("threadFilter", "*");
    private ObservableProperty<Boolean> threadFilterIsRegex = createBooleanProperty("threadFilterIsRegex", false);

    private ObservableProperty<String> instanceFilter = createStringProperty("instanceFilter", "*.*.*.*");
    private ObservableProperty<Boolean> instanceFilterIsRegex = createBooleanProperty("instanceFilterIsRegex", false);

    private ObservableProperty<String> stackFilter = createStringProperty("stackFilter", "*");
    private ObservableProperty<Boolean> stackFilterIsRegex = createBooleanProperty("stackFilterIsRegex", false);


    public ObservableProperty<Boolean> getInstanceFilterIsRegex() {
        return instanceFilterIsRegex;
    }

    public ObservableProperty<Boolean> getStackFilterIsRegex() {
        return stackFilterIsRegex;
    }

    public ObservableProperty<Boolean> getThreadFilterIsRegex() {
        return threadFilterIsRegex;
    }

    public ObservableProperty<String> getInstanceFilter() {
        return instanceFilter;
    }

    public ObservableProperty<String> getStackFilter() {
        return stackFilter;
    }

    public ObservableProperty<String> getThreadFilter() {
        return threadFilter;
    }
}
