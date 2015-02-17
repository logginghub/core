package com.logginghub.logging.modules;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.*;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.modules.configuration.StackHistoryConfiguration;
import com.logginghub.logging.repository.SofBlockStreamRotatingReader;
import com.logginghub.logging.repository.SofBlockStreamRotatingWriter;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.servers.SocketHubMessageHandler;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.NamedThreadFactory;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.filter.CompositeAndFilter;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StackHistoryModule implements Module<StackHistoryConfiguration>, Destination<StackSnapshot> {

    private static final Logger logger = Logger.getLoggerFor(StackHistoryModule.class);

    private static final Logger safeLogger = Logger.getSafeLoggerFor(StackHistoryModule.class);

    private StackHistoryConfiguration configuration;

    private ExecutorService threadPool;

    private SofBlockStreamRotatingWriter writer;
    private SofBlockStreamRotatingReader reader;

    private AtomicInteger nextJobNumber = new AtomicInteger(0);

    private Map<Integer, Boolean> jobKillFlags = new HashMap<Integer, Boolean>();

    /**
     * Used for testing that things have been flushed, it bypasses checking the unflushed data buffers
     */
    private boolean checkLatest = true;

    @SuppressWarnings("unchecked") @Override public void configure(StackHistoryConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        File folder = new File(configuration.getFolder());
        String prefix = "stack.";
        String postfix = ".binary";
        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(StackSnapshot.class, 0);
        sofConfiguration.registerType(StackTrace.class, 1);
        sofConfiguration.registerType(StackTraceItem.class, 2);
        sofConfiguration.registerType(InstanceKey.class, 3);

        writer = new SofBlockStreamRotatingWriter(folder, prefix, postfix, sofConfiguration);
        writer.setTotalFileSizeLimit(configuration.getTotalFileSizeLimit());
        writer.setRotationSize(ByteUtils.parse(configuration.getFileSizeLimit()));
        writer.setBlocksize((int) ByteUtils.parse(configuration.getBlockSize()));
        writer.setMaximumFlushInterval(TimeUtils.parseInterval(configuration.getMaximumFlushInterval()));
        writer.setUseEventTimes(configuration.isUseEventTimes());

        reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, sofConfiguration);

        SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);

        socketHub.addMessageListener(HistoricalStackDataRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                threadPool.execute(new Runnable() {
                    @Override public void run() {
                        handleDataRequestStreaming((HistoricalStackDataRequest) message, source);
                    }
                });
            }
        });

        socketHub.addMessageListener(HistoricalStackDataJobKillRequest.class, new SocketHubMessageHandler() {
            @Override public void handle(final LoggingMessage message, final SocketConnectionInterface source) {
                handleJobKillRequest((HistoricalStackDataJobKillRequest) message, source);
            }
        });

        if (!configuration.isReadOnly()) {
            ChannelMessagingService service = discovery.findService(ChannelMessagingService.class);
            service.subscribe(Channels.stackSnapshots, new Destination<ChannelMessage>() {
                @Override public void send(ChannelMessage channelMessage) {
                    StackSnapshot snapshot = (StackSnapshot) channelMessage.getPayload();
                    StackHistoryModule.this.send(snapshot);
                }
            });

        }
    }

    private void handleJobKillRequest(HistoricalStackDataJobKillRequest message, SocketConnectionInterface source) {

        jobKillFlags.put(message.getJobNumber(), true);

        BaseRequestResponseMessage response = new BaseRequestResponseMessage();
        response.setCorrelationID(message.getCorrelationID());
        try {
            source.send(response);
        } catch (LoggingMessageSenderException e) {
            logger.info(e, "Failed to send job kill response");
        }

    }

    public void dumpIndex() throws IOException, SofException {
        reader.dumpIndex();
    }

    public HistoricalStackDataResponse handleDataRequest(HistoricalDataRequest message) {

        logger.fine("Handling data request : from '{}' to '{}'", Logger.toDateString(message.getStart()), Logger.toDateString(message.getEnd()));

        final List<StackSnapshot> matchingEvents = new ArrayList<StackSnapshot>();

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                matchingEvents.add((StackSnapshot) t);
            }
        };

        try {
            reader.visit(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
        } catch (SofException e) {
            logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
        }

        if (checkLatest) {
            try {
                writer.visitLatest(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
            } catch (SofException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            } catch (EOFException e) {
                logger.warn(e, "Failed to extract historical index elements for request '{}'", message);
            }
        }

        StackSnapshot[] snapshots = matchingEvents.toArray(new StackSnapshot[matchingEvents.size()]);

        HistoricalStackDataResponse response = new HistoricalStackDataResponse();
        response.setSnapshots(snapshots);
        response.setCorrelationID(message.getCorrelationID());

        return response;
    }

    public void handleDataRequestStreaming(final HistoricalStackDataRequest message, final QueueAwareLoggingMessageSender source) {

        final int jobNumber = nextJobNumber.getAndIncrement();
        jobKillFlags.put(jobNumber, false);

        Stopwatch sw = Stopwatch.start("");
        logger.info("Handling streaming data request : from '{}' to '{}' : level filter '{}' and quick filter '{}'",
                Logger.toDateString(message.getStart()),
                Logger.toDateString(message.getEnd()),
                message.getLevelFilter(),
                message.getQuickfilter());

        final List<StackSnapshot> batch = new ArrayList<StackSnapshot>();

        final MutableInt counter = new MutableInt(0);
        final MutableInt elements = new MutableInt(0);

        final CompositeAndFilter<StackSnapshot> eventFilter = new CompositeAndFilter<StackSnapshot>();
        // TODO : support the instance filter in the request
        if (StringUtils.isNotNullOrEmpty(message.getQuickfilter())) {

        }

        Destination<SerialisableObject> visitor = new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                StackSnapshot event = (StackSnapshot) t;
                if (eventFilter.passes(event)) {
                    batch.add(event);
                    if (batch.size() == configuration.getStreamingBatchSize()) {

                        if (jobKillFlags.get(jobNumber)) {
                            logger.info("Job kill flag detected, killing historical search job");
                            throw new JobKilledException();
                        }

                        logger.debug("Sending batched data");
                        sendBatch(batch, message, source, jobNumber, false);
                        batch.clear();
                        counter.increment();
                    }
                    elements.increment();
                }
            }
        };

        boolean killed = false;

        if (message.isMostRecentFirst()) {
            if (!killed) {
                killed = visitLatest(message, visitor);
            }

            if (!killed) {
                killed = visitOlder(message, visitor);
            }
        } else {
            if (!killed) {
                killed = visitOlder(message, visitor);
            }

            if (!killed) {
                killed = visitLatest(message, visitor);
            }
        }

        if (!killed) {
            sendBatch(batch, message, source, jobNumber, true);
            counter.increment();
        }

        logger.info("Handling streaming data request completed successfully in {} : from '{}' to '{}' : {} elements sent in {} batches",
                sw.stopAndFormat(),
                Logger.toDateString(message.getStart()),
                Logger.toDateString(message.getEnd()),
                elements.value,
                counter.value);

    }

    private boolean visitOlder(HistoricalStackDataRequest message, Destination<SerialisableObject> visitor) {
        boolean killed = false;
        try {
            reader.visit(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
        } catch (SofException e) {
            logger.warn(e, "Failed to extract historical stack elements for request '{}'", message);
        } catch (JobKilledException e) {
            killed = true;
            logger.info("Stack job was killed, all done");
        }
        return killed;
    }

    private boolean visitLatest(HistoricalStackDataRequest message, Destination<SerialisableObject> visitor) {
        boolean killed = false;
        try {
            Stopwatch latestSW = Stopwatch.start("Visiting latest events");
            writer.visitLatest(message.getStart(), message.getEnd(), visitor, message.isMostRecentFirst());
            logger.info("{}", latestSW.stopAndFormat());
        } catch (SofException e) {
            logger.warn(e, "Failed to extract historical stack elements for request '{}'", message);
        } catch (EOFException e) {
            logger.warn(e, "Failed to extract historical stack elements for request '{}'", message);
        } catch (JobKilledException e) {
            logger.info("Job was killed, all done");
            killed = true;
        }

        return killed;
    }

    private void sendBatch(List<StackSnapshot> eventBatch, final HistoricalStackDataRequest message, final QueueAwareLoggingMessageSender source, int jobNumber, boolean lastBatch) {
        StackSnapshot[] snapshots = eventBatch.toArray(new StackSnapshot[eventBatch.size()]);
        HistoricalStackDataResponse response = new HistoricalStackDataResponse();
        response.setSnapshots(snapshots);
        response.setCorrelationID(message.getCorrelationID());
        response.setLastBatch(lastBatch);
        response.setJobNumber(jobNumber);
        try {
            boolean first = true;
            while (!source.isSendQueueEmpty()) {
                if (first) {
                    logger.debug("Pausing historical stack stream to wait for client to catch up...");
                    first = false;
                }
                ThreadUtils.sleep(10);
            }
            source.send(response);
        } catch (LoggingMessageSenderException e) {
            logger.warning(e, "Data request failed in {} ms : responding to requestID '{}' with '{}' elements", "?", response.getCorrelationID(), response.getSnapshots().length);
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


    public void flush() throws SofException, IOException {
        writer.flush();
    }

    public void setCheckLatest(boolean checkLatest) {
        this.checkLatest = checkLatest;
    }

    @Override public void send(StackSnapshot t) {
        safeLogger.finest("Handing new stack trace : {}", t);

        try {
            writer.send(t);
        } catch (IOException e) {
            logger.warn(e, "Failed to write log event to disk");
        } catch (SofException e) {
            logger.warn(e, "Failed to write log event to disk");
        }
    }
}
