package com.logginghub.logging.servers;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.hub.configuration.SocketHubConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ConnectedMessage;
import com.logginghub.logging.messages.ConnectionTypeMessage;
import com.logginghub.logging.messages.EventSubscriptionRequestMessage;
import com.logginghub.logging.messages.EventSubscriptionResponseMessage;
import com.logginghub.logging.messages.FilterRequestMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.ResponseMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.SubscriptionResponseMessage;
import com.logginghub.logging.messages.UnsubscriptionRequestMessage;
import com.logginghub.logging.messages.UnsubscriptionResponseMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.Is;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemErrExceptionHandler;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.UDPListener;
import com.logginghub.utils.logging.UDPListener.PatternisedUDPData;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * A full hub implementation based on socket IO.
 *
 * @author James
 */
@Provides({LogEvent.class, LoggingMessageSender.class}) public class SocketHub implements ServerSocketConnectorListener,
                                                                                          Closeable,
                                                                                          Module<SocketHubConfiguration>,
                                                                                          Source<LogEvent>,
                                                                                          Destination<LogEvent>,
                                                                                          LoggingMessageSender,
                                                                                          SocketHubInterface {

    private static final Logger logger = Logger.getLoggerFor(SocketHub.class);
    private List<ServerSocketConnectorListener> connectionListeners = new CopyOnWriteArrayList<ServerSocketConnectorListener>();
    private TimeProvider timeProvider = new SystemTimeProvider();
    private List<Closeable> closables = new ArrayList<Closeable>();
    private FilterHelper excludeFilter = new FilterHelper();
    private AtomicInteger nextConnectionID = new AtomicInteger();
    private Multiplexer<LogEvent> localListeners = new Multiplexer<LogEvent>();
    private Multiplexer<LoggingMessage> messageListeners = new Multiplexer<LoggingMessage>();
    private ChannelMultiplexer channelMultiplexer = new ChannelMultiplexer();
    private List<SocketConnectionInterface> connectionsList = new CopyOnWriteArrayList<SocketConnectionInterface>();
    private boolean forceHubTime = Boolean.getBoolean("socketHub.forceHubTime");
    private int lastConnections = -1;
    private int lastSubscriptions = -1;
    private int listeningPort = Integer.getInteger("socketHub.port", VLPorts.getSocketHubDefaultPort());
    private String statsInterval = "1 minute";
    private int maximumClientSendQueueSize = SocketConnection.writeBufferDefaultSize;
    private int messagesIn;
    private int messagesFilteredOut;

    private IntegerStat messagesOut = new IntegerStat("messages out", 0);

    private ServerSocketConnector serverSocketConnector;
    private List<FilteredMessageSender> subscribedConnections = new CopyOnWriteArrayList<FilteredMessageSender>();

    private UDPListener patternisedUDPListener;

    private boolean useUDPListeners = Boolean.getBoolean("socketHub.useUDPListeners");
    private int restfulListenerPort = Integer.getInteger("socketHub.restfulListenerPort", 0);
    private WorkerThread timer;

    private Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
    private SocketHubConfiguration configuration;
    private FactoryMap<Class<? extends LoggingMessage>, List<SocketHubMessageHandler>> messageHandlers = new FactoryMap<Class<? extends LoggingMessage>, List<SocketHubMessageHandler>>() {
        @Override protected List<SocketHubMessageHandler> createEmptyValue(Class<? extends LoggingMessage> key) {
            return new CopyOnWriteArrayList<SocketHubMessageHandler>();
        }
    };
    private String name = "Hub";

    public SocketHub() {
    }

    public static SocketHub createTestHub() {
        SocketHub hub = new SocketHub();
        hub.useRandomPort();
        return hub;
    }

    /**
     * If you have wired up external listeners to the hub, you can add them as closables if you'd like them to be closed
     * when the hub is stopped.
     *
     * @param closables
     */
    public void addCloseable(Closeable... closables) {
        for (Closeable closeable : closables) {
            this.closables.add(closeable);
        }
    }

    public void addAndSubscribeLocalListener(FilteredMessageSender logger) {
        Is.notNull(logger, "You can't add a null listener");
        subscribedConnections.add(logger);
    }

    @Override public void close() throws IOException {
        stop();
    }

    public InetSocketAddress getConnectionPoint() {
        return new InetSocketAddress("localhost", listeningPort);
    }

    public List<SocketConnectionInterface> getConnectionsList() {
        return connectionsList;
    }

    public int getPort() {

        if (serverSocketConnector != null) {
            return serverSocketConnector.getPort();
        } else {
            return listeningPort;
        }
    }

    // //////////////////////////////////////////////////////////////////
    // ServerSocketConnectorListener implementation
    // //////////////////////////////////////////////////////////////////

    public void setPort(int port) {
        listeningPort = port;
    }

    public List<FilteredMessageSender> getSubscribedConnections() {
        return subscribedConnections;
    }

    public void onConnectionClosed(SocketConnectionInterface connection, String reason) {
        connectionsList.remove(connection);
        subscribedConnections.remove(connection);

        // jshaw - we dont want to call this, as this is the notification that the connection is
        // already closed?!
        connection.stop();

        for (ServerSocketConnectorListener serverSocketConnectorListener : connectionListeners) {
            serverSocketConnectorListener.onConnectionClosed(connection, reason);
        }

        logger.info(String.format("Connection closed : %s : %s", connection, reason));

    }

    public void onNewConnection(SocketConnectionInterface connection) {

        int connectionID = nextConnectionID.getAndIncrement();
        connection.setConnectionID(connectionID);

        try {
            connection.send(new ConnectedMessage(connectionID));
        } catch (LoggingMessageSenderException e) {
            logger.warn(e,
                    "Failed to send connected message to connection '{}' - did they disconnect really quickly?",
                    connection);
        }

        connectionsList.add(connection);
        connection.setMessagesOutCounter(messagesOut);
        logger.info(String.format("New connection accepted : %s", connection));

        for (ServerSocketConnectorListener serverSocketConnectorListener : connectionListeners) {
            serverSocketConnectorListener.onNewConnection(connection);
        }
    }

    public void onNewMessage(LoggingMessage message, SocketConnectionInterface source) {
        logger.fine("[{}] Message {} received from {}", name, message, source);
        messageListeners.send(message);

        if (message instanceof SubscriptionRequestMessage) {
            logger.info("Subscribing {} to all events", source);
            subscribedConnections.add(source);

            try {
                source.send(new SubscriptionResponseMessage());
            } catch (LoggingMessageSenderException e) {
                throw new RuntimeException("Failed to send subscription response message back to connection " + source,
                        e);
            }
        } else if (message instanceof UnsubscriptionRequestMessage) {
            logger.info("Unsubscribing {} from all events", source);
            subscribedConnections.remove(source);

            try {
                source.send(new UnsubscriptionResponseMessage());
            } catch (LoggingMessageSenderException e) {
                throw new RuntimeException("Failed to send subscription response message back to connection " + source,
                        e);
            }
        } else if (message instanceof EventSubscriptionRequestMessage) {
            EventSubscriptionRequestMessage request = (EventSubscriptionRequestMessage) message;

            String[] channels = request.getChannels();
            if (request.isSubscribe()) {
                if (channels == null || channels.length == 0) {
                    logger.info("Subscribing {} to all events", source);
                    subscribedConnections.add(source);
                } else {
                    processChannelSubscription(source, channels);
                }

            } else {
                if (channels == null || channels.length == 0) {
                    logger.info("Unsubscribing {} from all events", source);
                    subscribedConnections.remove(source);
                } else {
                    processChannelUnsubscription(source, channels);
                }
            }

            try {
                source.send(new EventSubscriptionResponseMessage(request.getCorrelationID(),
                        request.isSubscribe(),
                        "",
                        true,
                        channels));
            } catch (LoggingMessageSenderException e) {
                logger.info(e, "Failed to send event subscription response  back to connection " + source);
            }
        } else if (message instanceof ConnectionTypeMessage) {
            ConnectionTypeMessage connectionTypeMessage = (ConnectionTypeMessage) message;
            logger.info("Setting connection type of '{}' to '{}'", source, connectionTypeMessage.getType());
            source.setConnectionType(connectionTypeMessage.getType());
            source.setConnectionDescription(connectionTypeMessage.getName());

            try {
                source.send(new ResponseMessage(connectionTypeMessage.getCorrelationID()));
            } catch (LoggingMessageSenderException e) {
                logger.info(e, "Failed to send connection type response back to connection " + source);
            }
        } else if (message instanceof LogEventMessage) {
            LogEventMessage logEventMessage = (LogEventMessage) message;
            processLogEvent(logEventMessage, source);
        } else if (message instanceof FilterRequestMessage) {
            FilterRequestMessage filterRequestMessage = (FilterRequestMessage) message;
            processFilterRequest(filterRequestMessage, source);
        }

        List<SocketHubMessageHandler> handlers = messageHandlers.getOnlyIfExists(message.getClass());
        if (handlers != null) {
            for (SocketHubMessageHandler socketHubMessageHandler : handlers) {
                socketHubMessageHandler.handle(message, source);
            }
        }

        for (ServerSocketConnectorListener serverSocketConnectorListener : connectionListeners) {
            serverSocketConnectorListener.onNewMessage(message, source);
        }
    }

    private void processChannelSubscription(SocketConnectionInterface source, String[] channels) {

        for (String channel : channels) {

            if (channel.endsWith("/")) {
                channel = channel.substring(0, channel.length() - 1);
            }

            channelMultiplexer.subscribe(channel, source);
        }
    }

    private void processChannelUnsubscription(SocketConnectionInterface source, String[] channels) {

        for (String channel : channels) {
            channelMultiplexer.unsubcribe(channel, source);
        }

    }

    private void processFilterRequest(FilterRequestMessage filterRequestMessage, SocketConnectionInterface source) {
        // Was thinking of putting this into a map, but then we'd have to look
        // it up for every message... might as well put it on the socket
        // connection as thats the one thing we've always got
        int levelFilter = filterRequestMessage.getLevelFilter();
        logger.info("Setting level filter on {} to {}", source, Level.parse("" + levelFilter));
        source.setLevelFilter(levelFilter);
    }

    //    public void processInternalLogEvent(LogEvent event) {
    //        forceHubTime(event);
    //
    //        LogEventMessage message = new LogEventMessage(event);
    //        processLogEvent(message, null);
    //    }

    // public void setTelemetryPort(int telemetryPort) {
    // this.telemetryPort = telemetryPort;
    // }

    public void processLogEvent(LogEventMessage message, SocketConnectionInterface source) {
        messagesIn++;

        LogEvent logEvent = message.getLogEvent();
        if (!excludeFilter.passes(logEvent)) {

            forceHubTime(logEvent);

            if (logEvent.getChannel() == null) {
                if (logEvent instanceof DefaultLogEvent) {
                    DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEvent;
                    defaultLogEvent.setChannel("events");
                }
            }

            boolean sent = false;

            for (final FilteredMessageSender connection : subscribedConnections) {
                if (connection != source) {
                    if (connection.getConnectionType() == SocketConnection.CONNECTION_TYPE_HUB_BRIDGE && source.getConnectionType() == SocketConnection.CONNECTION_TYPE_HUB_BRIDGE) {
                        logger.finest("[{}] not sending message back to bridge connection {} (from {})",
                                name,
                                connection,
                                source);
                    } else {
                        logger.finest("[{}] sending to {} (from {})", name, connection, source);
                        connection.send(logEvent);
                        sent = true;
                    }
                }
            }

            localListeners.send(logEvent);

            sent |= channelMultiplexer.send(logEvent, source);

            if (!sent) {
                logger.finest(
                        "[{}] We have no globally subscribed connections, or matching channel subscriptions; the log event is not being sent anywhere",
                        name);
            }

            logger.finer("[{}] LogEvent broadcast to {} subscribed connections", name, subscribedConnections.size());
        } else {
            messagesFilteredOut++;
            logger.finer("[{}] LogEvent was filtered out", name);
        }
    }

    public void setMaximumClientSendQueueSize(int maximumClientSendQueueSize) {
        this.maximumClientSendQueueSize = maximumClientSendQueueSize;
    }

    public void shutdown() {
        stopTimer();
        stop();
    }

    public void setRestfulListenerPort(int restfulListenerPort) {
        this.restfulListenerPort = restfulListenerPort;
    }

    public void start() {

        try {
            serverSocketConnector = new ServerSocketConnector(listeningPort, new SystemErrExceptionHandler());
        } catch (IOException e) {
            throw new FormattedRuntimeException("Failed to create the server socket connector", e);
        }

        serverSocketConnector.setMaximumClientSendQueueSize(maximumClientSendQueueSize);
        serverSocketConnector.addServerSocketConnectorListener(this);
        serverSocketConnector.start();
        logger.fine("Server socket connector started");

        if (useUDPListeners) {
            patternisedUDPListener = new UDPListener();
            patternisedUDPListener.getEventStream().addListener(new StreamListener<UDPListener.PatternisedUDPData>() {
                @Override public void onNewItem(PatternisedUDPData t) {
                    outputPatternisedEvent(t);
                }
            });
            patternisedUDPListener.start();
        }

        if (configuration == null || configuration.isOutputStats()) {
            startTimer();
        }
    }

    protected void outputPatternisedEvent(PatternisedUDPData data) {

        DefaultLogEvent event = new DefaultLogEvent();
        event.setPid(data.pid);
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(Logger.info);
        event.setSourceHost(data.host);
        event.setSourceAddress(data.hostIP);
        event.setSourceApplication(data.applicationName);
        event.setThreadName("");
        event.setSourceClassName("");
        event.setSourceMethodName("");

        event.setMessage(StringUtils.format("Pattern {} : {}", data.patternID, Arrays.toString(data.parameters)));

        LogEventMessage message = new LogEventMessage(event);
        processLogEvent(message, null);
    }

    public ServerSocketConnector getServerSocketConnector() {
        return serverSocketConnector;
    }

    public void stop() {
        if (serverSocketConnector != null) {
            serverSocketConnector.stop();
        }

        for (SocketConnectionInterface SocketConnectionInterface : connectionsList) {
            SocketConnectionInterface.close();
        }

        connectionsList.clear();

        logger.fine(
                "Server socket connector stopped, the server socket has been closed and all client connections have been closed");

        stopTimer();

        if (patternisedUDPListener != null) {
            patternisedUDPListener.close();
        }

        FileUtils.closeQuietly(closables);
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void useRandomPort() {
        listeningPort = NetUtils.findFreePort();

        if (restfulListenerPort > 0) {
            restfulListenerPort = NetUtils.findFreePort();
        }
    }

    private void forceHubTime(LogEvent event) {
        if (forceHubTime) {
            DefaultLogEvent defaultLogEvent = (DefaultLogEvent) event;
            defaultLogEvent.setLocalCreationTimeMillis(timeProvider.getTime());
        }
    }

    private void startTimer() {
        long interval = TimeUtils.parseInterval(statsInterval);
        timer = WorkerThread.everyNow("SocketHub-stats", interval, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                logStatus();
            }
        });
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    protected void logStatus() {

        if (this.connectionsList.size() == lastConnections &&
                this.subscribedConnections.size() == lastSubscriptions &&
                messagesIn == 0 &&
                messagesOut.getValue() == 0) {
            // Nothing worth logging
        } else {
            logger.info(String.format(
                    "Hub status : %d connections, %d subscriptions, %d filtered, %d messages in, %d messages out",
                    this.connectionsList.size(),
                    this.subscribedConnections.size(),
                    messagesFilteredOut,
                    messagesIn,
                    messagesOut.getDeltaValue()));

            messagesIn = 0;
            lastConnections = this.connectionsList.size();
            lastSubscriptions = this.subscribedConnections.size();
        }

    }

    @Override public void onBindFailure(ServerSocketConnector connector, IOException e) {
        if (throttler.isOkToFire()) {
            logger.warning(
                    "Socket hub has failed to bind to port {} : {} - we'll try to bind 5 times every second until it succeeds, but you'll only see this error message once every 10 seconds.",
                    connector.getPort(),
                    e.getMessage());
        }

        for (ServerSocketConnectorListener serverSocketConnectorListener : connectionListeners) {
            serverSocketConnectorListener.onBindFailure(connector, e);
        }
    }

    @Override public void onBound(ServerSocketConnector connector) {
        throttler.reset();
        logger.info(StringUtils.format("Socket hub has successfully bound to port {}", connector.getPort()));

        for (ServerSocketConnectorListener serverSocketConnectorListener : connectionListeners) {
            serverSocketConnectorListener.onBound(connector);
        }

    }

    public void waitUntilBound() {
        serverSocketConnector.waitUntilBound();
    }

    @Override public void configure(SocketHubConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        this.configuration = configuration;
        setPort(configuration.getPort());
        setRestfulListenerPort(configuration.getRestfulListenerPort());
        setMaximumClientSendQueueSize(configuration.getMaximumClientSendQueueSize());
        setStatsInterval(configuration.getStatsInterval());

        List<FilterConfiguration> filters = configuration.getFilters();
        for (FilterConfiguration filterConfiguration : filters) {
            excludeFilter.addFilter(filterConfiguration);
        }
    }

    @Override public void send(LogEvent event) {
        LogEventMessage message = new LogEventMessage(event);
        processLogEvent(message, null);
    }

    @Override public void addDestination(Destination<LogEvent> listener) {
        localListeners.addDestination(listener);
    }

    @Override public void removeDestination(Destination<LogEvent> listener) {
        localListeners.removeDestination(listener);
    }

    public List<Destination<LogEvent>> getChannelSubscribedConnections(String string) {
        return channelMultiplexer.getChannelSubscribedConnections(string);
    }

    public void addMessageDestination(Destination<LoggingMessage> listener) {
        messageListeners.addDestination(listener);
    }

    public void removeMessageDestination(Destination<LoggingMessage> listener) {
        messageListeners.removeDestination(listener);
    }

    public void send(LoggingMessage message) {
        onNewMessage(message, null);
    }

    @Override
    public void addMessageListener(Class<? extends LoggingMessage> messageType, SocketHubMessageHandler handler) {
        // TODO : if we used integer message types we could just put these in an array!
        messageHandlers.get(messageType).add(handler);
    }

    @Override
    public void removeMessageListener(Class<? extends LoggingMessage> messageType, SocketHubMessageHandler handler) {
        // TODO : if we used integer message types we could just put these in an array!
        messageHandlers.get(messageType).remove(handler);
    }

    public SocketClient createClient(String name) {
        SocketClient client = new SocketClient(name);
        client.addConnectionPoint(new InetSocketAddress(getPort()));
        return client;

    }

    @Override public void addConnectionListener(ServerSocketConnectorListener listener) {
        connectionListeners.add(listener);
    }

    @Override public void removeConnectionListener(ServerSocketConnectorListener listener) {
        connectionListeners.remove(listener);
    }

    public void disconnectAll() {
        logger.warn("Closing all connections");
        for (SocketConnectionInterface SocketConnectionInterface : connectionsList) {
            SocketConnectionInterface.close();
        }
    }

    public int getMessagesIn() {
        return messagesIn;
    }

    public IntegerStat getMessagesOut() {
        return messagesOut;
    }

    public void addFilter(FilterConfiguration filterConfiguration) {
        excludeFilter.addFilter(filterConfiguration);
    }

    public String getStatsInterval() {
        return statsInterval;
    }

    public void setStatsInterval(String statsInterval) {
        this.statsInterval = statsInterval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}