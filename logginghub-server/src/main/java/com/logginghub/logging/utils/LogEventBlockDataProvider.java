package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StreamListener;

public interface LogEventBlockDataProvider {
    void provideData(long start, long end, StreamListener<LogEvent> destination);    
}
