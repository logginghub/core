package com.logginghub.logging.frontend.charting;

import java.util.EnumSet;

import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.StreamResultItem;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.Statistics;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

public class NewAggregator implements StreamListener<StreamResultItem> {

    private static final Logger logger = Logger.getLoggerFor(NewAggregator.class);

    private long chunkInterval = 1000;
    private long startOfCurrentChunk = -1;
    private boolean generateMissingChunks = false;
    // private Mode mode = Mode.Count;

    private Statistics statistics = new Statistics();

    private Stream<ChunkedResult> outputStream = new Stream<ChunkedResult>();

    // private double value;
    private double totalSum;
    private double totalCount;
    // private double count;

    private EnumSet<AggregationType> publishingModes = EnumSet.allOf(AggregationType.class);

    private String source;

    private long lastData;

    private double lastValue = Double.NaN;
    
    public NewAggregator() {
        
    }

    public void setOutputStream(Stream<ChunkedResult> outputStream) {
        this.outputStream = outputStream;
    }

    public Stream<ChunkedResult> getOutputStream() {
        return outputStream;
    }

    // public void setMode(Mode mode)
    // {
    // this.mode = mode;
    // }

    public void update(long time, double value, String source) {
        lastData = System.currentTimeMillis();

        logger.fine("Updating chunk source {} with value {} at time {}", source, value, Logger.toDateString(time));
        long chunkedTime = chunk(time);

        if (startOfCurrentChunk == -1) {
            startOfCurrentChunk = chunkedTime;
        }
        else {
            if (chunkedTime > startOfCurrentChunk) {
                flush();

                if (generateMissingChunks) {
                    generateMissingChunks(time, source);
                }

                startOfCurrentChunk = chunk(time);
            }
            else if (time < startOfCurrentChunk) {
                // TODO : this might be bad, but most of the data goes back and
                // forwards a bit...
                // logger.warn(String.format("Time [%s] is before current chunk start [%s], is your source sequence out of order?",
                // new Date(time).toLocaleString(),
                // new Date(startOfCurrentChunk).toLocaleString()));
            }
        }

        // Don't count NaNs as they represent chart gaps and should not be counted!
        if (!Double.isNaN(value)) {
            this.statistics.addValue(value);
            this.lastValue = value;
            this.totalSum += value;
            this.totalCount++;
        }
    }

    private void fireResult(ChunkedResult result) {
        outputStream.send(result);
    }

    private void generateMissingChunks(long time, String source) {
        long startOfNextChunk = chunk(time);
        long missingPeriods = ((startOfNextChunk - startOfCurrentChunk) / chunkInterval) - 1;
        for (int i = 0; i < missingPeriods; i++) {
            long chunkStart = startOfCurrentChunk + ((i + 1) * chunkInterval);
            ChunkedResult gapResult = new ChunkedResult(chunkStart, chunkInterval, 0, source);
            fireResult(gapResult);
        }
    }

    public long chunk(long time) {
        return chunk(time, chunkInterval);
    }

    public static long chunk(long time, long chunkInterval) {
        return TimeUtils.chunk(time, chunkInterval);
    }

    public void setChunkInterval(long chunkInterval) {
        this.chunkInterval = chunkInterval;
    }

    public void setGenerateMissingChunks(boolean generateMissingChunks) {
        this.generateMissingChunks = generateMissingChunks;
    }

    public double getValue(AggregationType mode) {
        switch (mode) {
            case Mean:
                return statistics.calculateMean();
            case Count:
                return statistics.getCount();
            case Sum:
                return statistics.calculateSum();
            case Median:
                return statistics.calculateMedian();
            case Mode:
                return statistics.calculateMode();
            case StandardDeviation:
                return statistics.calculateStandardDeviationFast();
            case Percentile90:
                return statistics.calculatePercentile(90);
            case TotalCount:
                return totalCount;
            case TotalSum:
                return totalSum;
            case LastValue:
                return lastValue;
            default:
                throw new RuntimeException(String.format("Unsupported mode : " + mode));
        }
    }

    public void reset() {
        statistics = new Statistics();
        
        // jshaw - we definitely dont want to reset the last value here!
        // lastValue = Double.NaN;

    }

    public void complete() {
        // TODO : maybe put other methods on the stream class?
        // for (ChunkedResultHandler chunkedResultHandler : resultHandlers) {
        // chunkedResultHandler.complete();
        // }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void tick() {
        if (System.currentTimeMillis() - lastData > 1010) {
            logger.fine("Generating empty tick for series");
            update(System.currentTimeMillis(), Double.NaN, "");
        }
    }

    public void flush() {
        for (AggregationType mode : publishingModes) {
            ChunkedResult result = new ChunkedResult(startOfCurrentChunk, chunkInterval, getValue(mode), source /*
                                                                                                                 * +
                                                                                                                 * "/"
                                                                                                                 * +
                                                                                                                 * mode
                                                                                                                 */);
            logger.fine("Flushing time chunk result {}", result);
            fireResult(result);
        }

        reset();
    }

    /**
     * Similar to reset but this one sets us back to our completely empty state
     */
    public void clear() {
        reset();
        startOfCurrentChunk = -1;
        totalCount = 0;
        totalSum = 0;
        lastValue = Double.NaN;
    }

    public void setPublishingModes(EnumSet<AggregationType> publishingModes) {
        this.publishingModes = publishingModes;
    }

    public void setPublishingModes(AggregationType... modes) {
        EnumSet<AggregationType> set = EnumSet.noneOf(AggregationType.class);
        for (AggregationType mode : modes) {
            set.add(mode);
        }
        setPublishingModes(set);
    }

    public void parseInterval(String newValue) {}

    @Override public void onNewItem(StreamResultItem t) {
        update(t.getTime(), Double.parseDouble(t.getResult()), t.getPath());
    }

}
