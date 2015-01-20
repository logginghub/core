package com.logginghub.logging.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.AggregatorModule;
import com.logginghub.logging.modules.OpenAggregationInterval;
import com.logginghub.logging.modules.OpenIntervalKey;
import com.logginghub.logging.modules.PatternManagerService;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.Is;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Result;
import com.logginghub.utils.Source;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class AggregatorCore implements Destination<PatternisedLogEvent>, Source<AggregatedLogEvent>, Asynchronous {

    private static Logger logger = Logger.getLoggerFor(AggregatorModule.class);
    private List<Aggregation> aggregations = new CopyOnWriteArrayList<Aggregation>();
    private TimeProvider timeProvider = new SystemTimeProvider();
    private Multiplexer<AggregatedLogEvent> multiplexer = new Multiplexer<AggregatedLogEvent>();

    private Map<OpenIntervalKey, OpenAggregationInterval> openIntervals = new HashMap<OpenIntervalKey, OpenAggregationInterval>();
    private WorkerThread expiryTimer;
    private WorkerThread debugTimer;
    private boolean useEventTimes = false;
    private PatternManagerService patternManager;

    public AggregatorCore(PatternManagerService patternManager) {
        this.patternManager = patternManager;
    }

    public void addAggregation(Aggregation aggregation) {
        aggregations.add(aggregation);
    }

    public void removeAggregation(Aggregation aggregation) {
        aggregations.remove(aggregation);
    }

    @Override public void addDestination(Destination<AggregatedLogEvent> listener) {
        multiplexer.addDestination(listener);
    }

    @Override public void removeDestination(Destination<AggregatedLogEvent> listener) {
        multiplexer.removeDestination(listener);
    }

    @Override public void send(PatternisedLogEvent t) {
        logger.finer("Aggregating event '{}' against {} aggregations", t, aggregations.size());
        for (Aggregation aggregation : aggregations) {

            int patternID = aggregation.getPatternID();
            if (t.getPatternID() == patternID) {

                int labelIndex = aggregation.getCaptureLabelIndex();
                String value = t.getVariable(labelIndex);
                if (value == null) {
                    logger.info("No value found for label '{}' in pattern '{}' - is you configuration correct?", labelIndex, t.getPatternID());
                }
                else {
                    String seriesKey = buildSeriesKey(t, aggregation); // , patternID, labelIndex,
                                                                       // aggregation.getType(),
                                                                       // aggregation.getEventParts());

                    OpenIntervalKey key = new OpenIntervalKey();
                    key.setInterval(aggregation.getInterval());
                    key.setLabelIndex(aggregation.getCaptureLabelIndex());
                    key.setPatternID(aggregation.getPatternID());
                    key.setSeriesKey(seriesKey);
                    key.setType(aggregation.getType());

                    long time;
                    if (useEventTimes) {
                        time = t.getTime();
                    }
                    else {
                        time = timeProvider.getTime();
                    }
                    long intervalTime = TimeUtils.chunk(time, aggregation.getInterval());
                    long endOfIntervalTime = intervalTime + aggregation.getInterval();

                    OpenAggregationInterval toDispatch = null;
                    synchronized (openIntervals) {

                        OpenAggregationInterval openInterval = openIntervals.get(key);
                        if (openInterval == null) {
                            logger.finer("Opening new interval for key '{}'", key);
                            openInterval = new OpenAggregationInterval(seriesKey,
                                                                       aggregation,
                                                                       intervalTime,
                                                                       aggregation.getInterval(),
                                                                       aggregation.getType());

                            if (key.getType() == AggregationType.Count) {
                                openInterval.update(1);
                            }
                            else {
                                openInterval.update(Double.parseDouble(value));
                            }
                            openIntervals.put(key, openInterval);
                        }
                        else {
                            long endOfOpenInterval = openInterval.getIntervalStart() + openInterval.getIntervalLength();
                            logger.finest("Time is '{}' : Open interval start is '{}', with length '{}' so end is '{}",
                                          Logger.toDateString(time),
                                          Logger.toDateString(openInterval.getIntervalStart()),
                                          openInterval.getIntervalLength(),
                                          Logger.toDateString(endOfIntervalTime));
                            if (time < endOfOpenInterval) {
                                // Still in the same interval
                                logger.finest("Updating existing interval for key '{}'", key);
                                if (key.getType() == AggregationType.Count) {
                                    openInterval.update(1);
                                }
                                else {
                                    openInterval.update(Double.parseDouble(value));
                                }
                            }
                            else {

                                logger.finest("Dispatching existing and creating new interval key '{}'", key);

                                // Outside the current interval
                                toDispatch = openInterval;

                                // Build a new interval
                                openInterval = new OpenAggregationInterval(seriesKey,
                                                                           aggregation,
                                                                           intervalTime,
                                                                           aggregation.getInterval(),
                                                                           aggregation.getType());
                                if (key.getType() == AggregationType.Count) {
                                    openInterval.update(1);
                                }
                                else {
                                    openInterval.update(Double.parseDouble(value));
                                }
                                openIntervals.put(key, openInterval);
                            }
                        }
                    }

                    if (toDispatch != null) {
                        dispatch(toDispatch);
                    }

                }
            }
        }
    }

    private void dispatch(OpenAggregationInterval openInterval) {

        Aggregation aggregationKey = openInterval.getAggregation();

        // This is the newer version, relying more heavily on the aggregationID to provide a lookup
        // back to the original configuration data, which results in a much smaller message
        AggregatedLogEvent event = new AggregatedLogEvent();
        event.setAggregationID(aggregationKey.getAggregationID());
        event.setTime(openInterval.getIntervalStart());
        event.setValue(openInterval.getValue());

        if (StringUtils.isNotNullOrEmpty(openInterval.getAggregation().getGroupBy())) {
            event.setSeriesKey(openInterval.getSeriesKey());
        }
        else {
            Pattern pattern = patternManager.getPatternByID(aggregationKey.getPatternID()).getValue();
            // TODO : need a way to resolve the capture label index without parsing the bloody
            // pattern again!!
            // jshaw : I suggest another interface that the module can expose to its collaborators,
            // rather than putting it on the main pattern management interface
            event.setSeriesKey(StringUtils.format("{}/{}/{}", pattern.getName(), aggregationKey.getCaptureLabelIndex(), aggregationKey.getType()));
        }

        logger.fine("Dispatching aggregated log event '{}'", event);

        multiplexer.send(event);

    }

    @Override public void start() {
        stop();

        expiryTimer = WorkerThread.every("LoggingHub-Aggregator-IntervalExpiryTimer", 100, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                checkOpenIntervals();
            }
        });

    }

    protected void logDebugStatus() {
        synchronized (openIntervals) {
            Set<Entry<OpenIntervalKey, OpenAggregationInterval>> entrySet = openIntervals.entrySet();
            for (Entry<OpenIntervalKey, OpenAggregationInterval> entry : entrySet) {
                logger.info("Aggregator debug stats : {}={} ", entry.getValue().getAggregation().getAggregationID(), entry.getValue().getValue());
            }
        }
    }

    public void checkOpenIntervals() {
        synchronized (openIntervals) {

            long time = timeProvider.getTime();

            Set<Entry<OpenIntervalKey, OpenAggregationInterval>> entrySet = openIntervals.entrySet();
            Iterator<Entry<OpenIntervalKey, OpenAggregationInterval>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<OpenIntervalKey, OpenAggregationInterval> entry = (Map.Entry<OpenIntervalKey, OpenAggregationInterval>) iterator.next();

                OpenAggregationInterval value = entry.getValue();
                long intervalEnd = value.getIntervalStart() + value.getIntervalLength();

                if (time >= intervalEnd) {
                    logger.finer("Open interval '{}' has expired, dispatching", value);
                    dispatch(value);
                    iterator.remove();
                }
            }

        }
    }

    @Override public void stop() {
        if (expiryTimer != null) {
            expiryTimer.stop();
            expiryTimer = null;
        }

        if (debugTimer != null) {
            debugTimer.stop();
            debugTimer = null;
        }
    }

    private String buildSeriesKey(PatternisedLogEvent t, Aggregation aggregation) {

        String seriesKey;
        String groupBy = aggregation.getGroupBy();
        if (StringUtils.isNotNullOrEmpty(groupBy)) {
            AggregatedPatternParser parser = new AggregatedPatternParser();
            Result<Pattern> patternByID = patternManager.getPatternByID(aggregation.getPatternID());
            Pattern pattern = patternByID.getValue();
            Is.notNull(pattern,
                       "Pattern for pattern ID '{}' wasn't found, we can't recover from this, it looks like you've got an aggregation with a bad patternID?",
                       aggregation.getPatternID());

            parser.parse(groupBy, pattern.getPattern());
            seriesKey = parser.format(t);
        }
        else {
            seriesKey = Integer.toString(aggregation.getAggregationID());
        }

        return seriesKey;
    }

    public String buildSeriesKey(PatternisedLogEvent entry, int patternID, int labelIndex, AggregationType type, String[] eventParts) {

        String specificBit = "";

        if (eventParts != null) {
            StringBuilder streamBuilder = new StringBuilder();

            String div = "";
            for (String string : eventParts) {
                streamBuilder.append(div);
                if (string.contains("Source Application")) {
                    streamBuilder.append(entry.getSourceApplication());
                }
                else if (string.contains("Source Host")) {
                    // TODO : move to configuration
                    if (EnvironmentProperties.getBoolean("dontShrinkHostnames")) {
                        streamBuilder.append(entry.getSourceHost());
                    }
                    else {
                        streamBuilder.append(shortenHost(entry.getSourceHost()));

                    }
                }
                else if (string.contains("Source IP")) {
                    streamBuilder.append(entry.getSourceAddress());
                }
                else {
                    streamBuilder.append(string);
                }

                div = "/";
            }
            streamBuilder.append(div);

            specificBit = streamBuilder.toString();

        }

        String path = StringUtils.format("{}/{}/{}{}", patternID, labelIndex, specificBit, type);
        logger.trace("Output path is '{}'", path);
        return path;
    }

    public static String shortenHost(String key) {

        String shorter;
        int indexOf = key.indexOf(".");
        if (indexOf != -1) {
            shorter = key.substring(0, indexOf);
        }
        else {
            shorter = key;
        }

        return shorter;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setUseEventTimes(boolean useEventTimes) {
        this.useEventTimes = useEventTimes;
    }

    public boolean isUseEventTimes() {
        return useEventTimes;
    }

}
