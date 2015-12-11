package com.logginghub.logging.modules.history;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.Destination;

import java.util.List;

public interface EventBuffer {
    void addEvent(DefaultLogEvent t);

    void clear();

    int sizeof(DefaultLogEvent t);

    int countEvents();
    int countBetween(long start, long end);
    long getWatermark();
    
    int size();
    
    void extractEventsBetween(List<LogEvent> matchingEvents, long start, long end);
    void extractIndexBetween(List<HistoricalIndexElement> index, long start, long end);
    void extractEventsBetween(Destination<LogEvent> visitor, long start, long end);
    
    int getBlockSequence();
    
    // TODO : add a remove listener
    void addIndexListener(Destination<HistoricalIndexElement> destination);
}
