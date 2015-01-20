package com.logginghub.logging.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.logging.Logger;

public class BlockIndex {

    private static final Logger logger = Logger.getLoggerFor(BlockIndex.class);
    private List<LogEventBlockElement> elements = new ArrayList<LogEventBlockElement>();

    public void addBlock(LogEventBlockElement block) {
        logger.trace("Adding log event block '{}'", block);
        synchronized (elements) {
            elements.add(block);
        }

        // TODO : insert sort
        Collections.sort(elements, LogEventBlockElement.TimeComparator);
    }

    public void loadData(long start, long end, StreamListener<LogEvent> events) {

        LogEventBlockElement range = new LogEventBlockElement();
        range.setEarliestTime(start);
        range.setLatestTime(end);

        logger.info("Loading data for range '{}'", range);

        synchronized (elements) {
            for (LogEventBlockElement logEventBlockElement : elements) {
                logger.trace("Checking block '{}'", logEventBlockElement);
                if (logEventBlockElement.overlaps(range)) {
                    logger.info("  Block '{}' matches, streaming data...", logEventBlockElement);                    
                    logEventBlockElement.getDataProvider().provideData(start, end, events);
                }

                if (logEventBlockElement.getEarliestTime() > range.getLatestTime()) {
                    // This block starts after the range finishes, so we are done searching if the
                    // list is ordered
                    break;
                }
            }
        }
    }

}
