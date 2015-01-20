package com.logginghub.logging.modules;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.configuration.PatternConfiguration;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.logging.servers.DispatchQueue;
import com.logginghub.logging.servers.DispatchQueue.DispatchQueueConfiguration;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.logging.utils.ObservableListListener;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.Result;
import com.logginghub.utils.Source;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

@Provides(PatternisedLogEvent.class) public class PatterniserModule implements Module<PatterniserConfiguration>, Destination<LogEvent>,
                Source<PatternisedLogEvent> {

    private static final Logger logger = Logger.getLoggerFor(PatterniserModule.class);
    private PatterniserConfiguration configuration;
    private PatternCollection patternCollection = new PatternCollection();

    private FactoryMap<String, MutableInt> debugCounters = new FactoryMap<String, MutableInt>() {
        @Override protected MutableInt createEmptyValue(String key) {
            return new MutableInt(0);
        }
    };

    private boolean outputStats;
    private Timer debugTimer;
    private DispatchQueue<LogEvent> queue;
    private PatternManagerService patternManager;

    @Override public void configure(PatterniserConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        patternCollection.setMatchAgainstAllPatterns(configuration.isMatchAgainstAllPatterns());

        this.outputStats = configuration.isOutputStats();

        List<PatternConfiguration> patterns = configuration.getPatterns();
        patternCollection.configureFromConfigurations(patterns);

        patternManager = discovery.findService(PatternManagerService.class);

        // HACK: for tests that dont both setting up dependencies
        Result<ObservableList<Pattern>> result = patternManager.getPatterns();
        if (result != null) {
            
            ObservableList<Pattern> value = result.getValue();
            
            value.addListenerAndNotifyCurrent(new ObservableListListener<Pattern>() {
                @Override public void onRemoved(Pattern t) {
                    patternCollection.remove(t);
                }              
                
                @Override public void onAdded(Pattern t) {
                    patternCollection.add(t);    
                }
            });
        }

        Source<LogEvent> logEventSource = discovery.findService(Source.class, LogEvent.class, configuration.getLogEventSourceRef());
        logEventSource.addDestination(this);

        if (configuration.isUseQueue()) {
            queue = new DispatchQueue<LogEvent>();
            DispatchQueueConfiguration queueConfiguration = new DispatchQueueConfiguration();
            queueConfiguration.setAsynchronousQueueDiscardSize(configuration.getMaximumQueueSize());
            queueConfiguration.setAsynchronousQueueWarningSize((int) (configuration.getMaximumQueueSize() * 0.8));
            queueConfiguration.setName("PatterniserProcessingQueue");
            queue.configure(queueConfiguration, new ProxyServiceDiscovery());

            queue.addDestination(new Destination<LogEvent>() {
                @Override public void send(LogEvent t) {
                    processEvent(t);
                }
            });
        }

        final ChannelMessagingService channelMessaging = discovery.findService(ChannelMessagingService.class);
        patternCollection.addDestination(new Destination<PatternisedLogEvent>() {
            @Override public void send(PatternisedLogEvent t) {
                if(outputStats) {
                    debugCounters.get("" + t.getPatternID()).increment();
                }
                
                String channel = Channels.getPatternisedStream(t.getPatternID());
                logger.fine("Broadcasting patternised data via channel messaging on channel '{}'", channel);
                ChannelMessage message = new ChannelMessage(channel, t);
                try {
                    channelMessaging.send(message);
                }
                catch (LoggingMessageSenderException e) {
                    logger.info(e, "Failed to send message '{}'", message);
                }
            }
        });

    }

    @Override public void start() {
        stop();

        if (outputStats) {
            debugTimer = TimerUtils.everySecond("PatterniserDebugger", new Runnable() {
                @Override public void run() {
                    logDebugStats();
                }
            });
        }

        if (queue != null) {
            queue.start();
        }
    }

    protected void logDebugStats() {
        synchronized (debugCounters) {
            StringBuilder builder = new StringBuilder();

            builder.append("PatterniserModule status : ");

            Set<Entry<String, MutableInt>> entrySet = debugCounters.entrySet();
            String div = "";
            for (Entry<String, MutableInt> entry : entrySet) {
                builder.append(div).append("'").append(entry.getKey()).append("' = ").append(entry.getValue().getValue());
                div = " ";
                entry.getValue().setValue(0);
            }

            logger.info(builder.toString());
        }
    }

    @Override public void stop() {
        if (debugTimer != null) {
            debugTimer.cancel();
            debugTimer = null;
        }

        if (queue != null) {
            queue.stop();
        }
    }

    @Override public void addDestination(Destination<PatternisedLogEvent> listener) {
        patternCollection.addDestination(listener);
    }

    @Override public void removeDestination(Destination<PatternisedLogEvent> listener) {
        patternCollection.removeDestination(listener);
    }

    @Override public void send(LogEvent t) {
        if (queue != null) {
            queue.send(t);
        }
        else {
            processEvent(t);
        }
    }

    /**
     * Patternise this event using the first pattern that matches.
     * 
     * @param t
     * @return
     */
    public PatternisedLogEvent patternise(LogEvent t) {
        patternCollection.patternise(t);
        return null;
    }

    private void processEvent(LogEvent t) {
        patternCollection.send(t);
    }

    public void waitForQueueToDrain() {
        if (queue != null) {
            queue.waitForQueueToDrain();
        }
    }

    public DefaultLogEvent depatternise(PatternisedLogEvent patternisedLogEvent) {
        return patternCollection.depatternise(patternisedLogEvent);
    }

}
