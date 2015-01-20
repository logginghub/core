package com.logginghub.logging.messaging;

import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.sof.SerialisableObject;

public interface AggregatedDataAPI {
    public interface SerialisableAggregatedEventFilter extends Filter<PatternisedLogEvent>, SerialisableObject {}    
    StreamProgress requestPatternisedData(int patternID, long from, long to, SerialisableAggregatedEventFilter filter, PatternisedLogEventListener listener);    
}
