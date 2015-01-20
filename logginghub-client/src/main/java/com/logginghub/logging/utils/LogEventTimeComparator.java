package com.logginghub.logging.utils;

import java.util.Comparator;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.CompareUtils;

public class LogEventTimeComparator implements Comparator<LogEvent> {

    public int compare(LogEvent o1, LogEvent o2) {
        return CompareUtils.compareLongs(o1.getOriginTime(), o2.getOriginTime());
    }

}
