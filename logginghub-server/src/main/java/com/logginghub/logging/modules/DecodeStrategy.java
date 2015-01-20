package com.logginghub.logging.modules;

import java.io.File;
import java.io.IOException;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.StreamListener;

public interface DecodeStrategy {
    String getStrategyName();
    
    boolean canParse(File input);

    void decode(File file, StreamListener<LogEventBlockElement> blockListener, StreamListener<LogEvent> eventListener) throws IOException;
}
