package com.logginghub.logging.modules;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.filters.LogEventLevelFilter;
import com.logginghub.logging.filters.MultipleEventContainsFilter;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.modules.configuration.DiskHistoryConfiguration;
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
import com.logginghub.utils.StringUtils;
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

public class DiskHistoryModule implements Module<DiskHistoryConfiguration>, HistoryService {

    private static final Logger logger = Logger.getLoggerFor(DiskHistoryModule.class);

    private static final Logger safeLogger = Logger.getSafeLoggerFor(DiskHistoryModule.class);

    private DiskHistoryConfiguration configuration;

    private ExecutorService threadPool;

    private SofBlockStreamRotatingWriter writer;
    private SofBlockStreamRotatingReader reader;

    /**
     * Used for testing that things have been flushed, it bypasses checking the unflushed data
     * buffers
     */
    private boolean checkLatest = true;

    private WorkerThread indexPublisher;

    @SuppressWarnings("unchecked") @Override public void configure(DiskHistoryConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        File folder = new File(configuration.getFolder());
        String prefix = "hub.log.";
        String postfix = ".binary";
        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(DefaultLogEvent.class, 0);

        writer = new SofBlockStreamRotatingWriter(folder, prefix, postfix, sofConfiguration);
        writer.setTotalFileSizeLimit(configuration.getTotalFileSizeLimit());
        writer.setRotationSize(ByteUtils.parse(configuration.getFileSizeLimit()));
        writer.setBlocksize((int) ByteUtils.parse(configuration.getBlockSize()));
        writer.setMaximumFlushInterval(TimeUtils.parseInterval(configuration.getMaximumFlushInterval()));
        writer.setUseEventTimes(configuration.isUseEventTimes());

        reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, sofConfiguration);

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

        if (!configuration.isReadOnly()) {
            @SuppressWarnings("unchecked") Source<LogEvent> eventSource = discovery.findService(Source.class,
                                                                                                LogEvent.class,
                                                                                                configuration.getLogEventSourceRef());
            eventSource.addDestination(new Destination<LogEvent>() {
                @Override public void send(LogEvent t) {
                    DiskHistoryModule.this.send(t);
                }
            });
        }
    }

    public void dumpIndex() throws IOException, SofException {
        reader.dumpIndex();
    }

    public HistoricalDataResponse handleDataRequest(HistoricalDataRequest message) {

        logger.fine("Handling data request : from '{}' to '{}'",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()));

        final List<LogEvent> matchingEvents = new ArrayList<LogEvent>();

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                matchingEvents.add((DefaultLogEvent) t);
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
                writer.visitLatest(message.getStart(), message.getEnd(), visitor);
            }
            catch (SofException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
            catch (EOFException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
        }

        DefaultLogEvent[] responseEvents = matchingEvents.toArray(new DefaultLogEvent[matchingEvents.size()]);

        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setEvents(responseEvents);
        response.setCorrelationID(message.getCorrelationID());

        return response;
    }

    public void handleDataRequestStreaming(final HistoricalDataRequest message, final QueueAwareLoggingMessageSender source) {
        Stopwatch sw = Stopwatch.start("");
        logger.info("Handling streaming data request : from '{}' to '{}' : level filter '{}' and quick filter '{}'",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    message.getLevelFilter(),
                    message.getQuickfilter());

        final List<LogEvent> batch = new ArrayList<LogEvent>();

        final MutableInt counter = new MutableInt(0);
        final MutableInt elements = new MutableInt(0);

        final CompositeAndFilter<LogEvent> eventFilter = new CompositeAndFilter<LogEvent>();
        eventFilter.addFilter(new LogEventLevelFilter(message.getLevelFilter()));
        if (StringUtils.isNotNullOrEmpty(message.getQuickfilter())) {
            eventFilter.addFilter(new MultipleEventContainsFilter(message.getQuickfilter(), false));
        }

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                LogEvent event = (LogEvent) t;
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
                writer.visitLatest(message.getStart(), message.getEnd(), visitor);
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
        threadPool = Executors.newCachedThreadPool(new NamedThreadFactory("LoggingHub-DiskHistoryModule-Worker-"));
        writer.start();
    }

    @Override public void stop() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }

        writer.stop();
    }

    private void handleDataRequest(HistoricalDataRequest message, QueueAwareLoggingMessageSender source) {
        handleDataRequestStreaming(message, source);
    }

    public void send(LogEvent t) {
        safeLogger.finest("Handing new log event : {}", t);

        if (t instanceof DefaultLogEvent) {
            DefaultLogEvent defaultLogEvent = (DefaultLogEvent) t;
            try {
                writer.send(defaultLogEvent);
            }
            catch (IOException e) {
                logger.warn(e, "Failed to write log event to disk");
            }
            catch (SofException e) {
                logger.warn(e, "Failed to write log event to disk");
            }
        }
        else {
            logger.fine("Ignoring non-default log event '{}'", t);
        }
    }

    public void flush() throws SofException, IOException {
        writer.flush();
    }

    public void setCheckLatest(boolean checkLatest) {
        this.checkLatest = checkLatest;
    }
}
