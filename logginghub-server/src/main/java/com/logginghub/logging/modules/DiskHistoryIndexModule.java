package com.logginghub.logging.modules;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.modules.configuration.DiskHistoryIndexConfiguration;
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
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class DiskHistoryIndexModule implements Module<DiskHistoryIndexConfiguration>, HistoryService {

    private static final Logger logger = Logger.getLoggerFor(DiskHistoryIndexModule.class);

    private static final Logger safeLogger = Logger.getSafeLoggerFor(DiskHistoryIndexModule.class);

    private DiskHistoryIndexConfiguration configuration;

    private Destination<ChannelMessage> channelSubscriptionsModule;
    // private SofRepository sofRepository;

    private TimeProvider timeProvider = new SystemTimeProvider();

    private HistoricalIndexElement currentIndex;

    private SofBlockStreamRotatingWriter writer;
    private SofBlockStreamRotatingReader reader;

    private ExecutorService threadPool = Executors.newCachedThreadPool(new NamedThreadFactory("LoggingHub-DiskHistoryIndexModule-Worker-"));

    private WorkerThread indexPublisher;

    private long currentIndexStartTime = -1;

    @SuppressWarnings("unchecked") @Override public void configure(DiskHistoryIndexConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        // resetCurrentIndex();

        // LocalDiskRepositoryConfiguration config = new LocalDiskRepositoryConfiguration();
        // config.setDataFolder(configuration.getFolder());
        // config.setPrefix(configuration.getFilename() + ".");
        // config.setFileDurationMilliseconds(TimeUtils.parseInterval(configuration.getFileDuration()));

        File folder = new File(configuration.getFolder());
        String prefix = "hub.log.";
        String postfix = ".index";
        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(HistoricalIndexElement.class, 1);

        writer = new SofBlockStreamRotatingWriter(folder, prefix, postfix, sofConfiguration);
        writer.setTotalFileSizeLimit(configuration.getTotalFileSizeLimit());
        writer.setRotationSize(ByteUtils.parse(configuration.getFileSizeLimit()));
        writer.setBlocksize((int) ByteUtils.parse(configuration.getBlockSize()));
        writer.setMaximumFlushInterval(TimeUtils.parseInterval(configuration.getMaximumFlushInterval()));
        writer.setUseEventTimes(configuration.isTriggerFromEventTimes());

        reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, sofConfiguration);

        SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);

        socketHub.addMessageListener(HistoricalIndexRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                threadPool.execute(new Runnable() {
                    @Override public void run() {
                        handleIndexRequestStreaming((HistoricalIndexRequest) message, source);
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
                    DiskHistoryIndexModule.this.send(t);
                }
            });
        }

        channelSubscriptionsModule = discovery.findService(Destination.class, ChannelMessage.class);

        logger.info("Successfully configured DiskHistoryIndexModule");

    }

    public HistoricalIndexResponse handleIndexRequest(HistoricalIndexRequest message) {

        logger.info("Handling index request : from '{}' to '{}'",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()));

        final List<HistoricalIndexElement> index = new ArrayList<HistoricalIndexElement>();

        try {
            reader.visit(message.getStart(), message.getEnd(), new Destination<SerialisableObject>() {
                @Override public void send(SerialisableObject t) {
                    index.add((HistoricalIndexElement) t);
                }
            }, false);
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }

        try {
            writer.visitLatest(message.getStart(), message.getEnd(), new Destination<SerialisableObject>() {
                @Override public void send(SerialisableObject t) {
                    index.add((HistoricalIndexElement) t);
                }
            });
        }
        catch (EOFException e) {
            // This probably means we've tried to read a bit of
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }

        // sofRepository.populateIndex(index, );

        HistoricalIndexResponse response = new HistoricalIndexResponse();

        HistoricalIndexElement[] elements = new HistoricalIndexElement[index.size()];
        index.toArray(elements);
        response.setElements(elements);
        response.setCorrelationID(message.getCorrelationID());

        return response;
    }

    public void handleIndexRequestStreaming(final HistoricalIndexRequest message, final QueueAwareLoggingMessageSender source) {
        Stopwatch sw = Stopwatch.start("");
        logger.info("Handling streaming index request : from '{}' to '{}' : from connection '{}'",
                    Logger.toDateString(message.getStart()),
                    Logger.toDateString(message.getEnd()),
                    source);

        final List<HistoricalIndexElement> batch = new ArrayList<HistoricalIndexElement>();

        final MutableInt counter = new MutableInt(0);
        final MutableInt elements = new MutableInt(0);

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                batch.add((HistoricalIndexElement) t);
                if (batch.size() == configuration.getStreamingBatchSize()) {
                    sendBatch(batch, message, source, false);
                    batch.clear();
                    counter.increment();
                }
                elements.increment();
            }
        };

        try {
            reader.visit(message.getStart(), message.getEnd(), visitor, false);
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }

        try {
            writer.visitLatest(message.getStart(), message.getEnd(), visitor);
        }
        catch (EOFException e) {
            // This probably means we've tried to read a bit of
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }
        catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
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

    private void sendBatch(List<HistoricalIndexElement> eventBatch,
                           final HistoricalIndexRequest message,
                           final QueueAwareLoggingMessageSender source,
                           boolean lastBatch) {

        if (eventBatch.size() > 0) {
            logger.fine("Sending index response of {} elements from '{}' to '{}'",
                        eventBatch.size(),
                        Logger.toDateString(eventBatch.get(0).getTime()),
                        Logger.toDateString(eventBatch.get(eventBatch.size() - 1).getTime()));
        }
        else {
            logger.fine("Sending index response of {} elements", eventBatch.size());
        }

        HistoricalIndexElement[] responseEvents = eventBatch.toArray(new HistoricalIndexElement[eventBatch.size()]);
        HistoricalIndexResponse response = new HistoricalIndexResponse();
        response.setElements(responseEvents);
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
                           "Index request failed in {} ms : responding to requestID '{}' with '{}' elements",
                           "?",
                           response.getCorrelationID(),
                           response.getElements().length);
        }

    }

    @Override public void start() {
        stop();

        // sofRepository.startIndexUpdater();

        if (!configuration.isTriggerFromEventTimes()) {
            indexPublisher = WorkerThread.everySecond("LoggingHub-DiskHistoryModule-IndexPublisher", new Runnable() {
                @Override public void run() {
                    try {
                        if (currentIndex != null && currentIndex.getTotalCount() > 0) {
                            publishIndex();
                            resetCurrentIndex(timeProvider.getTime());
                        }
                    }
                    catch (SofException e) {
                        logger.warn(e, "Failed to write index to disk");
                    }
                }
            });
        }

        writer.start();
    }

    protected void publishIndex() throws SofException {

        HistoricalIndexResponse response = new HistoricalIndexResponse();
        response.setElements(new HistoricalIndexElement[] { new HistoricalIndexElement(currentIndex) });
        response.setCorrelationID(-1);
        ChannelMessage message = new ChannelMessage(Channels.historyUpdates, response);
        logger.finer("Publishing current index '{}'", currentIndex);

        // Push the message out to the hub
        channelSubscriptionsModule.send(message);

        // Push the index out to disk
        try {
            writer.send(currentIndex);
        }
        catch (IOException e) {
            logger.warn(e, "Failed to write index to disk");
        }

    }

    private void resetCurrentIndex(long time) {
        currentIndex.setInfoCount(0);
        currentIndex.setOtherCount(0);
        currentIndex.setWarningCount(0);
        currentIndex.setSevereCount(0);

        currentIndex.setInterval(1000);
        currentIndex.setTime(TimeUtils.chunk(time, 1000));
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    @Override public void stop() {
        // sofRepository.startIndexUpdater();

        if (indexPublisher != null) {
            indexPublisher.stop();
            indexPublisher = null;
        }

        writer.stop();
    }

    // private void handleDataRequest(HistoricalDataRequest message, QueueAwareLoggingMessageSender
    // source) {
    // handleDataRequestStreaming(message, source);
    // }

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

    public void send(LogEvent t) {
        safeLogger.finest("Handing new log event : {}", t);

        long time;
        if (configuration.isTriggerFromEventTimes()) {
            time = t.getOriginTime();
        }
        else {
            time = timeProvider.getTime();
        }

        if (currentIndex == null) {
            currentIndex = new HistoricalIndexElement();
            resetCurrentIndex(time);
        }

        if (configuration.isTriggerFromEventTimes()) {
            if (currentIndexStartTime == -1) {
                currentIndexStartTime = time;
            }
            else {

                if (time - currentIndexStartTime > 1000) {
                    try {
                        publishIndex();
                        resetCurrentIndex(time);
                    }
                    catch (SofException e) {
                        logger.warn(e, "Failed to publish index");
                    }

                    currentIndexStartTime = time;
                }
            }
        }

        currentIndex.increment(t);
    }

    public void flush() throws SofException, IOException {
        writer.flush();
    }

}
