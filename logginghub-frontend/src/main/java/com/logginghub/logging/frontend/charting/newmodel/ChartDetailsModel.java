package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 31/01/15.
 */
public class ChartDetailsModel extends Observable {

    private ObservableProperty<String> title = createStringProperty("title", "Chart title");
    private ObservableProperty<String> subtitle = createStringProperty("subtitle", "Chart title");

    private ObservableLong startTime = createLongProperty("startTime", System.currentTimeMillis());
    private ObservableLong endTime = createLongProperty("endTime", System.currentTimeMillis()+1);
    private ObservableLong duration = createLongProperty("duration", TimeUtils.parseInterval("1 minute"));

    public ObservableProperty<String> getTitle() {
        return title;
    }

    public ObservableProperty<String> getSubtitle() {
        return subtitle;
    }

    public ObservableLong getEndTime() {
        return endTime;
    }

    public ObservableLong getStartTime() {
        return startTime;
    }

    public ObservableLong getDuration() {
        return duration;
    }
}
