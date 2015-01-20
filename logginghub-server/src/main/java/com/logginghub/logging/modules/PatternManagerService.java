package com.logginghub.logging.modules;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.Result;

public interface PatternManagerService {

    Result<Pattern> getPatternByID(int patternID);
    Result<ObservableList<Pattern>> getPatterns();
    Result<Pattern> createPattern(String name, String pattern);

    // TODO : should these be in their own module? Or should we generalise this into a persitance module?
    
    Result<ObservableList<Aggregation>> getAggregations();
    Result<Aggregation> createAggregation(int pattern, int label, long interval, AggregationType type, String groupBy);
    
}
