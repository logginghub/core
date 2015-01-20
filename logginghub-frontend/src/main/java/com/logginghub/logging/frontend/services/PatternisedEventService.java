package com.logginghub.logging.frontend.services;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Destination;

public interface PatternisedEventService {

    void addPatternisedEventListener(Destination<PatternisedLogEvent> destination);
    void removePatternisedEventListener(Destination<PatternisedLogEvent> destination);
    
}
