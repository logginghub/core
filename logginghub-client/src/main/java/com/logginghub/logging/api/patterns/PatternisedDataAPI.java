package com.logginghub.logging.api.patterns;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.AggregatedLogEventListener;
import com.logginghub.logging.messaging.StreamProgress;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.sof.SerialisableObject;

public interface PatternisedDataAPI {
    public interface SerialisableAggreatedEventFilter extends Filter<AggregatedLogEvent>, SerialisableObject {}    
    StreamProgress requestPatternisedData(int patternID, long from, long to, SerialisableAggreatedEventFilter filter, AggregatedLogEventListener listener);    
}
