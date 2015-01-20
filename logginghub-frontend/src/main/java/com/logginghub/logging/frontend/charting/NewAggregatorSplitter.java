package com.logginghub.logging.frontend.charting;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.analysis.ChunkedResultHandler;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.StreamResultItem;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.logging.Logger;

public class NewAggregatorSplitter implements StreamListener<StreamResultItem> {

    private static final Logger logger = Logger.getLoggerFor(NewAggregatorSplitter.class);

    private Map<String, NewAggregator> chunkersPerSource = new HashMap<String, NewAggregator>();
    private List<ChunkedResultHandler> resultHandlers;
    private boolean overrideEventTime;
    private long chunkInterval = 1000;
    private Stream<ChunkedResult> outputStream = new Stream<ChunkedResult>();
    private boolean generateEmptyTicks = false;
    
    private AggregationType publishingMode = AggregationType.Mean;

    public NewAggregatorSplitter() {

    }

    public Stream<ChunkedResult> getOutputStream() {
        return outputStream;
    }

    public void setChunkInterval(long chunkInterval) {
        this.chunkInterval = chunkInterval;
        synchronized (chunkersPerSource) {
            Collection<NewAggregator> values = chunkersPerSource.values();
            for (NewAggregator timeChunker : values) {
                timeChunker.setChunkInterval(chunkInterval);
            }
        }
    }

    public void setPublishingModes(AggregationType mode) {
        this.publishingMode = mode;
        synchronized (chunkersPerSource) {
            Collection<NewAggregator> values = chunkersPerSource.values();
            for (NewAggregator timeChunker : values) {
                timeChunker.setPublishingModes(mode);
            }
        }
    }

    /**
     * Simluates the end of all active time chunks, used only for tests at the
     * moment
     */
    public void flush() {
        synchronized (chunkersPerSource) {
            Collection<NewAggregator> values = chunkersPerSource.values();
            for (NewAggregator timeChunker : values) {
                timeChunker.flush();
            }
        }
    }
    
    public void tick() {
        synchronized (chunkersPerSource) {
            Collection<NewAggregator> values = chunkersPerSource.values();
            for (NewAggregator timeChunker : values) {
                timeChunker.tick();
            }
        }
    }

    private long getEventTime(LogEvent entry) {
        if (overrideEventTime) {
            return System.currentTimeMillis();
        } else {
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
        synchronized (chunkersPerSource) {
            Collection<NewAggregator> values = chunkersPerSource.values();
            for (NewAggregator timeChunker : values) {
                timeChunker.clear();
            }
        }
    }

    @Override
    public void onNewItem(StreamResultItem t) {

        logger.finer("New result received : '{}'", t);

        NewAggregator timeChunker;
        synchronized (chunkersPerSource) {
            timeChunker = chunkersPerSource.get(t.getPath());
            if (timeChunker == null) {
                logger.finer("Building new time chunker for label '{}'", t.getPath());
                timeChunker = new NewAggregator();
                timeChunker.setOutputStream(outputStream);
                timeChunker.setChunkInterval(chunkInterval);
                timeChunker.setPublishingModes(publishingMode);
                timeChunker.setSource(t.getPath());
                chunkersPerSource.put(t.getPath(), timeChunker);
            }
        }
        
        long eventTime = t.getTime();
        logger.finer("Updating time chunker with new event - event time (may have been overridden) was '{}' ({})",
                Logger.toDateString(eventTime), eventTime);

        double result;
        if (t.isNumeric()) {
            result = Double.parseDouble(t.getResult());
        } else {
            result = 1;
        }

        timeChunker.update(eventTime, result, t.getPath());

    }

    
    public boolean isGenerateEmptyTicks() {
        return generateEmptyTicks;
    }
    
    public void setGenerateEmptyTicks(boolean generateEmptyTicks) {
        this.generateEmptyTicks = generateEmptyTicks;
    }
}
