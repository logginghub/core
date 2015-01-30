package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 29/01/15.
 */

public class TimeModel extends Observable {

    private ObservableLong time = createLongProperty("time", 0);
    private ObservableProperty<Boolean> edited = createBooleanProperty("edited", false);

    public ObservableLong getTime() {
        return time;
    }

    public ObservableProperty<Boolean> getEdited() {
        return edited;
    }
}
