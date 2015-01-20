package com.logginghub.logging.modules;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.filter.Filter;

public class PatternisedEventMatchesFilter implements Filter<PatternisedLogEvent> {

    public PatternisedEventMatchesFilter(String value) {}

    @Override public boolean passes(PatternisedLogEvent t) {
        return false;

    }

}
