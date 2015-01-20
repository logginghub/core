package com.logginghub.logging.api.patterns;

import java.util.List;

import com.logginghub.utils.Result;
import com.logginghub.utils.ResultListener;

public interface PatternManagementAPI {
    
    void getPatterns(ResultListener<List<Pattern>> listener);    
    Result<List<Pattern>> getPatterns();
    
    Result<Pattern> createPattern(Pattern template);
    void createPattern(Pattern template, ResultListener<Pattern> listener);
    
    void getAggregations(ResultListener<List<Aggregation>> listener);
    Result<List<Aggregation>> getAggregations();
    
    Result<Aggregation> createAggregation(Aggregation template);
    void createAggregation(Aggregation template, ResultListener<Aggregation> listener);
    
}
