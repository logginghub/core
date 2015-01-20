package com.logginghub.logging.frontend.charting.historical;

import java.util.List;

public interface HistoricalDataProviderService {
    List<TimeSeriesDataPoint> getDataPoints(String series, long start, long end);    
}
