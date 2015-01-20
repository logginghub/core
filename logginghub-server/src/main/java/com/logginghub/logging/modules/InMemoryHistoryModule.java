package com.logginghub.logging.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LZ4CompressionStrategy;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.SofSerialisationStrategy;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
import com.logginghub.logging.modules.history.CompressedBlockEventBuffer;
import com.logginghub.logging.modules.history.EventBuffer;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.servers.SocketHubMessageHandler;
import com.logginghub.utils.Batcher;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Is;
import com.logginghub.utils.NamedThreadFactory;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class InMemoryHistoryModule implements Module<InMemoryHistoryConfiguration>, HistoryService {

    private EventBuffer events;

    // private CircularArray<DefaultLogEvent> events;
    private static final Logger logger = Logger.getLoggerFor(InMemoryHistoryModule.class);

    private static final Logger safeLogger = Logger.getSafeLoggerFor(InMemoryHistoryModule.class);

    private InMemoryHistoryConfiguration configuration;

    private ExecutorService threadPool = Executors.newCachedThreadPool(new NamedThreadFactory("LoggingHub-InMemoryHistoryWorker-"));

    private Destination<ChannelMessage> channelSubscriptionsModule;

    private Batcher<HistoricalIndexElement> batcher;

    @SuppressWarnings("unchecked") @Override public void configure(InMemoryHistoryConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        if (!configuration.isDisableSafetyChecks()) {
            Is.greaterThanZero(ByteUtils.parse(configuration.getBlockSize()),
                               "The blockSize attribute of the inMemoryHistory configuration must be greater than zero - value is currently '{}'",
                               configuration.getBlockSize());

            Is.greaterThan(ByteUtils.parse(configuration.getMaximumSize()),
                           ByteUtils.parse(configuration.getBlockSize()),
                           "The maximumSize attribute must be larger than the blockSize attribute - current values are '{}' and '{}' respectively",
                           configuration.getMaximumSize(),
                           configuration.getBlockSize());

            Is.greaterThan(ByteUtils.parse(configuration.getMaximumSize()),
                           ByteUtils.megabytes(1),
                           "The maximumSize attribute ({}) must be larger than 1 MB - otherwise we risk not being able to store multiple blocks containing larger events",
                           configuration.getMaximumSize());

            Is.greaterThan(ByteUtils.parse(configuration.getBlockSize()),
                           ByteUtils.kilobytes(100),
                           "The blockSize attribute ({}) must be larger than 100 KB - otherwise we risk not being able to store larger events",
                           configuration.getBlockSize());

            Is.lessThan(ByteUtils.parse(configuration.getMaximumSize()),
                        Runtime.getRuntime().maxMemory() - ByteUtils.megabytes(50),
                        "The maximumSize ({}) is too large compared to the maximum amount of memory available to the JVM ({}) (with 50 MB reserved for GC headroom.) Please reduce the maximumSize or increase the JVM heap size using the -Xmx???m option.",
                        ByteUtils.format(ByteUtils.parse(configuration.getMaximumSize())),
                        ByteUtils.format(Runtime.getRuntime().maxMemory()));

            if (ByteUtils.parse(configuration.getBlockSize()) * 2 > ByteUtils.parse(configuration.getMaximumSize())) {
                logger.warning("The current block size '{}' and maximum size '{}' mean you'll only ever have one block active - this will effectively means you'll never store more than the block size, so it might mean you've misconfigured the maximumMemory",
                               configuration.getBlockSize(),
                               configuration.getMaximumSize());
            }
        }

        events = new CompressedBlockEventBuffer(new LZ4CompressionStrategy(),
                                                new SofSerialisationStrategy(false, true),
                                                (int) ByteUtils.parse(configuration.getBlockSize()),
                                                (int) ByteUtils.parse(configuration.getMaximumSize()));

        Timeout timeout = new Timeout(TimeUtils.parseInterval(configuration.getIndexBatcherTimeout()), TimeUnit.MILLISECONDS);
        batcher = new Batcher<HistoricalIndexElement>(timeout);

        batcher.addDestination(new Destination<List<HistoricalIndexElement>>() {
            @Override public void send(List<HistoricalIndexElement> t) {
                HistoricalIndexResponse response = new HistoricalIndexResponse();
                response.setElements(t.toArray(new HistoricalIndexElement[t.size()]));
                response.setCorrelationID(-1);
                ChannelMessage message = new ChannelMessage(Channels.historyUpdates, response);
                channelSubscriptionsModule.send(message);
            }
        });

        events.addIndexListener(new Destination<HistoricalIndexElement>() {
            @Override public void send(HistoricalIndexElement t) {
                batcher.send(t);
            }
        });

        // Dilemna - does this module reach out and wire itself into the socket hub, or does the
        // socket hub reach out to us?
        // What if there are multiple provides of history? I guess they all process the message?

        SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);

        socketHub.addMessageListener(HistoricalDataRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                threadPool.execute(new Runnable() {
                    @Override public void run() {
                        handleDataRequest((HistoricalDataRequest) message, source);
                    }
                });
            }
        });

        socketHub.addMessageListener(HistoricalIndexRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                threadPool.execute(new Runnable() {
                    @Override public void run() {
                        handleIndexRequest((HistoricalIndexRequest) message, source);
                    }
                });
            }
        });

        @SuppressWarnings("unchecked") Source<LogEvent> eventSource = discovery.findService(Source.class,
                                                                                            LogEvent.class,
                                                                                            configuration.getLogEventSourceRef());
        eventSource.addDestination(new Destination<LogEvent>() {
            @Override public void send(LogEvent t) {
                handleNewEvent(t);
            }
        });

        // TODO : conver to an interface so the proxy will work
        channelSubscriptionsModule = discovery.findService(Destination.class, ChannelMessage.class);

        logger.info("Successfully configured InMemoryHistoryModule with maximumSize '{}' and blockSize '{}'",
                    configuration.getMaximumSize(),
                    configuration.getBlockSize());

        // events = new CircularArray<DefaultLogEvent>(DefaultLogEvent.class,
        // configuration.getCircularBufferSize());
    }

    // public List<LogEvent> getEvents() {
    //
    // List<LogEvent> events = new ArrayList<LogEvent>();
    //
    // int size = this.events.size();
    // for (int i = 0; i < size; i++) {
    // events.add(this.events.get(i));
    // }
    //
    // return events;
    //
    // }

    public HistoricalDataResponse handleDataRequest(HistoricalDataRequest message) {

        int size = events.size();
        logger.fine("Handling data request : from '{}' to '{}' : we have '{}' events in the buffer",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    size);

        List<LogEvent> matchingEvents = new ArrayList<LogEvent>();

        events.extractEventsBetween(matchingEvents, message.getStart(), message.getEnd());
        //
        //
        // for (int i = 0; i < size; i++) {
        // LogEvent event = events.get(i);
        // long time = event.getOriginTime();
        // if (time >= message.getStart() && time < message.getEnd()) {
        // if (event instanceof DefaultLogEvent) {
        // DefaultLogEvent defaultLogEvent = (DefaultLogEvent) event;
        // matchingEvents.add(defaultLogEvent);
        // }
        // else {
        // // What to do! Internal logging might look like this! But we'd need to convert
        // // it into a default event to be able to serialise it!
        // }
        // }
        // }

        DefaultLogEvent[] responseEvents = matchingEvents.toArray(new DefaultLogEvent[matchingEvents.size()]);

        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setEvents(responseEvents);
        response.setCorrelationID(message.getCorrelationID());
        return response;
    }

    public void handleDataRequestStreaming(final HistoricalDataRequest message, final QueueAwareLoggingMessageSender source) {

        int size = events.size();
        logger.fine("Handling data request : from '{}' to '{}' : we have '{}' events in the buffer",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    size);

        final List<LogEvent> batch = new ArrayList<LogEvent>();

        Destination<LogEvent> visitor = new Destination<LogEvent>() {

            @Override public void send(LogEvent t) {
                batch.add(t);
                if (batch.size() == configuration.getStreamingBatchSize()) {
                    sendBatch(batch, message, source, false);
                    batch.clear();
                }
            }

        };

        events.extractEventsBetween(visitor, message.getStart(), message.getEnd());

        sendBatch(batch, message, source, true);

    }

    private void sendBatch(List<LogEvent> eventBatch,
                           final HistoricalDataRequest message,
                           final QueueAwareLoggingMessageSender source,
                           boolean lastBatch) {

        DefaultLogEvent[] responseEvents = eventBatch.toArray(new DefaultLogEvent[eventBatch.size()]);
        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setEvents(responseEvents);
        response.setCorrelationID(message.getCorrelationID());
        response.setLastBatch(lastBatch);

        try {
            while (!source.isSendQueueEmpty()) {
                ThreadUtils.sleep(10);
            }
            logger.fine("Sending batch of {} items to source {}", eventBatch.size(), source);
            source.send(response);
        }
        catch (LoggingMessageSenderException e) {
            logger.warning(e,
                           "Data request failed in {} ms : responding to requestID '{}' with '{}' elements",
                           "?",
                           response.getCorrelationID(),
                           response.getEvents().length);
        }

    }

    public HistoricalIndexResponse handleIndexRequest(HistoricalIndexRequest message) {
        int size = events.size();

        logger.fine("Handling index request : from '{}' to '{}' : we have '{}' events in the buffer",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    size);

        List<HistoricalIndexElement> index = new ArrayList<HistoricalIndexElement>();

        events.extractIndexBetween(index, message.getStart(), message.getEnd());

        // // TODO : support other periods
        // Indexifier model = new Indexifier(1000);
        //
        // for (int i = 0; i < size; i++) {
        // LogEvent event = events.get(i);
        // long time = event.getOriginTime();
        // if (time >= message.getStart() && time < message.getEnd()) {
        // model.addEvent(event);
        // }
        // }

        HistoricalIndexResponse response = new HistoricalIndexResponse();
        response.setElements(index.toArray(new HistoricalIndexElement[index.size()]));
        response.setCorrelationID(message.getCorrelationID());

        return response;
    }

    @Override public void start() {
        stop();
        if (batcher != null) {
            batcher.start();
        }
    }

    @Override public void stop() {
        if (batcher != null) {
            batcher.stop();
        }
    }

    private void handleDataRequest(HistoricalDataRequest message, QueueAwareLoggingMessageSender source) {
        handleDataRequestStreaming(message, source);
    }

    private void handleIndexRequest(HistoricalIndexRequest message, QueueAwareLoggingMessageSender source) {
        Stopwatch sw = Stopwatch.start("");
        HistoricalIndexResponse response = handleIndexRequest(message);

        try {
            source.send(response);
            logger.info("Index request successfully processed in {} ms : responding to requestID '{}' with '{}' elements",
                        sw.stopAndGetFormattedDurationMillis(),
                        response.getCorrelationID(),
                        response.getElements().length);
        }
        catch (LoggingMessageSenderException e) {
            logger.warning(e,
                           "Index request failed in {} ms : responding to requestID '{}' with '{}' elements",
                           sw.stopAndGetFormattedDurationMillis(),
                           response.getCorrelationID(),
                           response.getElements().length);
        }
    }

    public void handleNewEvent(LogEvent t) {
        safeLogger.finest("Handing new log event : {}", t);
        if (t instanceof DefaultLogEvent) {
            DefaultLogEvent defaultLogEvent = (DefaultLogEvent) t;
            events.addEvent(defaultLogEvent);
        }
        else {
            logger.fine("Ignoring non-default log event '{}'", t);
        }
    }

    public int getBlockSequence() {
        return events.getBlockSequence();
    }

}
