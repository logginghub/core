package com.logginghub.logging.repository;

import java.util.List;

import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.logging.repository.cache.AggregatedDataKey;
import com.logginghub.logging.repository.cache.TimeSeriesDataKey;

public interface DataHandlerInterface {

    void storeSeries(TimeSeriesDataKey key, TimeSeriesData data);
    TimeSeriesData retrieveSeries(TimeSeriesDataKey key);
    List<TimeSeriesDataKey> querySeries(String seriesName, long fromTime, long toTime);

    
    void storeAggregation(AggregatedDataKey key, AggregatedData data);
    AggregatedData retrieveAggregation(AggregatedDataKey key);
    List<AggregatedDataKey> queryAggregations(String seriesName, long fromTime, long toTime, long aggregationInterval);

}
