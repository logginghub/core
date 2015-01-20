package com.logginghub.analytics.model;

public interface GeneralisedTimeSeriesDataPointListener {
    void onNewDataPoint(GeneralAggregatedDataPoint dataPoint);
}
