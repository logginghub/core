package com.logginghub.logging.frontend.analysis;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;
import com.logginghub.utils.logging.Logger;

public class TimeChunkingGenerator implements ValueStripper2ResultListener, ResultGenerator {

    private static final Logger logger = Logger.getLoggerFor(TimeChunkingGenerator.class);

    private Map<String, TimeChunker> chunkersPerSource = new HashMap<String, TimeChunker>();
    private List<ChunkedResultHandler> resultHandlers;
    private boolean overrideEventTime;
    private AggregationType[] modes = EnumSet.allOf(AggregationType.class).toArray(new AggregationType[] {});
    private long chunkInterval;

    public TimeChunkingGenerator() {
        this(1000);
    }

    public TimeChunkingGenerator(long chunkInterval) {
        this.chunkInterval = chunkInterval;
    }

    public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        logger.debug("New result received : label '{}' (numeric {}) value '{}' for event '{}'", label, isNumeric, value, entry);

        if (isNumeric) {
            TimeChunker timeChunker = chunkersPerSource.get(label);
            if (timeChunker == null) {
                logger.trace("Building new time chunker for label '{}'", label);
                timeChunker = new TimeChunker(resultHandlers);
                timeChunker.setChunkInterval(chunkInterval);
                timeChunker.setPublishingModes(modes);
                timeChunker.setSource(label);
                chunkersPerSource.put(label, timeChunker);
            }

            long eventTime = getEventTime(entry);
            logger.trace("Updating time chunker with new event - event time (may have been overridden) was '{}' ({})", Logger.toDateString(eventTime), eventTime);
            timeChunker.update(eventTime, Double.parseDouble(value), label);
        }
        else {
            logger.trace("Result wasn't numeric, ignoring");
        }
    }
    
    /**
     * Simluates the end of all active time chunks, used only for tests at the moment
     */
    public void flush() {
        Collection<TimeChunker> values = chunkersPerSource.values();
        for (TimeChunker timeChunker : values) {
            timeChunker.flush();
        }
    }

    private long getEventTime(LogEvent entry) {
        if (overrideEventTime) {
            return System.currentTimeMillis();
        }
        else {
            return entry.getOriginTime();
        }
    }

    public void addChunkedResultHandler(ChunkedResultHandler chunkedResultHandler) {
        if (resultHandlers == null) {
            resultHandlers = new CopyOnWriteArrayList<ChunkedResultHandler>();
        }
        this.resultHandlers.add(chunkedResultHandler);
    }
    
    public void removeChunkedResultHandler(ChunkedResultHandler chunkedResultHandler) {
        if (resultHandlers != null) {
            resultHandlers.remove(chunkedResultHandler);
        }
    }

    public void clear() {
        Collection<TimeChunker> values = chunkersPerSource.values();
        for (TimeChunker timeChunker : values) {
            timeChunker.clear();
        }
    }

    public void setPublishingModes(AggregationType... modes) {
        this.modes = modes;
    }

    @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isBoolean, LogEvent entry) {
        // TODO : get rid of these crappy methods 
        logger.trace("Ignoring call to onNewPatternisedResult - everything needs to come through onNewResult now");
    }
}
