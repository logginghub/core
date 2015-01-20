package com.logginghub.logging.modules;

import java.util.List;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.configuration.AggregationConfiguration;
import com.logginghub.logging.modules.configuration.AggregatorConfiguration;
import com.logginghub.logging.utils.AggregatorCore;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.logging.utils.ObservableListListener;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.Result;
import com.logginghub.utils.Source;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;

@Provides(AggregatedLogEvent.class) public class AggregatorModule implements Module<AggregatorConfiguration>, Destination<PatternisedLogEvent>,
                Source<AggregatedLogEvent> {

    private static Logger logger = Logger.getLoggerFor(AggregatorModule.class);
    private AggregatorConfiguration configuration;

    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.Log, logger);

    private WorkerThread debugTimer;
    private ChannelMessagingService channelMessagingService;
    private PatternManagerService patternManager;

    private AggregatorCore aggregatorCore;

    public AggregatorModule() {
        
    }
    
    @Override public void addDestination(Destination<AggregatedLogEvent> listener) {
        aggregatorCore.addDestination(listener);
    }

    @Override public void removeDestination(Destination<AggregatedLogEvent> listener) {
        aggregatorCore.removeDestination(listener);
    }
    
    public void setPatternManager(PatternManagerService patternManager) {
        this.patternManager = patternManager;
    }

    @Override public void configure(AggregatorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        patternManager = discovery.findService(PatternManagerService.class);
        aggregatorCore = new AggregatorCore(patternManager);

        channelMessagingService = discovery.findService(ChannelMessagingService.class);

        List<AggregationConfiguration> aggregations = configuration.getAggregations();
        for (AggregationConfiguration aggregationConfiguration : aggregations) {
            Aggregation aggregation = new Aggregation();

            String eventParts = aggregationConfiguration.getEventParts();
            aggregation.setAggregationID(aggregationConfiguration.getAggregationID());
            aggregation.setGroupBy(eventParts);
            aggregation.setCaptureLabelIndex(aggregationConfiguration.getLabelIndex());
            aggregation.setPatternID(aggregationConfiguration.getPatternID());
            aggregation.setType(AggregationType.valueOf(aggregationConfiguration.getType()));
            aggregation.setInterval(TimeUtils.parseInterval(aggregationConfiguration.getInterval()));

            logger.fine("Adding aggregation '{}'", aggregationConfiguration);
            aggregatorCore.addAggregation(aggregation);
        }

        // HACK: for tests that dont both setting up dependencies
        Result<ObservableList<Aggregation>> result = patternManager.getAggregations();
        if (result != null) {

            ObservableList<Aggregation> value = result.getValue();

            value.addListenerAndNotifyCurrent(new ObservableListListener<Aggregation>() {
                @Override public void onRemoved(Aggregation t) {}

                @Override public void onAdded(Aggregation aggregation) {
                    logger.fine("Adding aggregation '{}'", aggregation);
                    AggregatorModule.this.aggregatorCore.addAggregation(aggregation);
                }
            });

        }

        @SuppressWarnings("unchecked") Source<PatternisedLogEvent> eventSource = discovery.findService(Source.class,
                                                                                                       PatternisedLogEvent.class,
                                                                                                       configuration.getPatternisedEventSourceRef());
        eventSource.addDestination(this);
        
        aggregatorCore.addDestination(new Destination<AggregatedLogEvent>() {
            @Override public void send(AggregatedLogEvent t) {
                dispatch(t);
            }
        });
    }

    @Override public void send(PatternisedLogEvent t) {
        aggregatorCore.send(t);
    }

    private void dispatch(AggregatedLogEvent event) {

        logger.fine("Dispatching aggregated log event '{}'", event);

        String newChannel = Channels.getAggregatedStream(event.getAggregationID());
        ChannelMessage newMessage = new ChannelMessage(newChannel, event);
        try {
            channelMessagingService.send(newMessage);
        }
        catch (LoggingMessageSenderException e) {
            exceptionPolicy.handle(e);
        }
    }

    @Override public void start() {
        stop();

        aggregatorCore.start();

        // if (configuration.isOutputStats()) {
        // debugTimer = WorkerThread.every("IntervalExpiryTimer",
        // TimeUtils.parseInterval(configuration.getStatsInterval()),
        // TimeUnit.MILLISECONDS,
        // new Runnable() {
        // @Override public void run() {
        // logDebugStatus();
        // }
        // });
        // }
    }

    protected void logDebugStatus() {
        // synchronized (openIntervals) {
        // Set<Entry<OpenIntervalKey, OpenInterval>> entrySet = openIntervals.entrySet();
        // for (Entry<OpenIntervalKey, OpenInterval> entry : entrySet) {
        // logger.info("Aggregator debug stats : {}={} ",
        // entry.getValue().getAggregationKey().getAggregationID(), entry.getValue().getValue());
        // }
        // }
    }

    protected void checkOpenIntervals() {
        aggregatorCore.checkOpenIntervals();
    }

    @Override public void stop() {
        if (debugTimer != null) {
            debugTimer.stop();
            debugTimer = null;
        }
        
        aggregatorCore.stop();
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        aggregatorCore.setTimeProvider(timeProvider);
    }

    public TimeProvider getTimeProvider() {
        return aggregatorCore.getTimeProvider();
    }

}
