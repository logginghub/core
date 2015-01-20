package com.logginghub.analytics.model;

public interface TimeSeriesDataTransformer {
    TimeSeriesDataPoint transform(TimeSeriesDataPoint original);
}
