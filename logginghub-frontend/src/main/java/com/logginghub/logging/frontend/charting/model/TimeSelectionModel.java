package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;
import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Created by james on 29/01/15.
 */
public class TimeSelectionModel extends Observable {

    private TimeModel startTime = new TimeModel();
    private TimeModel endTime = new TimeModel();
//    private ObservableLong endTime = createLongProperty("endTimeTime", 0);
//    private ObservableProperty<Boolean> dynamicEndTime = createBooleanProperty("dynamicEndTime", true);

//    public ObservableProperty<Boolean> getDynamicEndTime() {
//        return dynamicEndTime;
//    }
//
//    public ObservableLong getStartTime() {
//        return startTime;
//    }
//
//    public ObservableLong getEndTime() {
//        return endTime;
//    }


    public TimeModel getStartTime() {
        return startTime;
    }

    public TimeModel getEndTime() {
        return endTime;
    }
}
