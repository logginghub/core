package com.logginghub.logging.api.patterns;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.StreamingDestination;

public interface HistoricalDataAPI {
    void streamHistoricalPatternisedEvents(long fromTime, long toTime, StreamingDestination<PatternisedLogEvent> destination);
    void streamHistoricalAggregatedEvents(long fromTime, long toTime, StreamingDestination<AggregatedLogEvent> destination);
}
