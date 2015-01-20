package com.logginghub.logging.frontend.charting.historical;

import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableLong;

public class HistoricalChartModel extends Observable {

    private ObservableLong startTime = createLongProperty("startTime", 0);  
    private ObservableLong duration = createLongProperty("duration", TimeUtils.hours(1));
    
    public ObservableLong getStartTime() {
        return startTime;
    }
    
    public ObservableLong getDuration() {
        return duration;
    }

    public long getEndTime() {
        return startTime.longValue() + duration.longValue();
         
    }
    
    
    
}
