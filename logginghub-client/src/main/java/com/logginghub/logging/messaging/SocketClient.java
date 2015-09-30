package com.logginghub.logging.messaging;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.levelsetting.InstanceFilter;
import com.logginghub.logging.api.levelsetting.LevelSettingAPI;
import com.logginghub.logging.api.levelsetting.LevelSettingsConfirmation;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.api.levelsetting.MultipleResultListener;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.AggregationListRequest;
import com.logginghub.logging.api.patterns.AggregationListResponse;
import com.logginghub.logging.api.patterns.HistoricalDataAPI;
import com.logginghub.logging.api.patterns.InstanceDetails;
import com.logginghub.logging.api.patterns.InstanceManagementAPI;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternListRequest;
import com.logginghub.logging.api.patterns.PatternListResponse;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.api.patterns.PingRequest;
import com.logginghub.logging.api.patterns.PingResponse;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.AbstractLoggingMessageSource;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.ChannelSubscriptionRequestMessage;
import com.logginghub.logging.messages.ChannelSubscriptionResponseMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.EventSubscriptionRequestMessage;
import com.logginghub.logging.messages.EventSubscriptionResponseMessage;
import com.logginghub.logging.messages.FilterRequestMessage;
import com.logginghub.logging.messages.HistoricalAggregatedDataRequest;
import com.logginghub.logging.messages.HistoricalAggregatedDataResponse;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalPatternisedDataRequest;
import com.logginghub.logging.messages.HistoricalPatternisedDataResponse;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.MapMessage;
import com.logginghub.logging.messages.RequestResponseMessage;
import com.logginghub.logging.messages.ResponseMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.SubscriptionResponseMessage;
import com.logginghub.logging.messages.UnsubscriptionRequestMessage;
import com.logginghub.logging.messages.UnsubscriptionResponseMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Handler;
import com.logginghub.utils.LatchFuture;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Result;
import com.logginghub.utils.ResultListener;
import com.logginghub.utils.StreamingDestination;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Wraps the business logic side of being a logging client, backed by a SocketConnector to do the donkey work.
 *
 * @author admin
 */
public class SocketClient extends AbstractLoggingMessageSource implements LoggingMessageSender, Closeable, LogEventListener, ChannelMessagingService {
    private static final Logger logger = Logger.getLoggerFor(SocketClient.class);
    private Set<String> autoEventChannelSubscriptions = new HashSet<String>();
    private boolean autoGlobalSubscription = true;
    private boolean autoSubscribe = false;
    private SocketConnector connector;
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.SystemErr);
    private int levelFilter = Level.ALL.intValue();
    private AtomicInteger nextCorrelationID = new AtomicInteger(0);
    private boolean respondToPings = true;
    private Timeout timeout = Timeout.defaultTimeout;

    // private List<LoggingMessage> queued = new ArrayList<LoggingMessage>();
    private Map<Integer, Handler<LoggingMessage>> requestResponseHandlers = new HashMap<Integer, Handler<LoggingMessage>>();
    private SubscriptionController<Destination<ChannelMessage>, ChannelMessage> subscriptionController = new SubscriptionController<Destination<ChannelMessage>, ChannelMessage>() {
        @Override protected Future<Boolean> handleFirstSubscription(String channel) {
            return handleFirstSubscriptionInternal(channel);
        }

        @Override protected Future<Boolean> handleLastSubscription(String channel) {
            return handleLastSubscriptionInternal(channel);
        }
    };
    private int sent;
    private int pid;

    public SocketClient() {
        this("");
    }

    public SocketClient(SocketConnector connector) {
        this.connector = connector;

        connector.addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                handleMessage(message);
            }
        });

        connector.addSocketConnectorListener(new SocketConnectorListener() {
            public void onConnectionEstablished() {
                // sendQueued();
                reestablishSubscriptions();
            }

            public void onConnectionLost(String reason) {
            }
        });
    }

    public SocketClient(String name) {
        this(new SocketConnector(name));
    }

    /**
     * Convenience methods to create a new socket client, and automatically connect and attach the provided listener.
     *
     * @param inetSocketAddress
     * @param autoSubscribe
     * @param eventBucket
     * @return
     * @throws ConnectorException
     */
    public static SocketClient connect(InetSocketAddress connectionPoint, boolean autoSubscribe, LogEventListener listener) throws ConnectorException {
        SocketClient client = new SocketClient();
        client.addConnectionPoint(connectionPoint);
        client.setAutoSubscribe(autoSubscribe);
        client.addLogEventListener(listener);
        client.connect();
        return client;
    }

    private void setupPingHandler() {
        if (respondToPings) {
            subscribe(Channels.pingRequests, new Destination<ChannelMessage>() {
                @Override public void send(ChannelMessage message) {
                    ChannelMessage channelRequestMessage = (ChannelMessage) message;
                    SerialisableObject payload = channelRequestMessage.getPayload();
                    if (payload instanceof PingRequest) {
                        PingRequest pingRequest = (PingRequest) payload;

                        if (StringUtils.isNotNullOrEmpty(channelRequestMessage.getReplyToChannel())) {

                            InstanceDetails instanceDetails = getInstanceDetails();

                            PingResponse response = new PingResponse();
                            response.setInstanceDetails(instanceDetails);
                            response.setTimestamp(pingRequest.getTimestamp());

                            ChannelMessage reply = new ChannelMessage(channelRequestMessage.getReplyToChannel(), response);
                            reply.setCorrelationID(channelRequestMessage.getCorrelationID());

                            try {
                                SocketClient.this.send(reply);
                            } catch (LoggingMessageSenderException e) {
                                logger.warning(e, "Failed to send ping reply");
                            }
                        } else {
                            logger.warn("We have received a ping request with a null response to channel, ignoring");
                        }
                    }
                }
            });
        }
    }

    public void addAutoSubscription(String channel) {
        synchronized (autoEventChannelSubscriptions) {
            autoEventChannelSubscriptions.add(channel);
        }
    }

    public void addConnectionPoint(InetSocketAddress address) {
        connector.addConnectionPoint(address);
    }

    public void addConnectionPoints(List<InetSocketAddress> inetSocketAddressList) {
        for (InetSocketAddress inetSocketAddress : inetSocketAddressList) {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public Future<Boolean> addSubscription(String channel, Destination<ChannelMessage> destination) {
        return subscriptionController.addSubscription(channel, destination);
    }

    public void close() {
        connector.close();
    }

    public void connect() throws ConnectorException {
        logger.fine("Socket client connecting...");

        setupPingHandler();

        connector.connect();
        if (autoSubscribe) {
            try {
                // Check global subscriptions
                if (autoGlobalSubscription) {
                    logger.fine("Auto-subscribe is true, so sending subscription message");
                    subscribe();
                }

                // Channel subscriptions
                for (String channel : autoEventChannelSubscriptions) {
                    logger.info("Auto-subscribe is true, so sending subscription message for channel '{}'", channel);
                    subscribe(channel);
                }

                // If someone has set a level filter, need to pass that on as well
                if (levelFilter != Level.ALL.intValue()) {
                    logger.fine("A level filter has been set, sending to hub");
                    sendLevelFilter();
                }
            } catch (LoggingMessageSenderException e) {
                throw new ConnectorException("Failed to auto-subscribe", e);
            }
        }
    }

    public void disconnect() {
        connector.disconnect();
    }

    public void flush() {
        connector.flush();
    }

    public boolean getAutoSubscribe() {
        return autoSubscribe;
    }

    public void setAutoSubscribe(boolean autoSubscribe) {
        this.autoSubscribe = autoSubscribe;
    }

    public SocketConnector getConnector() {
        return connector;
    }

    public int getNextCorrelationID() {
        return nextCorrelationID.getAndIncrement();
    }

    public boolean isConnected() {
        return connector.isConnected();
    }

    public void onNewLogEvent(LogEvent event) {
        try {
            send(new LogEventMessage(event));
        } catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send log event message", e);
        }
    }

    public void removeAutoSubscription(String channel) {
        synchronized (autoEventChannelSubscriptions) {
            autoEventChannelSubscriptions.remove(channel);
        }
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        connector.removeConnectionPoint(inetSocketAddress);
    }

    public void removeSubscription(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.removeSubscription(channel, destination);
    }

    public void replaceConnectionList(List<InetSocketAddress> parseAddressAndPortList) {
        connector.replaceConnectionList(parseAddressAndPortList);
    }

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        sent++;
        connector.send(message);
    }

    //    public void sendDirectWithNoQueue(LoggingMessage message) throws LoggingMessageSenderException {
    //        connector.sendDirectWithNoQueue(message);
    //    }

    /**
     * Has a real issue if two people call it at the same time, no way to tell which is which. The outcome on the hub is idempotent though, so it doesn't matter that much.
     *
     * @param subscriptionRequestMessage
     * @throws LoggingMessageSenderException
     */
    public SubscriptionResponseMessage sendBlocking(SubscriptionRequestMessage subscriptionRequestMessage) throws LoggingMessageSenderException, TimeoutException, InterruptedException {
        BlockingMessageFilter filter = new BlockingMessageFilter() {
            @Override public boolean passes(LoggingMessage loggingMessage) {
                return loggingMessage instanceof SubscriptionResponseMessage;
            }
        };

        SubscriptionResponseMessage responseMessage = (SubscriptionResponseMessage) sendBlocking(filter, subscriptionRequestMessage);
        return responseMessage;
    }

    public RequestResponseMessage sendBlocking(RequestResponseMessage message) throws LoggingMessageSenderException, TimeoutException, InterruptedException {
        final int correlationID = getNextCorrelationID();
        message.setCorrelationID(correlationID);

        BlockingMessageFilter filter = new BlockingMessageFilter() {
            @Override public boolean passes(LoggingMessage message) {
                return (message instanceof RequestResponseMessage && correlationID == ((RequestResponseMessage) message).getCorrelationID());
            }
        };

        RequestResponseMessage response = (RequestResponseMessage) sendBlocking(filter, message);
        return response;
    }

    private LoggingMessage sendBlocking(BlockingMessageFilter filter, LoggingMessage message) throws LoggingMessageSenderException, TimeoutException, InterruptedException {
        sent++;
        addLoggingMessageListener(filter);

        try {
            connector.send(message);
            LoggingMessage response = filter.await();
            return response;
        } finally {
            removeLoggingMessageListener(filter);
        }

    }

    public void sendHistoricalIndexRequest(long from, long to) {

        logger.info("Sending history index request");

        try {
            send(new HistoricalIndexRequest(from, to));
        } catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send log event message", e);
        }
    }

    public void sendWhenConnected(LoggingMessage message) {
        connector.sendWhenConnected(message);
    }

    public void setAutoGlobalSubscription(boolean autoGlobalSubscription) {
        this.autoGlobalSubscription = autoGlobalSubscription;
    }

    public boolean isDebug() {
        return connector.isDebug();
    }

    public void setDebug(boolean b) {
        connector.setDebug(b);
    }

    public void setForceFlush(boolean forceFlush) {
        connector.setForceFlush(forceFlush);
    }

    public void setLevelFilter(int levelFilter) throws LoggingMessageSenderException {
        this.levelFilter = levelFilter;
        sendLevelFilter();
    }

    public void setWriteQueueMaximumSize(int writeQueueMaximumSize) {
        connector.setWriteQueueMaximumSize(writeQueueMaximumSize);
    }

    public void setWriteQueueOverflowPolicy(SocketConnection.SlowSendingPolicy policy) {
        connector.setWriteQueueOverflowPolicy(policy);
    }

    public void subscribe() throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof SubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        SubscriptionRequestMessage subscriptionMessage = new SubscriptionRequestMessage();
        send(subscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            } else {
                throw new LoggingMessageSenderException("Subscribe request failed; timed out waiting for the response message");
            }
        } catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void subscribe(String... channels) throws LoggingMessageSenderException {
        // TODO : multiple channel subscriptions
        for (String channel : channels) {
            subscribe(channel);
        }
    }

    public void subscribe(String channel) throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof EventSubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        EventSubscriptionRequestMessage subscriptionMessage = new EventSubscriptionRequestMessage(getNextCorrelationID(), true, channel);
        send(subscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            } else {
                throw new LoggingMessageSenderException("Channel subscribe request failed; timed out waiting for the response message");
            }
        } catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Channel subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void subscribe(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.addSubscription(channel, destination);
    }

    public void subscribe(Destination<ChannelMessage> destination, String... channels) {
        for (String channel : channels) {
            // TODO : handle bulk subscriptions all the way through the subscription stack
            subscriptionController.addSubscription(channel, destination);
        }
    }

    // public void subscribeToAggregatedPatternEvents(AggregationKey... keys) {
    // sendWhenConnected(new AggregatedPatternDataSubscriptionRequestMessage(true, keys));
    // }

    public void unsubscribe() throws LoggingMessageSenderException {
        final CountDownLatch latch = new CountDownLatch(1);
        addLoggingMessageListener(new LoggingMessageListener() {
            public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof UnsubscriptionResponseMessage) {
                    latch.countDown();
                }
            }
        });

        UnsubscriptionRequestMessage unsubscriptionMessage = new UnsubscriptionRequestMessage();
        send(unsubscriptionMessage);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                // Happy
            } else {
                throw new LoggingMessageSenderException("Subscribe request failed; timed out waiting for the response message");
            }
        } catch (InterruptedException e) {
            throw new LoggingMessageSenderException("Subscribe request may have failed; the thread was interupted waiting for the response message");
        }
    }

    public void unsubscribe(String channel, Destination<ChannelMessage> destination) {
        subscriptionController.removeSubscription(channel, destination);
    }

    // public void unsubscribeToAggregatedPatternEvents(final AggregationKey... keys) {
    // // TODO : test me, not sure this works, and its different from the subscribe path (ie
    // // blocking)
    // final CountDownLatch latch = new CountDownLatch(1);
    // addLoggingMessageListener(new LoggingMessageListener() {
    // public void onNewLoggingMessage(LoggingMessage message) {
    // if (message instanceof AggregatedPatternDataSubscriptionResponseMessage) {
    // latch.countDown();
    // }
    // }
    // });
    //
    // try {
    // connector.send(new AggregatedPatternDataSubscriptionRequestMessage(false, keys));
    //
    // try {
    // if (latch.await(10, TimeUnit.SECONDS)) {
    // // Happy
    // }
    // else {
    // throw new
    // LoggingMessageSenderException("AggregatedPatternDataSubscription request failed; timed out waiting for the response message");
    // }
    // }
    // catch (InterruptedException e) {
    // throw new
    // LoggingMessageSenderException("AggregatedPatternDataSubscription request may have failed; the thread was interupted waiting for the response message");
    // }
    // }
    // catch (LoggingMessageSenderException e) {
    // throw new
    // FormattedRuntimeException("Failed to send AggregatedPatternDataSubscription message", e);
    // }
    //
    // }

    private void sendLevelFilter() throws LoggingMessageSenderException {
        FilterRequestMessage filterMessage = new FilterRequestMessage(levelFilter);
        send(filterMessage);
    }

    protected void handleMessage(LoggingMessage message) {
        logger.fine("[{}] New message received by the socket client '{}'", getName(), message);

        fireNewMessage(message);

        if (message instanceof ChannelMessage) {
            ChannelMessage channelMessage = (ChannelMessage) message;
            subscriptionController.dispatch(channelMessage.getChannel(), channelMessage, null);
        }

        if (message instanceof RequestResponseMessage) {
            RequestResponseMessage requestResponseMessage = (RequestResponseMessage) message;
            int requestID = requestResponseMessage.getCorrelationID();
            Handler<LoggingMessage> handler = requestResponseHandlers.get(requestID);
            if (handler != null) {
                if (handler.handle(message)) {
                    requestResponseHandlers.remove(requestID);
                }
            }
        }
    }

    protected void reestablishSubscriptions() {
        Set<String> channels = subscriptionController.getChannels();
        if (channels.size() > 0) {
            String[] channelsArray = channels.toArray(new String[channels.size()]);
            ChannelSubscriptionRequestMessage subscriptionMessage = new ChannelSubscriptionRequestMessage(getNextCorrelationID(), true, channelsArray);
            try {
                send(subscriptionMessage);
            } catch (LoggingMessageSenderException e) {
                exceptionPolicy.handle(e);
            }
        }

    }

    private Future<Boolean> handleFirstSubscriptionInternal(String channel) {
        final LatchFuture<Boolean> future = new LatchFuture<Boolean>();

        final ChannelSubscriptionRequestMessage request = new ChannelSubscriptionRequestMessage(getNextCorrelationID(), true, channel);

        requestResponseHandlers.put(request.getCorrelationID(), new Handler<LoggingMessage>() {
            public boolean handle(LoggingMessage t) {
                boolean handled = false;
                if (t instanceof ChannelSubscriptionResponseMessage) {
                    ChannelSubscriptionResponseMessage response = (ChannelSubscriptionResponseMessage) t;
                    if (response.getCorrelationID() == request.getCorrelationID()) {
                        future.trigger(response.isSuccess());
                        handled = true;
                    }

                }
                return handled;
            }
        });

        // Only send the message if we are connected - otherwise the subscription will be created
        // when we connect
        if (isConnected()) {
            try {
                send(request);
            } catch (LoggingMessageSenderException e) {
                // Probably an async disconnection - not to worry as the subscription will get
                // re-created automatically when we reconnect
            }
        } else {
            // Not to worry as the subscription will get created automatically when we reconnect
        }

        return future;
    }

    private Future<Boolean> handleLastSubscriptionInternal(String channel) {
        try {
            send(new ChannelSubscriptionRequestMessage(getNextCorrelationID(), false, channel));
        } catch (final LoggingMessageSenderException e) {
            throw new FormattedRuntimeException("Failed to send message", e);
        }

        // TODO : implement unsubscribe futures
        return null;
    }

    public void send(ChannelMessage message) throws LoggingMessageSenderException {
        connector.send(message);
    }

    public String getName() {
        return connector.getName();

    }

    public void setName(String name) {
        connector.setName(name);
    }

    public int getSent() {
        return sent;
    }

    public HistoricalDataAPI getHistoricalDataAPI() {
        return new HistoricalDataAPI() {
            public void streamHistoricalPatternisedEvents(long fromTime, long toTime, final StreamingDestination<PatternisedLogEvent> destination) {

                // Construct the history data request message to send to the hub
                final HistoricalPatternisedDataRequest request = new HistoricalPatternisedDataRequest();
                request.setCorrelationID(getNextCorrelationID());

                // We'll request the last 5 minutes of data
                //                long now = System.currentTimeMillis();
                //                request.setStart(TimeUtils.before(now, "5 minutes"));
                //                request.setEnd(now);
                request.setStart(fromTime);
                request.setEnd(toTime);

                // The request will execute asynchronously in one or more batch updates, so if we
                // want to
                // wait for the response to complete we'll need to coordinate between the threads
                final CountDownLatch latch = new CountDownLatch(1);

                // We can use some counters to track the process of the request
                final MutableInt batches = new MutableInt(0);
                final MutableInt count = new MutableInt(0);

                // We need to bind to the message receiver to pick out the HistoricalDataResponse
                // messages
                LoggingMessageListener listener = new LoggingMessageListener() {

                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof HistoricalPatternisedDataResponse) {
                            HistoricalPatternisedDataResponse response = (HistoricalPatternisedDataResponse) message;
                            if (response.getCorrelationID() == request.getCorrelationID()) {

                                PatternisedLogEvent[] events = response.getEvents();

                                for (PatternisedLogEvent defaultLogEvent : events) {
                                    // This is where you add your code to consume the historical
                                    // events
                                    destination.send(defaultLogEvent);
                                }

                                count.value += events.length;
                                batches.value++;

                                // The isLastBatch field indidicates when all of the data has been
                                // received
                                if (!response.isLastBatch()) {
                                    // append("=== more to follow ===");
                                } else {
                                    // append("======================");

                                    // This is the final batch, so notify the main thread we are
                                    // done
                                    latch.countDown();
                                }
                            }
                        }

                    }
                };

                addLoggingMessageListener(listener);

                // Send the request - note that we always register the listener _before_ sending the
                // request
                // to avoid race conditions.
                try {
                    send(request);
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }

                // Block the main thread and wait for the response to arrive
                boolean done = false;
                int lastBatches = 0;
                while (!done) {
                    try {
                        if (latch.await(5, TimeUnit.SECONDS)) {
                            done = true;
                            destination.onStreamComplete();
                        }else{
                            if(batches.value == lastBatches) {
                                throw new RuntimeException(StringUtils.format("Timeout reached waiting for another batch - we've received {} batches so far.", batches.value));
                            }
                        }
                        lastBatches = batches.value;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        removeLoggingMessageListener(listener);
                    }
                }

            }

            @Override public void streamHistoricalAggregatedEvents(long fromTime, long toTime, final StreamingDestination<AggregatedLogEvent> destination) {
                // Construct the history data request message to send to the hub
                final HistoricalAggregatedDataRequest request = new HistoricalAggregatedDataRequest();
                request.setCorrelationID(getNextCorrelationID());

                //                // We'll request the last 5 minutes of data
                //                long now = System.currentTimeMillis();
                //                request.setStart(TimeUtils.before(now, "5 minutes"));
                //                request.setEnd(now);
                request.setStart(fromTime);
                request.setEnd(toTime);

                // The request will execute asynchronously in one or more batch updates, so if we
                // want to
                // wait for the response to complete we'll need to coordinate between the threads
                final CountDownLatch latch = new CountDownLatch(1);

                // We can use some counters to track the process of the request
                final MutableInt batches = new MutableInt(0);
                final MutableInt count = new MutableInt(0);

                // We need to bind to the message receiver to pick out the HistoricalDataResponse
                // messages
                LoggingMessageListener listener = new LoggingMessageListener() {

                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof HistoricalAggregatedDataResponse) {
                            HistoricalAggregatedDataResponse response = (HistoricalAggregatedDataResponse) message;
                            if (response.getCorrelationID() == request.getCorrelationID()) {

                                AggregatedLogEvent[] events = response.getEvents();

                                for (AggregatedLogEvent defaultLogEvent : events) {
                                    // This is where you add your code to consume the historical
                                    // events
                                    destination.send(defaultLogEvent);
                                }

                                count.value += events.length;
                                batches.value++;

                                // The isLastBatch field indidicates when all of the data has been
                                // received
                                if (!response.isLastBatch()) {
                                    // append("=== more to follow ===");
                                } else {
                                    // append("======================");

                                    // This is the final batch, so notify the main thread we are
                                    // done
                                    latch.countDown();
                                }
                            }
                        }

                    }
                };

                addLoggingMessageListener(listener);

                // Send the request - note that we always register the listener _before_ sending the
                // request
                // to avoid race conditions.
                try {
                    send(request);
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }

                // Block the main thread and wait for the response to arrive
                boolean done = false;
                int lastBatches = 0;
                while (!done) {
                    try {
                        if (latch.await(5, TimeUnit.SECONDS)) {
                            done = true;
                            destination.onStreamComplete();
                        }else{
                            if(batches.value == lastBatches) {
                                throw new RuntimeException(StringUtils.format("Timeout reached waiting for another batch - we've received {} batches so far.", batches.value));
                            }
                        }
                        lastBatches = batches.value;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        removeLoggingMessageListener(listener);
                    }
                }
                // append("Received {} events in {} batches", count, batches);
            }
        };
    }

    public PatternManagementAPI getPatternManagementAPI() {
        return new PatternManagementAPI() {

            @Override public Result<Pattern> createPattern(Pattern template) {
                final Result<Pattern> result = new Result<Pattern>();

                createPattern(template, new ResultListener<Pattern>() {
                    @Override public void onUnsuccessful(String reason) {
                        result.unsuccessful(reason);
                    }

                    @Override public void onTimedout() {
                        result.timedOut();
                    }

                    @Override public void onSuccessful(Pattern pattern) {
                        result.success(pattern);
                    }

                    @Override public void onFailed(Throwable t) {
                        result.failed(t.getMessage());
                    }
                });

                return result;
            }

            @Override public void getPatterns(ResultListener<List<Pattern>> listener) {

                PatternListRequest request = new PatternListRequest();
                ChannelMessage message = new ChannelMessage(Channels.patternListRequests, request);
                final int requestID = getNextCorrelationID();
                message.setCorrelationID(requestID);

                final Exchanger<PatternListResponse> exchanger = new Exchanger<PatternListResponse>();

                LoggingMessageListener messageListener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof ResponseMessage) {
                            ResponseMessage channelResponseMessage = (ResponseMessage) message;

                            if (channelResponseMessage.getCorrelationID() == requestID) {
                                PatternListResponse response = (PatternListResponse) channelResponseMessage.getPayload();
                                try {
                                    exchanger.exchange(response, 1, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    // TODO : what to do here?
                                    logger.warning(e);
                                } catch (TimeoutException e) {
                                    // TODO : what to do here? Did the other thread die?!
                                    logger.warning(e);
                                }

                            }
                        }

                    }
                };

                connector.addLoggingMessageListener(messageListener);

                try {
                    connector.send(message);
                } catch (LoggingMessageSenderException e) {
                    listener.onFailed(e);
                }

                try {
                    PatternListResponse response = exchanger.exchange(null, Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

                    if (response.wasSuccessful()) {
                        listener.onSuccessful(response.getPatterns());
                    } else {
                        // TODO : implement failures
                        listener.onUnsuccessful(response.getUnsuccessfulReason());
                    }

                } catch (InterruptedException e) {
                    listener.onFailed(e);
                } catch (TimeoutException e) {
                    listener.onTimedout();
                }

                connector.removeLoggingMessageListener(messageListener);
            }

            @Override public Result<List<Pattern>> getPatterns() {

                final Result<List<Pattern>> result = new Result<List<Pattern>>();

                getPatterns(new ResultListener<List<Pattern>>() {
                    @Override public void onUnsuccessful(String reason) {
                        result.unsuccessful(reason);
                    }

                    @Override public void onTimedout() {
                        result.timedOut();
                    }

                    @Override public void onSuccessful(List<Pattern> patternList) {
                        result.success(patternList);
                    }

                    @Override public void onFailed(Throwable t) {
                        result.failed(t.getMessage());
                    }
                });

                return result;

            }

            @Override public void createPattern(Pattern template, ResultListener<Pattern> listener) {
                MapMessage map = new MapMessage();
                map.put("action", "createPattern");
                map.put("name", template.getName());
                map.put("pattern", template.getPattern());

                ChannelMessage message = new ChannelMessage(Channels.patternListRequests, map);
                final int requestID = getNextCorrelationID();
                message.setCorrelationID(requestID);

                final Exchanger<ResponseMessage> exchanger = new Exchanger<ResponseMessage>();

                LoggingMessageListener messageListener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof ResponseMessage) {
                            ResponseMessage channelResponseMessage = (ResponseMessage) message;

                            if (channelResponseMessage.getCorrelationID() == requestID) {
                                MapMessage response = (MapMessage) channelResponseMessage.getPayload();
                                try {
                                    exchanger.exchange(channelResponseMessage, 1, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    // TODO : what to do here?
                                    logger.warning(e);
                                } catch (TimeoutException e) {
                                    // TODO : what to do here? Did the other thread die?!
                                    logger.warning(e);
                                }
                            }
                        }
                    }
                };

                connector.addLoggingMessageListener(messageListener);

                try {
                    connector.send(message);
                } catch (LoggingMessageSenderException e) {
                    listener.onFailed(e);
                }

                try {
                    ResponseMessage response = exchanger.exchange(null, Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

                    if (response.wasSuccessful()) {

                        MapMessage resultMap = (MapMessage) response.getPayload();
                        // TODO : shouldn't we just update the template?
                        Pattern pattern = new Pattern();
                        pattern.setPattern(resultMap.get("pattern"));
                        pattern.setName(resultMap.get("name"));
                        pattern.setPatternId(resultMap.getInt("patternID"));
                        template.setPatternId(pattern.getPatternId());

                        listener.onSuccessful(pattern);
                    } else {
                        if (response.wasFailure()) {
                            listener.onFailed(new RuntimeException(response.getFailureReason()));
                        } else {
                            listener.onUnsuccessful(response.getUnsuccessfulReason());
                        }
                    }

                } catch (InterruptedException e) {
                    listener.onFailed(e);
                } catch (TimeoutException e) {
                    listener.onTimedout();
                }

                connector.removeLoggingMessageListener(messageListener);
            }

            @Override public void getAggregations(ResultListener<List<Aggregation>> listener) {
                AggregationListRequest request = new AggregationListRequest();
                ChannelMessage message = new ChannelMessage(Channels.aggregationListRequests, request);
                final int requestID = getNextCorrelationID();
                message.setCorrelationID(requestID);

                final Exchanger<AggregationListResponse> exchanger = new Exchanger<AggregationListResponse>();

                LoggingMessageListener messageListener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof ResponseMessage) {
                            ResponseMessage channelResponseMessage = (ResponseMessage) message;

                            if (channelResponseMessage.getCorrelationID() == requestID) {
                                AggregationListResponse response = (AggregationListResponse) channelResponseMessage.getPayload();
                                try {
                                    exchanger.exchange(response, 1, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    // TODO : what to do here?
                                    logger.warning(e);
                                } catch (TimeoutException e) {
                                    // TODO : what to do here? Did the other thread die?!
                                    logger.warning(e);
                                }

                            }
                        }

                    }
                };

                connector.addLoggingMessageListener(messageListener);

                try {
                    connector.send(message);
                } catch (LoggingMessageSenderException e) {
                    listener.onFailed(e);
                }

                try {
                    AggregationListResponse response = exchanger.exchange(null, Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

                    if (response.wasSuccessful()) {
                        listener.onSuccessful(response.getAggregations());
                    } else {
                        // TODO : implement failures
                        listener.onUnsuccessful(response.getUnsuccessfulReason());
                    }

                } catch (InterruptedException e) {
                    listener.onFailed(e);
                } catch (TimeoutException e) {
                    listener.onTimedout();
                }

                connector.removeLoggingMessageListener(messageListener);
            }

            @Override public Result<List<Aggregation>> getAggregations() {
                final Result<List<Aggregation>> result = new Result<List<Aggregation>>();

                getAggregations(new ResultListener<List<Aggregation>>() {
                    @Override public void onUnsuccessful(String reason) {
                        result.unsuccessful(reason);
                    }

                    @Override public void onTimedout() {
                        result.timedOut();
                    }

                    @Override public void onSuccessful(List<Aggregation> patternList) {
                        result.success(patternList);
                    }

                    @Override public void onFailed(Throwable t) {
                        result.failed(t.getMessage());
                    }
                });

                return result;

            }

            @Override public Result<Aggregation> createAggregation(Aggregation template) {
                final Result<Aggregation> result = new Result<Aggregation>();

                createAggregation(template, new ResultListener<Aggregation>() {
                    @Override public void onUnsuccessful(String reason) {
                        result.unsuccessful(reason);
                    }

                    @Override public void onTimedout() {
                        result.timedOut();
                    }

                    @Override public void onSuccessful(Aggregation pattern) {
                        result.success(pattern);
                    }

                    @Override public void onFailed(Throwable t) {
                        result.failed(t.getMessage());
                    }
                });

                return result;

            }

            @Override public void createAggregation(Aggregation template, ResultListener<Aggregation> listener) {
                MapMessage map = new MapMessage();
                map.put("action", "createAggregation");
                map.put("patternID", template.getPatternID());
                map.put("captureLabelIndex", template.getCaptureLabelIndex());
                map.put("groupBy", template.getGroupBy());
                map.put("interval", template.getInterval());
                map.put("type", template.getType());

                ChannelMessage message = new ChannelMessage(Channels.aggregationListRequests, map);
                final int requestID = getNextCorrelationID();
                message.setCorrelationID(requestID);

                final Exchanger<ResponseMessage> exchanger = new Exchanger<ResponseMessage>();

                LoggingMessageListener messageListener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {

                        if (message instanceof ResponseMessage) {
                            ResponseMessage channelResponseMessage = (ResponseMessage) message;

                            if (channelResponseMessage.getCorrelationID() == requestID) {
                                MapMessage response = (MapMessage) channelResponseMessage.getPayload();
                                try {
                                    exchanger.exchange(channelResponseMessage, 1, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    // TODO : what to do here?
                                    logger.warning(e);
                                } catch (TimeoutException e) {
                                    // TODO : what to do here? Did the other thread die?!
                                    logger.warning(e);
                                }
                            }
                        }
                    }
                };

                connector.addLoggingMessageListener(messageListener);

                try {
                    connector.send(message);
                } catch (LoggingMessageSenderException e) {
                    listener.onFailed(e);
                }

                try {
                    ResponseMessage response = exchanger.exchange(null, Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

                    if (response.wasSuccessful()) {

                        MapMessage resultMap = (MapMessage) response.getPayload();
                        Aggregation aggregation = new Aggregation();

                        aggregation.setAggregationID(resultMap.getInt("aggregationID"));
                        aggregation.setCaptureLabelIndex(resultMap.getInt("captureLabelIndex"));
                        aggregation.setGroupBy(resultMap.get("groupBy"));
                        aggregation.setInterval(resultMap.getLong("interval"));
                        aggregation.setPatternID(resultMap.getInt("patternID"));
                        aggregation.setType(AggregationType.valueOf(resultMap.get("type")));

                        listener.onSuccessful(aggregation);
                    } else {
                        if (response.wasFailure()) {
                            listener.onFailed(new RuntimeException(response.getFailureReason()));
                        } else {
                            listener.onUnsuccessful(response.getUnsuccessfulReason());
                        }
                    }

                } catch (InterruptedException e) {
                    listener.onFailed(e);
                } catch (TimeoutException e) {
                    listener.onTimedout();
                }

                connector.removeLoggingMessageListener(messageListener);
            }
        };
    }

    public LevelSettingAPI getLevelSettingAPI() {
        return new LevelSettingAPI() {

            @Override public void setLevels(InstanceFilter filter, LevelSettingsGroup settings, final MultipleResultListener<LevelSettingsConfirmation> listener) {

                LevelSettingsRequest request = new LevelSettingsRequest();
                request.setFilter(filter);
                request.setLevelSettings(settings);

                ChannelMessage channelMessage = new ChannelMessage(Channels.levelSetting, request);
                final int correlationID = getNextCorrelationID();
                channelMessage.setCorrelationID(correlationID);
                channelMessage.setReplyToChannel(Channels.getPrivateConnectionChannel(connector.getConnectionID()));

                final Filter<LoggingMessage> responseFilter = new Filter<LoggingMessage>() {
                    @Override public boolean passes(LoggingMessage t) {
                        boolean passes = false;
                        if (t instanceof ChannelMessage) {
                            ChannelMessage response = (ChannelMessage) t;
                            if (response.getCorrelationID() == correlationID) {
                                passes = true;
                            }
                        }
                        return passes;
                    }
                };

                // TODO : figure out a way to remove these transient listeners over time?
                addLoggingMessageListener(new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {
                        if (responseFilter.passes(message)) {
                            ChannelMessage channelMessage = (ChannelMessage) message;
                            Result<LevelSettingsConfirmation> payload = (Result<LevelSettingsConfirmation>) channelMessage.getPayload();
                            listener.onResult(payload);
                        }
                    }
                });

                try {
                    connector.send(channelMessage);
                } catch (LoggingMessageSenderException e) {
                    // TODO : really?
                    e.printStackTrace();
                }
            }
        };
    }

    public InstanceManagementAPI getInstanceManagementAPI() {
        return new InstanceManagementAPI() {

            @Override public void sendPing() {
                ChannelMessage request = new ChannelMessage(Channels.pingRequests, new PingRequest());
                request.setCorrelationID(getNextCorrelationID());
                request.setReplyToChannel(Channels.getPrivateConnectionChannel(connector.getConnectionID()));
                try {
                    connector.send(request);
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }

            @Override public void addPingListener(final Destination<PingResponse> destination) {
                LoggingMessageListener listener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {
                        if (message instanceof ChannelMessage) {
                            ChannelMessage channelRequestMessage = (ChannelMessage) message;
                            SerialisableObject payload = channelRequestMessage.getPayload();

                            if (payload instanceof PingResponse) {
                                PingResponse response = (PingResponse) payload;
                                destination.send(response);
                            }
                        }

                        if (message instanceof PingResponse) {
                            PingResponse response = (PingResponse) message;
                            destination.send(response);
                        }
                    }
                };
                connector.addLoggingMessageListener(listener);

            }
        };
    }

    public void setRespondToPings(boolean respondToPings) {
        this.respondToPings = respondToPings;
    }

    public boolean willRespondToPings() {
        return this.respondToPings;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public InstanceDetails getInstanceDetails() {

        InstanceDetails instanceDetails = new InstanceDetails();
        if (connector.getCurrentConnection() != null && connector.getCurrentConnection().getSocket() != null) {
            instanceDetails.setLocalPort(connector.getCurrentConnection().getSocket().getLocalPort());
            instanceDetails.setHostIP(connector.getCurrentConnection().getSocket().getLocalAddress().toString());
            if (instanceDetails.getHostIP().startsWith("/")) {
                instanceDetails.setHostIP(instanceDetails.getHostIP().substring(1));
            }
        }
        instanceDetails.setHostname(NetUtils.getLocalHostname());
        instanceDetails.setInstanceName(SocketClient.this.connector.getName());
        instanceDetails.setPid(pid);

        return instanceDetails;

    }

    public int getConnectionID() {
        return connector.getConnectionID();

    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }


}
