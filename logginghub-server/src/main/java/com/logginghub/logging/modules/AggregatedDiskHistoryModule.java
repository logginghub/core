package com.logginghub.logging.modules;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HealthCheckRequest;
import com.logginghub.logging.messages.HealthCheckResponse;
import com.logginghub.logging.messages.HistoricalAggregatedDataRequest;
import com.logginghub.logging.messages.HistoricalAggregatedDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.modules.configuration.AggregatedDiskHistoryConfiguration;
import com.logginghub.logging.repository.SofBlockStreamRotatingReader;
import com.logginghub.logging.repository.SofBlockStreamRotatingWriter;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.servers.SocketHubMessageHandler;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.NamedThreadFactory;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.filter.CompositeAndFilter;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class AggregatedDiskHistoryModule implements Module<AggregatedDiskHistoryConfiguration>, HistoryService {

    private static final Logger logger = Logger.getLoggerFor(AggregatedDiskHistoryConfiguration.class);

    private static final Logger safeLogger = Logger.getSafeLoggerFor(AggregatedDiskHistoryConfiguration.class);

    private AggregatedDiskHistoryConfiguration configuration;

    private ExecutorService threadPool;

    private SofBlockStreamRotatingWriter writer;
    private SofBlockStreamRotatingReader reader;

    /**
     * Used for testing that things have been flushed, it bypasses checking the unflushed data
     * buffers
     */
    private boolean checkLatest = true;

    private WorkerThread indexPublisher;

    @SuppressWarnings("unchecked") @Override public void configure(AggregatedDiskHistoryConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        File folder = new File(configuration.getFolder());
        String prefix = configuration.getFilename();
        String postfix = ".binary";
        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(AggregatedLogEvent.class, 0);

        writer = new SofBlockStreamRotatingWriter(folder, prefix, postfix, sofConfiguration);
        writer.setTotalFileSizeLimit(configuration.getTotalFileSizeLimit());
        writer.setRotationSize(ByteUtils.parse(configuration.getFileSizeLimit()));
        writer.setBlocksize((int) ByteUtils.parse(configuration.getBlockSize()));
        writer.setMaximumFlushInterval(TimeUtils.parseInterval(configuration.getMaximumFlushInterval()));
        writer.setUseEventTimes(configuration.isUseEventTimes());

        reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, sofConfiguration);

        SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);

        socketHub.addMessageListener(HistoricalAggregatedDataRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                threadPool.execute(new Runnable() {
                    @Override public void run() {
                        handleDataRequest((HistoricalAggregatedDataRequest) message, source);
                    }
                });
            }
        });

        final ChannelMessagingService channelMessagingService = discovery.findService(ChannelMessagingService.class);
        channelMessagingService.subscribe(Channels.aggregatedHistoryRequests, new Destination<ChannelMessage>() {
            @Override public void send(final ChannelMessage t) {
                SerialisableObject payload = t.getPayload();
                if (payload instanceof HealthCheckRequest) {
                    final HealthCheckRequest healthCheckRequest = (HealthCheckRequest) payload;
                    threadPool.execute(new Runnable() {
                        @Override public void run() {
                            processHealthCheckRequest(healthCheckRequest, t, channelMessagingService);
                        }
                    });
                }
            }
        });
        
        if (!configuration.isReadOnly()) {
            Source<AggregatedLogEvent> eventSource = discovery.findService(Source.class,
                                                                           AggregatedLogEvent.class,
                                                                           configuration.getLogEventSourceRef());
            eventSource.addDestination(new Destination<AggregatedLogEvent>() {
                @Override public void send(AggregatedLogEvent t) {
                    AggregatedDiskHistoryModule.this.send(t);
                }
            });
        }
    }

    protected void processHealthCheckRequest(HealthCheckRequest healthCheckRequest,
                                             final ChannelMessage request,
                                             final ChannelMessagingService channelMessagingService) {
        logger.info("Processing aggregated disk history health check request");

        reader.healthCheck(new Destination<String>() {
            @Override public void send(String t) {

                HealthCheckResponse response = new HealthCheckResponse();
                response.setContent(t);
                response.setMoreToFollow(true);

                ChannelMessage reply = new ChannelMessage();
                reply.setCorrelationID(request.getCorrelationID());
                reply.setChannel(request.getReplyToChannel());
                reply.setPayload(response);
                try {
                    channelMessagingService.send(reply);
                }
                catch (LoggingMessageSenderException e) {
                    logger.info("Failed to send health check response : {}", e.getMessage());
                }
            }
        });

        HealthCheckResponse response = new HealthCheckResponse();
        response.setContent("");
        response.setMoreToFollow(false);

        ChannelMessage reply = new ChannelMessage();
        reply.setCorrelationID(request.getCorrelationID());
        reply.setChannel(request.getReplyToChannel());
        reply.setPayload(response);
        try {
            channelMessagingService.send(reply);
        }
        catch (LoggingMessageSenderException e) {
            logger.info("Failed to send health check response : {}", e.getMessage());
        }
        logger.info("Processing aggregated disk history health check request complete.");
    }
    
    public void dumpIndex() throws IOException, SofException {
        reader.dumpIndex();
    }

    public HistoricalAggregatedDataResponse handleDataRequest(HistoricalAggregatedDataRequest message) {

        logger.fine("Handling data request : from '{}' to '{}'", Logger.toDateString(message.getStart()), Logger.toDateString(message.getEnd()));

        final List<AggregatedLogEvent> matchingEvents = new ArrayList<AggregatedLogEvent>();

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                matchingEvents.add((AggregatedLogEvent) t);
            }
        };

        if (checkLatest) {
            try {
                writer.visitLatest(message.getStart(), message.getEnd(), visitor, false);
            }
            catch (SofException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
            catch (EOFException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
        }
        
        try {
            reader.visit(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }


        AggregatedLogEvent[] responseEvents = matchingEvents.toArray(new AggregatedLogEvent[matchingEvents.size()]);

        HistoricalAggregatedDataResponse response = new HistoricalAggregatedDataResponse();
        response.setEvents(responseEvents);
        response.setCorrelationID(message.getCorrelationID());

        return response;
    }

    public void handleDataRequestStreaming(final HistoricalAggregatedDataRequest message, final QueueAwareLoggingMessageSender source) {
        Stopwatch sw = Stopwatch.start("");
        logger.info("Handling streaming data request : from '{}' to '{}' : level filter '{}' and quick filter '{}'",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    message.getLevelFilter(),
                    message.getQuickfilter());

        final List<AggregatedLogEvent> batch = new ArrayList<AggregatedLogEvent>();

        final MutableInt counter = new MutableInt(0);
        final MutableInt elements = new MutableInt(0);

        final CompositeAndFilter<AggregatedLogEvent> eventFilter = new CompositeAndFilter<AggregatedLogEvent>();
//        eventFilter.addFilter(new AggregatedLogEventLevelFilter(message.getLevelFilter()));
//        if (StringUtils.isNotNullOrEmpty(message.getQuickfilter())) {
//            eventFilter.addFilter(new AggregatedMultipleEventContainsFilter(message.getQuickfilter(), false));
//        }

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                AggregatedLogEvent event = (AggregatedLogEvent) t;
                if (eventFilter.passes(event)) {
                    batch.add(event);
                    if (batch.size() == configuration.getStreamingBatchSize()) {
                        sendBatch(batch, message, source, false);
                        batch.clear();
                        counter.increment();
                    }
                    elements.increment();
                }
            }
        };

        try {
            reader.visit(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }

        if (checkLatest) {
            try {
                Stopwatch latestSW = Stopwatch.start("Visiting latest events");
                writer.visitLatest(message.getStart(), message.getEnd(), visitor, false);
                logger.info("{}", latestSW.stopAndFormat());
            }
            catch (SofException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
            catch (EOFException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
        }

        sendBatch(batch, message, source, true);
        counter.increment();

        logger.info("Handling streaming data request completed successfully in {} : from '{}' to '{}' : {} elements sent in {} batches",
                    sw.stopAndFormat(),
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    elements.value,
                    counter.value);

    }

    private void sendBatch(List<AggregatedLogEvent> eventBatch,
                           final HistoricalAggregatedDataRequest message,
                           final QueueAwareLoggingMessageSender source,
                           boolean lastBatch) {
        AggregatedLogEvent[] responseEvents = eventBatch.toArray(new AggregatedLogEvent[eventBatch.size()]);
        HistoricalAggregatedDataResponse response = new HistoricalAggregatedDataResponse();
        response.setEvents(responseEvents);
        response.setCorrelationID(message.getCorrelationID());
        response.setLastBatch(lastBatch);

        try {
            while (!source.isSendQueueEmpty()) {
                ThreadUtils.sleep(10);
            }
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

    @Override public void start() {
        stop();
        threadPool = Executors.newCachedThreadPool(new NamedThreadFactory("LoggingHub-AggregatedDiskHistoryModule-Worker-"));
        writer.start();
    }

    @Override public void stop() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }

        writer.stop();
    }

    private void handleDataRequest(HistoricalAggregatedDataRequest message, QueueAwareLoggingMessageSender source) {
        handleDataRequestStreaming(message, source);
    }

    public void send(AggregatedLogEvent t) {
        safeLogger.finest("Handing new aggregated log event : {}", t);

        try {
            writer.send(t);
        }
        catch (IOException e) {
            logger.warn(e, "Failed to write log event to disk");
        }
        catch (SofException e) {
            logger.warn(e, "Failed to write log event to disk");
        }
    }

    public void flush() throws SofException, IOException {
        writer.flush();
    }

    public void setCheckLatest(boolean checkLatest) {
        this.checkLatest = checkLatest;
    }
}
