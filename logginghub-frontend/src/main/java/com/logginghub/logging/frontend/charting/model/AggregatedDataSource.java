package com.logginghub.logging.frontend.charting.model;

/**
 * Abstraction that allows both {@link ExpressionConfiguration} and {@link AggregationConfiguration} models to be used as sources for a {@link ChartSeriesModel }
 */
public interface AggregatedDataSource {

    String getName();

}
