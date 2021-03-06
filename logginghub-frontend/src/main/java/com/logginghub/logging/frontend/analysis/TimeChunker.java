package com.logginghub.logging.frontend.analysis;

import com.logginghub.logging.frontend.charting.NewAggregator;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.Statistics;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * @author James
 * @deprecated We should be using the {@link NewAggregator} in preference to this class now.
 */
public class TimeChunker {

    private static final Logger logger = Logger.getLoggerFor(TimeChunker.class);
    private long chunkInterval = 1000;
    private long startOfCurrentChunk = -1;
    private boolean generateMissingChunks = false;
    // private Mode mode = Mode.Count;

    private Statistics statistics = new Statistics();

    // private double value;
    private double totalSum;
    private double totalCount;
    // private double count;

    private double lastValue = Double.NaN;

    private EnumSet<AggregationType> publishingModes = EnumSet.allOf(AggregationType.class);

    private List<ChunkedResultHandler> resultHandlers;
    private String source;
    private String label;
    private String groupBy;

    // public enum Mode {
    // Count,
    // Sum,
    // TotalSum,
    // TotalCount,
    // Mean,
    // Median,
    // Mode,
    // StandardDeviation,
    // Percentile90,
    // }

    public TimeChunker(ChunkedResultHandler resultHandler) {
        List<ChunkedResultHandler> handlers = new ArrayList<ChunkedResultHandler>();
        handlers.add(resultHandler);
        this.resultHandlers = handlers;
    }

    public TimeChunker(List<ChunkedResultHandler> resultHandlers) {
        this.resultHandlers = resultHandlers;
    }

    // public void setMode(Mode mode)
    // {
    // this.mode = mode;
    // }

    /**
     * Similar to reset but this one sets us back to our completely empty state
     */
    public void clear() {
        reset();
        startOfCurrentChunk = -1;
        totalCount = 0;
        totalSum = 0;
    }

    public void complete() {
        for (ChunkedResultHandler chunkedResultHandler : resultHandlers) {
            chunkedResultHandler.complete();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setChunkInterval(long chunkInterval) {
        this.chunkInterval = chunkInterval;
    }

    public void setGenerateMissingChunks(boolean generateMissingChunks) {
        this.generateMissingChunks = generateMissingChunks;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public void setPublishingModes(AggregationType... modes) {
        EnumSet<AggregationType> set = EnumSet.noneOf(AggregationType.class);
        for (AggregationType mode : modes) {
            set.add(mode);
        }
        setPublishingModes(set);
    }

    public void setPublishingModes(EnumSet<AggregationType> publishingModes) {
        this.publishingModes = publishingModes;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void update(long time, double value, String source, String label, String groupBy) {
        logger.fine("Updating chunk source {} with value {} at time {}", source, value, Logger.toDateString(time));
        long chunkedTime = chunk(time);

        if (startOfCurrentChunk == -1) {
            startOfCurrentChunk = chunkedTime;
        } else {
            if (chunkedTime > startOfCurrentChunk) {
                flush();

                if (generateMissingChunks) {
                    generateMissingChunks(time, source, label, groupBy);
                }

                startOfCurrentChunk = chunk(time);
            } else if (time < startOfCurrentChunk) {
                throw new RuntimeException(String.format("Time [%s] is before current chunk start [%s], is your source sequence out of order?",
                                                         new Date(time).toLocaleString(),
                                                         new Date(startOfCurrentChunk).toLocaleString()));
            }
        }

        this.statistics.addValue(value);
        this.totalSum += value;
        this.totalCount++;
        this.lastValue = value;
    }

    public long chunk(long time) {
        return chunk(time, chunkInterval);
    }

    public void flush() {
        for (AggregationType mode : publishingModes) {
            ChunkedResult result = new ChunkedResult(startOfCurrentChunk,
                                                     chunkInterval,
                                                     getValue(mode),
                                                     mode.toString(),
                                                     label,
                                                     groupBy,
                                                     source + "/" + mode);
            logger.fine("Flushing time chunk result {}", result);
            fireResult(result);
        }

        reset();
    }

    private void generateMissingChunks(long time, String source, String label, String groupBy) {
        long startOfNextChunk = chunk(time);
        long missingPeriods = ((startOfNextChunk - startOfCurrentChunk) / chunkInterval) - 1;
        for (int i = 0; i < missingPeriods; i++) {
            long chunkStart = startOfCurrentChunk + ((i + 1) * chunkInterval);
            ChunkedResult gapResult = new ChunkedResult(chunkStart, chunkInterval, 0, "gap", source, label, groupBy);
            fireResult(gapResult);
        }
    }

    public static long chunk(long time, long chunkInterval) {
        return TimeUtils.chunk(time, chunkInterval);
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

    private void fireResult(ChunkedResult result) {
        for (ChunkedResultHandler chunkedResultHandler : resultHandlers) {
            chunkedResultHandler.onNewChunkedResult(result);
        }
    }

    public void reset() {
        statistics = new Statistics();
        lastValue = Double.NaN;
    }
}
