package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.modules.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.RequestResponseMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketClientManager.State;
import com.logginghub.logging.messaging.SocketClientManagerListener;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.NamedModule;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ServiceDiscovery;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class EnvironmentModule implements NamedModule<EnvironmentConfiguration>, EnvironmentNotificationService, EnvironmentMessagingService, SocketClientDirectAccessService {

    private static final Logger logger = Logger.getLoggerFor(EnvironmentModule.class);
    private List<EnvironmentNotificationListener> listeners = new CopyOnWriteArrayList<EnvironmentNotificationListener>();
    private List<SocketClientManager> socketClientManagers = new ArrayList<SocketClientManager>();

    private Set<HubConfiguration> notConnectedHubs = new HashSet<HubConfiguration>();
    private Set<HubConfiguration> connectedHubs = new HashSet<HubConfiguration>();
    private String name;
    private boolean environmentConnectionEstablished = false;
    private boolean subscribe = false;

    private List<HubConfiguration> hub = new ArrayList<HubConfiguration>();

    @Override public void configure(EnvironmentConfiguration configuration, ServiceDiscovery discovery) {

        name = configuration.getName();
        socketClientManagers.clear();

        List<HubConfiguration> hubs = configuration.getHubs();
        for (final HubConfiguration hubConfiguration : hubs) {

            SocketClient client = new SocketClient();
            client.setAutoGlobalSubscription(false);
            client.addConnectionPoint(new InetSocketAddress(hubConfiguration.getHost(), hubConfiguration.getPort()));

            SocketClientManager socketClientManager = new SocketClientManager(client);
            socketClientManager.addSocketClientManagerListener(new SocketClientManagerListener() {
                @Override public void onStateChanged(State fromState, State toState) {
                    handleStateChange(hubConfiguration, toState);
                }
            });
            socketClientManagers.add(socketClientManager);

            notConnectedHubs.add(hubConfiguration);
        }

    }

    public void initialise() {
        socketClientManagers.clear();

        for (final HubConfiguration hubConfiguration : hub) {

            SocketClient client = new SocketClient();
            client.setAutoGlobalSubscription(subscribe);
            client.addConnectionPoint(new InetSocketAddress(hubConfiguration.getHost(), hubConfiguration.getPort()));

            SocketClientManager socketClientManager = new SocketClientManager(client);
            socketClientManager.addSocketClientManagerListener(new SocketClientManagerListener() {
                @Override public void onStateChanged(State fromState, State toState) {
                    handleStateChange(hubConfiguration, toState);
                }
            });
            socketClientManagers.add(socketClientManager);

            notConnectedHubs.add(hubConfiguration);
        }

    }

    protected void handleStateChange(HubConfiguration hubConfiguration, State toState) {
        logger.info("Handling state change for hub '{}' - new state is '{}'", hubConfiguration, toState);
        if (toState == State.Connected) {

            notConnectedHubs.remove(hubConfiguration);
            connectedHubs.add(hubConfiguration);

            for (EnvironmentNotificationListener listener : listeners) {
                listener.onHubConnectionEstablished(hubConfiguration);
            }

            if (connectedHubs.size() == 1) {
                environmentConnectionEstablished = true;
                for (EnvironmentNotificationListener listener : listeners) {
                    listener.onEnvironmentConnectionEstablished();
                }
            }

            if (notConnectedHubs.isEmpty()) {
                for (EnvironmentNotificationListener listener : listeners) {
                    listener.onTotalEnvironmentConnectionEstablished();
                }
            }
        } else if (toState == State.NotConnected) {

            connectedHubs.remove(hubConfiguration);
            notConnectedHubs.add(hubConfiguration);

            for (EnvironmentNotificationListener listener : listeners) {
                listener.onHubConnectionLost(hubConfiguration);
            }

            if (notConnectedHubs.size() == 1) {
                for (EnvironmentNotificationListener listener : listeners) {
                    listener.onTotalEnvironmentConnectionLost();
                }
            }

            if (connectedHubs.isEmpty()) {
                environmentConnectionEstablished = false;
                for (EnvironmentNotificationListener listener : listeners) {
                    listener.onEnvironmentConnectionLost();
                }
            }

        }
    }

    @Override public void start() {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            logger.info("Starting socket client manager for environment '{}'", name);
            socketClientManager.start();
        }
    }

    @Override public void stop() {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            socketClientManager.stop();
        }
    }

    @Override public void addListener(EnvironmentNotificationListener environmentNotificationListener) {
        listeners.add(environmentNotificationListener);
    }

    @Override public void removeListener(EnvironmentNotificationListener environmentNotificationListener) {
        listeners.remove(environmentNotificationListener);
    }

    @Override public <T> void send(final RequestResponseMessage request, final Destination<LoggingMessage> destination) {
        // TODO : how to decide which hub to send it to?!
        for (SocketClientManager socketClientManager : socketClientManagers) {
            try {
                final SocketClient client = socketClientManager.getClient();
                request.setCorrelationID(client.getNextCorrelationID());

                LoggingMessageListener listener = new LoggingMessageListener() {
                    @Override public void onNewLoggingMessage(LoggingMessage message) {
                        if (message instanceof RequestResponseMessage) {
                            RequestResponseMessage requestResponseMessage = (RequestResponseMessage) message;
                            if (requestResponseMessage.getCorrelationID() == request.getCorrelationID()) {
                                destination.send(message);
                                client.removeLoggingMessageListener(this);
                            }
                        }
                    }
                };

                client.addLoggingMessageListener(listener);
                client.send(request);

            } catch (LoggingMessageSenderException e) {
                throw new FormattedRuntimeException(e, "Failed to send message");
            }
        }

    }

    @Override public String getName() {
        return name;
    }

    @Override public Object sendStreaming(final RequestResponseMessage request, final Destination<LoggingMessage> destination) {

        LoggingMessageListener listener = new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof RequestResponseMessage) {
                    RequestResponseMessage requestResponseMessage = (RequestResponseMessage) message;
                    if (requestResponseMessage.getCorrelationID() == request.getCorrelationID()) {
                        destination.send(message);
                    }
                }
            }
        };

        // TODO : how to decide which hub to send it to?!
        for (SocketClientManager socketClientManager : socketClientManagers) {
            try {
                final SocketClient client = socketClientManager.getClient();
                request.setCorrelationID(client.getNextCorrelationID());
                client.addLoggingMessageListener(listener);
                client.send(request);
            } catch (LoggingMessageSenderException e) {
                throw new FormattedRuntimeException(e, "Failed to send message");
            }
        }

        return listener;
    }

    @Override public void stopStreaming(Object streamingToken) {
        // TODO : we should really tell the other side to stop the stream?
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.removeLoggingMessageListener((LoggingMessageListener) streamingToken);
        }

    }

    @Override public Future<Boolean> subscribe(String channel, Destination<ChannelMessage> destination) {
        // TODO : how to decide which hub to send it to?!
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            Future<Boolean> addSubscription = client.addSubscription(channel, destination);
            return addSubscription;
        }

        // TODO : this whole thing is buggered if we have more than one hub connection!
        return null;
    }

    @Override public boolean isEnvironmentConnectionEstablished() {
        return environmentConnectionEstablished;

    }

    @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
        // TODO : we should really tell the other side to stop the stream?
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.send(message);
        }
    }

    public void addLogEventListener(LogEventListener logEventListener) {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.addLogEventListener(logEventListener);
        }
    }

    public void removeLogEventListener(LogEventListener logEventListener) {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.removeLogEventListener(logEventListener);
        }
    }

    @Override public void subscribeToPrivateChannel(Destination<ChannelMessage> destination) {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.subscribe(Channels.getPrivateConnectionChannel(client.getConnectionID()), destination);
        }
    }

    @Override public void unsubscribeFromPrivateChannel(Destination<ChannelMessage> destination) {
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            client.unsubscribe(Channels.getPrivateConnectionChannel(client.getConnectionID()), destination);
        }
    }

    @Override public int getConnectionID() {
        // TODO : this is going to backfire at some point
        return socketClientManagers.get(0).getClient().getConnectionID();
    }

    @Override public List<SocketClient> getDirectAccess() {
        List<SocketClient> clients = new ArrayList<SocketClient>();
        for (SocketClientManager socketClientManager : socketClientManagers) {
            final SocketClient client = socketClientManager.getClient();
            clients.add(client);
        }

        return clients;
    }

}
