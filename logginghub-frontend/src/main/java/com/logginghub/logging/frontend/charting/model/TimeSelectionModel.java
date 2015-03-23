package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;

/**
 * Created by james on 29/01/15.
 */
public class TimeSelectionModel extends Observable {

    private TimeModel startTime = new TimeModel();
    private TimeModel endTime = new TimeModel();

    public TimeModel getStartTime() {
        return startTime;
    }

    public TimeModel getEndTime() {
        return endTime;
    }
}
