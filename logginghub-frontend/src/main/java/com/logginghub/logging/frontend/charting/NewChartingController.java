package com.logginghub.logging.frontend.charting;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.charting.model.AbstractChartModel;
import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.frontend.charting.model.ChartSeriesFilterModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.charting.model.PageModel;
import com.logginghub.logging.frontend.charting.model.PieChartModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamBuilder;
import com.logginghub.logging.frontend.charting.model.StreamConfiguration;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.StreamResultItem;
import com.logginghub.logging.frontend.charting.swing.Counterparts;
import com.logginghub.logging.frontend.model.ConnectionStateChangedEvent;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.messaging.SocketClientManager.State;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class NewChartingController {

    private TimeProvider timeProvider;

    private static final Logger logger = Logger.getLoggerFor(NewChartingController.class);

    private Map<Object, Object> counterparts = new HashMap<Object, Object>();

    private Map<AggregationConfiguration, NewAggregatorSplitter> aggregatorsByAggregationConfiguration = new HashMap<AggregationConfiguration, NewAggregatorSplitter>();
    private Map<PatternModel, ValueStripper2> valueStrippersByPattern = new HashMap<PatternModel, ValueStripper2>();
    private Map<StreamConfiguration, StreamBuilder> streamBuildersByDefintion = new HashMap<StreamConfiguration, StreamBuilder>();

    private Counterparts<ChartSeriesModel, StreamBuilder> streamBuilderCounterparts = new Counterparts<ChartSeriesModel, StreamBuilder>();
    private Counterparts<ChartSeriesModel, NewAggregatorSplitter> splitterCounterparts = new Counterparts<ChartSeriesModel, NewAggregatorSplitter>();

    private LogEventMultiplexer logEventMultiplexer = new LogEventMultiplexer();
    private NewChartingModel model;

    private Map<ChartSeriesModel, Stream<ChunkedResult>> resultStreams = new HashMap<ChartSeriesModel, Stream<ChunkedResult>>();

    private static AtomicInteger nextInstance = new AtomicInteger(0);
    private int instanceNumber = nextInstance.getAndIncrement();

    private AtomicInteger nextLocalPatternID = new AtomicInteger();

    private WorkerThread tickThread;

    private PatternManagementService patternService;

    public NewChartingController(final NewChartingModel model, TimeProvider timeProvider) {
        this.model = model;
        this.timeProvider = timeProvider;
        patternService = model.getPatternService();

        bindValueStrippers(model);
        // bindStreamBuilders(model);
        bindAggregators(model);
        bindCharts(model);

        // Work out the next patternID
        int maxPatternID = -1;
        ObservableList<PatternModel> patternModels = model.getPatternModels();
        for (PatternModel patternModel : patternModels) {
            maxPatternID = Math.max(maxPatternID, patternModel.getPatternID().get());
        }
        nextLocalPatternID.set(maxPatternID + 1);

        logger.info("New Charting Controller (instance {}) created against model (instance {})", getInstanceNumber(), model.getInstanceNumber());
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void bindDisconnectionEvents(EnvironmentModel environmentModel) {
        bindDisconnects(environmentModel);
    }

    private void bindDisconnects(EnvironmentModel environmentModel) {
        environmentModel.getConnectionStateStream().addListener(new com.logginghub.utils.StreamListener<ConnectionStateChangedEvent>() {
            public void onNewItem(ConnectionStateChangedEvent t) {

                if (t.getState() == State.NotConnected) {
                    ObservableList<PageModel> pages = model.getPages();
                    for (PageModel pageModel : pages) {
                        ObservableList<LineChartModel> chartingModels = pageModel.getChartingModels();
                        for (LineChartModel lineChartModel : chartingModels) {
                            // TODO : this is just broken, it'll always be null
                            LineChartView view = lineChartModel.getCounterpart();
                            if (view != null) {
                                view.addMarker(System.currentTimeMillis(), "");
                            }
                        }
                    }
                }
            }
        });
    }

    public Stream<ChunkedResult> getResultStreamFor(ChartSeriesModel chartModel) {
        return resultStreams.get(chartModel);
    }

    private void bindCharts(NewChartingModel model) {
        model.getPages().addListenerAndNotifyExisting(new ObservableListListener<PageModel>() {
            @Override public void onAdded(PageModel pageModel) {
                bindPageModel(pageModel);
            }

            @Override public void onRemoved(PageModel t) {}

            @Override public void onCleared() {}
        });
    }

    private void bindPageModel(PageModel pageModel) {

        pageModel.getChartingModels().addListenerAndNotifyExisting(new ObservableListListener<LineChartModel>() {
            @Override public void onAdded(final LineChartModel lineChartModel) {
                bindGenericChartModel(lineChartModel);
                bindLineChartModel(lineChartModel);
            }

            @Override public void onRemoved(LineChartModel t) {}

            @Override public void onCleared() {}
        });

        pageModel.getPieChartModels().addListenerAndNotifyExisting(new ObservableListListener<PieChartModel>() {
            @Override public void onAdded(final PieChartModel pieChartModel) {
                bindGenericChartModel(pieChartModel);
                bindPieChartModel(pieChartModel);
            }

            @Override public void onRemoved(PieChartModel t) {}

            @Override public void onCleared() {}
        });

    }

    public NewChartingModel getModel() {
        return model;
    }

    public LogEventMultiplexer getLogEventMultiplexer() {
        return logEventMultiplexer;
    }

    private void bindAggregators(NewChartingModel model) {
        model.getAggregationModels().addListenerAndNotifyExisting(new ObservableListListener<AggregationConfiguration>() {

            @Override public void onRemoved(AggregationConfiguration t) {
                // Decouple the aggregator from the stream
                NewAggregatorSplitter removed = aggregatorsByAggregationConfiguration.remove(t);
                StreamBuilder streamBuilder = getStreamBuilder(t.getStreamID().get());
                streamBuilder.getOutput().addListener(removed);
            }

            @Override public void onCleared() {}

            @Override public void onAdded(AggregationConfiguration t) {

                final NewAggregatorSplitter splitter = new NewAggregatorSplitter();
                // final NewAggregator aggregator = new NewAggregator();
                aggregatorsByAggregationConfiguration.put(t, splitter);

                t.getInterval().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        splitter.setChunkInterval(TimeUtils.parseInterval(newValue));
                    }
                });

                t.getType().addListenerAndNotifyCurrent(new ObservablePropertyListener<AggregationType>() {
                    @Override public void onPropertyChanged(AggregationType oldValue, AggregationType newValue) {
                        splitter.setPublishingModes(newValue);
                    }
                });

                // Attach the aggregator to the appropriate stream
                StreamBuilder streamBuilder = getStreamBuilder(t.getStreamID().get());
                streamBuilder.getOutput().addListener(splitter);

            }
        });
    }

    private void bindValueStrippers(final NewChartingModel model) {
        // Wire up value strippers to pattern models
        model.getPatternModels().addListenerAndNotifyExisting(new ObservableListListener<PatternModel>() {
            @Override public void onRemoved(PatternModel t) {
                ValueStripper2 removed = valueStrippersByPattern.remove(t);
                logEventMultiplexer.removeLogEventListener(removed);
            }

            @Override public void onCleared() {}

            @Override public void onAdded(final PatternModel t) {

                logger.debug("Building value stripper for pattern model {}", t);
                final ValueStripper2 stripper = new ValueStripper2();
                stripper.setPatternName(t.getName().asString());

                t.getPattern().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        stripper.setPattern(newValue);
                    }
                });

                t.getDebug().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                    @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                        stripper.setDebug(newValue);
                    }
                });

                counterparts.put(t, stripper);
                // t.setCounterpart(stripper);
                valueStrippersByPattern.put(t, stripper);
                logEventMultiplexer.addLogEventListener(stripper);

            }
        });
    }

    protected void bindGenericChartModel(final AbstractChartModel chartModel) {
        chartModel.getResetAt().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                updateResetTime(chartModel);
            }
        });
    }

    private void bindLineChartModel(final LineChartModel lineChartModel) {
        lineChartModel.getMatcherModels().addListenerAndNotifyExisting(new ObservableListListener<ChartSeriesModel>() {

            @Override public void onAdded(final ChartSeriesModel chartSeriesModel) {
                // Local scope variables
                final NewAggregatorSplitter splitter = new NewAggregatorSplitter();
                Stream<StreamResultItem> output = new Stream<StreamResultItem>() {
                    @Override public void send(StreamResultItem t) {
                        super.send(t);
                    }
                };
                final StreamBuilder builder = new StreamBuilder(output, patternService);

                logger.debug("ChartSeriesModel '{}' added to chart '{}'", chartSeriesModel, lineChartModel);

                // Create a pair of helpers for this series
                setupGeneralStreamBuilder(chartSeriesModel, splitter, builder);

                // Wire them together
                builder.getOutput().addListener(splitter);

                // Make the results available to the view
                final Stream<ChunkedResult> stream = new Stream<ChunkedResult>();
                splitter.getOutputStream().addListener(new StreamListener<ChunkedResult>() {
                    @Override public void onNewItem(ChunkedResult t) {
                        stream.send(t);
                    }
                });

                splitterCounterparts.put(chartSeriesModel, splitter);
                streamBuilderCounterparts.put(chartSeriesModel, builder);

                resultStreams.put(chartSeriesModel, stream);

                // Couple to a value stripper
                chartSeriesModel.getPatternID().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {

                        logger.debug("Binding ChartSeriesModel to pattern '{}' (old one was '{}')", oldValue, newValue);

                        // Remember to tell the builder so it
                        // can build the series
                        // name
                        builder.setPatternID(newValue);

                        // If we were attached to a stripper
                        // before, decouple
                        if (oldValue != null) {
                            ValueStripper2 valueStripper = getValueStripper(oldValue);
                            if (valueStripper != null) {
                                valueStripper.removeResultListener(builder);
                            }
                        }

                        // Bind to the new one
                        ValueStripper2 valueStripper = getValueStripper(newValue);
                        if (valueStripper != null) {
                            valueStripper.addResultListener(builder);
                        }
                        else {
                            logger.warn("Coun't find value stripper called '{}', failed to bind chart series", newValue);
                        }
                    }
                });

                // Couple to a particular result label
                chartSeriesModel.getLabelIndex().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                        builder.setLabelIndex(newValue);
                    }
                });

                // Couple to a analysis type
                chartSeriesModel.getType().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        if (newValue != null) {
                            String capitals = StringUtils.capitalise(newValue);
                            try {
                                splitter.setPublishingModes(AggregationType.valueOf(capitals));
                            }
                            catch (IllegalArgumentException e) {
                                logger.warn("Type '{}' isn't a recognised type ({})", capitals, Arrays.toString(AggregationType.values()));
                            }
                        }
                    }
                });

                // Couple to the interval length
                chartSeriesModel.getInterval().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                        splitter.setChunkInterval(newValue);
                    }
                });
            }

            @Override public void onRemoved(ChartSeriesModel t) {

                logger.debug("ChartSeriesModel '{}' removed from chart '{}'", t, lineChartModel);

                StreamBuilder builder = streamBuilderCounterparts.remove(t);
                NewAggregatorSplitter splitter = splitterCounterparts.remove(t);

                // Decouple the builder and splitter
                builder.getOutput().removeListener(splitter);

                // Take the result stream out of the lookup list
                // -
                // TODO : the chart may still be bound?
                // TODO : if the chart reacts as well, if we
                // remove this first it
                // might not be able to decouple itself?
                Stream<ChunkedResult> removedStreak = resultStreams.remove(t);

                // Remove value stripper binding
                ValueStripper2 valueStripper = getValueStripper(t.getPatternID().get());
                if (valueStripper != null) {
                    valueStripper.removeResultListener(builder);
                }
            }

            @Override public void onCleared() {}

        });
    }

    private void bindPieChartModel(final PieChartModel chartModel) {

        // TODO : where does this vary from the line chart model?
        chartModel.getMatcherModels().addListenerAndNotifyExisting(new ObservableListListener<ChartSeriesModel>() {

            @Override public void onAdded(ChartSeriesModel chartSeriesModel) {
                // Local scope variables
                final NewAggregatorSplitter splitter = new NewAggregatorSplitter();
                Stream<StreamResultItem> output = new Stream<StreamResultItem>() {
                    @Override public void send(StreamResultItem t) {
                        super.send(t);
                    }
                };
                final StreamBuilder builder = new StreamBuilder(output, patternService);

                logger.debug("ChartSeriesModel '{}' added to chart '{}'", chartSeriesModel, chartModel);

                setupGeneralStreamBuilder(chartSeriesModel, splitter, builder);

                // Wire them together
                builder.getOutput().addListener(splitter);

                // Make the results available to the view
                final Stream<ChunkedResult> stream = new Stream<ChunkedResult>();
                splitter.getOutputStream().addListener(new StreamListener<ChunkedResult>() {
                    @Override public void onNewItem(ChunkedResult t) {
                        stream.send(t);
                    }
                });

                splitterCounterparts.put(chartSeriesModel, splitter);
                streamBuilderCounterparts.put(chartSeriesModel, builder);

                resultStreams.put(chartSeriesModel, stream);

                // Couple to a value stripper
                chartSeriesModel.getPatternID().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {

                        logger.debug("Binding ChartSeriesModel to pattern '{}' (old one was '{}')", oldValue, newValue);

                        // Remember to tell the builder so it
                        // can build the series
                        // name
                        builder.setPatternID(newValue);

                        // If we were attached to a stripper
                        // before, decouple
                        if (oldValue != null) {
                            ValueStripper2 valueStripper = getValueStripper(oldValue);
                            if (valueStripper != null) {
                                valueStripper.removeResultListener(builder);
                            }
                        }

                        // Bind to the new one
                        ValueStripper2 valueStripper = getValueStripper(newValue);
                        if (valueStripper != null) {
                            valueStripper.addResultListener(builder);
                        }
                        else {
                            logger.warn("Coun't find value stripper called '{}', failed to bind chart series", newValue);
                        }
                    }
                });

                // Couple to a particular result label
                chartSeriesModel.getLabelIndex().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                        builder.setLabelIndex(newValue);
                    }
                });

                // Couple to a analysis type
                chartSeriesModel.getType().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        if (newValue != null) {
                            String capitals = StringUtils.capitalise(newValue);
                            try {
                                splitter.setPublishingModes(AggregationType.valueOf(capitals));
                            }
                            catch (IllegalArgumentException e) {
                                logger.warn("Type '{}' isn't a recognised type ({})", capitals, Arrays.toString(AggregationType.values()));
                            }
                        }
                    }
                });

                // Couple to the interval length
                chartSeriesModel.getInterval().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
                    @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                        splitter.setChunkInterval(newValue);
                    }
                });
            }

            @Override public void onRemoved(ChartSeriesModel t) {

                logger.debug("ChartSeriesModel '{}' removed from chart '{}'", t, chartModel);

                StreamBuilder builder = streamBuilderCounterparts.remove(t);
                NewAggregatorSplitter splitter = splitterCounterparts.remove(t);

                // Decouple the builder and splitter
                builder.getOutput().removeListener(splitter);

                // Take the result stream out of the lookup list
                // -
                // TODO : the chart may still be bound?
                // TODO : if the chart reacts as well, if we
                // remove this first it
                // might not be able to decouple itself?
                Stream<ChunkedResult> removedStreak = resultStreams.remove(t);

                // Remove value stripper binding
                ValueStripper2 valueStripper = getValueStripper(t.getPatternID().get());
                if (valueStripper != null) {
                    valueStripper.removeResultListener(builder);
                }
            }

            @Override public void onCleared() {}

        });
    }

    // private void bindStreamBuilders(final NewChartingModel model) {
    // // Wire up stream builders to the stream builder definitions
    // model.getStreamModels().addListenerAndNotifyExisting(new
    // ObservableListListener<StreamConfiguration>() {
    //
    // @Override public void onRemoved(StreamConfiguration t) {
    // StreamBuilder removed = streamBuildersByDefintion.remove(model);
    //
    // ValueStripper2 valueStripper = getValueStripper(removed.getPatternName());
    // if (valueStripper != null) {
    // valueStripper.removeResultListener(removed);
    // }
    // }
    //
    // @Override public void onCleared() {}
    //
    // @Override public void onAdded(final StreamConfiguration t) {
    //
    // logger.debug("Building stream builder for stream definition {}", t);
    // Stream<StreamResultItem> output = new Stream<StreamResultItem>();
    //
    // final StreamBuilder streamBuilder = new StreamBuilder(output);
    //
    // t.getLabelName().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
    // @Override public void onPropertyChanged(String oldValue, String newValue) {
    // streamBuilder.setLabel(newValue);
    // }
    // });
    //
    // t.getPatternName().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
    // @Override public void onPropertyChanged(String oldValue, String newValue) {
    //
    // // Decouple from the current stream
    // ValueStripper2 valueStripper = getValueStripper(oldValue);
    // if (valueStripper != null) {
    // valueStripper.removeResultListener(streamBuilder);
    // }
    //
    // streamBuilder.setPatternName(newValue);
    //
    // // Couple to the new stream
    // valueStripper = getValueStripper(newValue);
    // if (valueStripper != null) {
    // valueStripper.addResultListener(streamBuilder);
    // }
    // else {
    // logger.warn("No value stripper found for pattern {}", newValue);
    // }
    // }
    // });
    //
    // t.getEventElements().addListener(new ObservableListListener<String>() {
    // @Override public void onRemoved(String element) {
    // streamBuilder.setEventElements(t.getEventElements());
    // }
    //
    // @Override public void onCleared() {}
    //
    // @Override public void onAdded(String element) {
    // streamBuilder.setEventElements(t.getEventElements());
    // }
    // });
    // streamBuilder.setEventElements(t.getEventElements());
    //
    // streamBuildersByDefintion.put(t, streamBuilder);
    //
    // }
    // });
    // }

    public ValueStripper2 getValueStripper(int searchPatternID) {
        ValueStripper2 result = null;
        Set<Entry<PatternModel, ValueStripper2>> entrySet = valueStrippersByPattern.entrySet();
        for (Entry<PatternModel, ValueStripper2> entry : entrySet) {
            PatternModel key = entry.getKey();
            int thisPatternID = key.getPatternID().get();
            if (thisPatternID == searchPatternID) {
                result = entry.getValue();
                break;
            }
        }

        return result;
    }

    public StreamBuilder getStreamBuilder(String streamID) {
        StreamBuilder result = null;
        Set<Entry<StreamConfiguration, StreamBuilder>> entrySet = streamBuildersByDefintion.entrySet();
        for (Entry<StreamConfiguration, StreamBuilder> entry : entrySet) {
            if (entry.getKey().getStreamID().get().equals(streamID)) {
                result = entry.getValue();
                break;
            }
        }

        return result;
    }

    public void addStreamDefinition(StreamConfiguration streamDefinitionModel) {
        model.getStreamModels().add(streamDefinitionModel);
    }

    public void addPattern(PatternModel newPattern) {

        int patternID = nextLocalPatternID.getAndIncrement();
        newPattern.getPatternID().set(patternID);

        model.getPatternModels().add(newPattern);
    }

    public StreamBuilder getStreamBuilderForModel(StreamConfiguration newValue) {
        StreamBuilder streamBuilder = streamBuildersByDefintion.get(newValue);
        return streamBuilder;
    }

    public NewAggregatorSplitter addAggregation(AggregationConfiguration aggregationConfiguration) {
        model.getAggregationModels().add(aggregationConfiguration);

        // This will have gone and down lots of things via listeners
        NewAggregatorSplitter splitter = aggregatorsByAggregationConfiguration.get(aggregationConfiguration);
        return splitter;
    }

    public String getNextStreamID() {

        int max = 0;
        ObservableList<StreamConfiguration> streamModels = model.getStreamModels();
        for (StreamConfiguration configuration : streamModels) {

            String string = configuration.getStreamID().get();
            try {
                int integer = Integer.parseInt(string);
                max = Math.max(max, integer);
            }
            catch (NumberFormatException e) {
                // Ignore it, non-numeric user field
            }
        }

        max++;

        return Integer.toString(max);

    }

    public PatternModel getPatternModel(String string) {
        PatternModel found = null;
        for (PatternModel patternModel : model.getPatternModels()) {
            if (patternModel.getName().equals(string)) {
                found = patternModel;
                break;
            }
        }
        return found;
    }

    public void removeChart(LineChartModel lineChartModel) {

        PageModel parentPage = lineChartModel.getParentPage();

        ObservableList<ChartSeriesModel> matcherModels = lineChartModel.getMatcherModels();
        // TODO : unbind everything

        parentPage.getChartingModels().remove(lineChartModel);

    }

    public void removeChart(PieChartModel pieChartModel) {

        PageModel parentPage = pieChartModel.getParentPage();

        ObservableList<ChartSeriesModel> matcherModels = pieChartModel.getMatcherModels();
        // TODO : unbind everything

        parentPage.getPieChartModels().remove(pieChartModel);

    }

    public void clearChartData() {
        Collection<NewAggregatorSplitter> values = splitterCounterparts.values();
        for (NewAggregatorSplitter splitter : values) {
            splitter.clear();
        }

    }

    public void start() {
        stop();
        tickThread = WorkerThread.everySecondDaemon("LoggingHub-NewChartingController-TickThread", new Runnable() {
            @Override public void run() {
                checkForResets();
                generateEmptyTicks();
            }
        });
    }

    protected void checkForResets() {

        final long now = timeProvider.getTime();
        model.visitCharts(new Visitor<AbstractChartModel>() {
            @Override public void visit(AbstractChartModel t) {

                if (t.shouldReset(now)) {
                    // Reset the view's internal model
                    t.reset();

                    // Reset the value splitters / aggregators - this in turn reset the chunkers
                    ObservableList<ChartSeriesModel> chartSeriesModels = t.getMatcherModels();
                    for (ChartSeriesModel chartSeriesModel : chartSeriesModels) {
                        NewAggregatorSplitter splitter = splitterCounterparts.get(chartSeriesModel);
                        splitter.clear();
                    }

                    updateResetTime(t);
                }
            }
        });
    }

    protected void updateResetTime(AbstractChartModel chartModel) {
        long now = timeProvider.getTime();
        String chart = chartModel.getTitle().get();
        String interval = chartModel.getResetAt().get();
        if (StringUtils.isNotNullOrEmpty(interval)) {
            try {
                chartModel.setResetAtTime(getResetTimeFor(now, chart, interval));
            }
            catch (Exception e) {
                logger.warn("Failed to parse reset time '{}' - chart reset interval cleared", interval);
                chartModel.setResetAtTime(Long.MAX_VALUE);
            }
        }
        else {
            chartModel.setResetAtTime(Long.MAX_VALUE);
        }
    }

    public long getResetTimeFor(long now, String chart, String interval) {
        String input = interval; // "10:00:00";

        // Out.div();
        // Out.out("Source time '{}'", Logger.toLocalDateString(now));
        // Out.out("We want to reset at '{}'", input);
        // Out.div();

        // Out.out("Time in ms '{}'", now);
        // Out.out("Time [local] '{}'", Logger.toLocalDateString(now));
        // Out.out("Time [UTC] '{}'", Logger.toDateString(now));
        // Out.div();

        Calendar calendar = TimeUtils.buildCalendarForTime(now, input);

        // Out.out("Calendar in ms '{}'", calendar.getTimeInMillis());
        // Out.out("Calendar [local] '{}'", Logger.toLocalDateString(calendar.getTimeInMillis()));
        // Out.out("Calendar [UTC] '{}'", Logger.toDateString(calendar.getTimeInMillis()));
        // Out.div();
        //
        // Out.out("Now      '{}'", now);
        // Out.out("Reset at '{}'", calendar.getTimeInMillis());
        // Out.div();

        if (calendar.getTimeInMillis() >= now) {
            // Out.out("Reset time in the future");
        }
        else {
            // Out.out("Reset time in the past, moving forwards to tomorrow");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Out.out("Next reset time in ms '{}'", calendar.getTimeInMillis());
        // Out.out("Next reset time [local] '{}'",
        // Logger.toLocalDateString(calendar.getTimeInMillis()));
        // Out.out("Next reset time [UTC] '{}'", Logger.toDateString(calendar.getTimeInMillis()));
        // Out.div();

        return calendar.getTimeInMillis();
    }

    protected void generateEmptyTicks() {
        Collection<NewAggregatorSplitter> values = splitterCounterparts.values();
        for (NewAggregatorSplitter newAggregatorSplitter : values) {
            if (newAggregatorSplitter.isGenerateEmptyTicks()) {
                newAggregatorSplitter.tick();
            }
        }
    }

    public void stop() {
        if (tickThread != null) {
            tickThread.stop();
        }
    }

    private void setupGeneralStreamBuilder(final ChartSeriesModel chartSeriesModel, final NewAggregatorSplitter splitter, final StreamBuilder builder) {
        builder.setEventParts(new String[] {});
        chartSeriesModel.getEventParts().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                builder.setStream(newValue);
            }
        });

        // Setup the group by support
        chartSeriesModel.getGroupBy().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                // jshaw - workaround for old configuration data
                if(newValue != null && newValue.equals("null")) {
                    builder.setGroupBy(null);
                }
                else{
                    builder.setGroupBy(newValue);
                }
            }
        });

        // chartSeriesModel.getWhitelist().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<String>() {
        // @Override public void onPropertyChanged(String oldValue, String newValue) {
        // builder.parseWhitelist(chartSeriesModel.getGroupBy().asString(), newValue);
        // }
        // });
        //
        // chartSeriesModel.getBlacklist().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<String>() {
        // @Override public void onPropertyChanged(String oldValue, String newValue) {
        // builder.parseBlacklist(chartSeriesModel.getGroupBy().asString(), newValue);
        // }
        // });

        chartSeriesModel.getFilters().addListenerAndNotifyExisting(new ObservableListListener<ChartSeriesFilterModel>() {
            @Override public void onRemoved(ChartSeriesFilterModel t) {
                builder.removeFilter(t);
            }

            @Override public void onCleared() {
                builder.removeAllFilters();
            }

            @Override public void onAdded(ChartSeriesFilterModel t) {
                builder.addFilter(t);
            }
        });

        chartSeriesModel.getGenerateEmptyTicks().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                splitter.setGenerateEmptyTicks(newValue);
            }
        });
    }
}
