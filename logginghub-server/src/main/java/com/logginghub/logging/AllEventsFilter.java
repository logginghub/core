package com.logginghub.logging;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;


public class AllEventsFilter implements Filter<LogEvent>{
    @Override public boolean passes(LogEvent event) {
        return true;
    }
}
