package com.logginghub.logging.modules;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.ChannelSubscriptionRequestMessage;
import com.logginghub.logging.messages.ChannelSubscriptionResponseMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.messaging.SubscriptionController;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.logging.servers.ServerHandlerSubscriptions;
import com.logginghub.logging.servers.ServerMessageHandler;
import com.logginghub.logging.servers.ServerSocketConnector;
import com.logginghub.logging.servers.ServerSocketConnectorListener;
import com.logginghub.logging.servers.ServerSubscriptionsService;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.servers.SocketHubMessageHandler;
import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Provides(ChannelMessage.class) public class ChannelSubscriptionsModule implements Module<ChannelSubscriptionsConfiguration>,
                Destination<ChannelMessage>, ChannelMessagingService, ServerSubscriptionsService {

    private static final Logger logger = Logger.getLoggerFor(ChannelSubscriptionsModule.class);
    // private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.Log, logger);
    private ChannelSubscriptionsConfiguration configuration;
    private Map<Destination<ChannelMessage>, Destination<LoggingMessage>> internalCounterparts = new HashMap<Destination<ChannelMessage>, Destination<LoggingMessage>>();

    private ServerHandlerSubscriptions serverHandlerSubscriptions = new ServerHandlerSubscriptions();

    private SubscriptionController<Destination<LoggingMessage>, LoggingMessage> subscriptions = new SubscriptionController<Destination<LoggingMessage>, LoggingMessage>() {
        @Override protected Future<Boolean> handleLastSubscription(String channel) {
            return null;
        }

        @Override protected Future<Boolean> handleFirstSubscription(String channel) {
            return null;
        }
    };

    // private boolean debug;

    @Override public void configure(ChannelSubscriptionsConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);
        SocketHubMessageHandler handler = new SocketHubMessageHandler() {
            @Override public void handle(LoggingMessage message, SocketConnectionInterface source) {
                ChannelMessage channelMessage = (ChannelMessage) message;
                broadcast(channelMessage, source);
            }
        };

        socketHub.addMessageListener(ChannelMessage.class, handler);

        // TODO : convert these to Message Sender interfaces?
        socketHub.addMessageListener(ChannelSubscriptionRequestMessage.class, new SocketHubMessageHandler() {
            @Override public void handle(LoggingMessage message, final SocketConnectionInterface source) {
                ChannelSubscriptionRequestMessage subMessage = (ChannelSubscriptionRequestMessage) message;
                String[] channels = subMessage.getChannels();

                for (String channel : channels) {
                    if (subMessage.isSubscribe()) {
                        logger.fine("Subscribing '{}' to channel '{}'", source, channel);
                        subscriptions.addSubscription(channel, source.getMessageDestination());
                    }
                    else {
                        subscriptions.removeSubscription(channel, source.getMessageDestination());
                    }
                }

                try {
                    source.send(new ChannelSubscriptionResponseMessage(subMessage.getCorrelationID(), subMessage.isSubscribe(), "", true, channels));
                }
                catch (LoggingMessageSenderException e) {
                    throw new RuntimeException("Failed to send subscription response message back to connection " + source, e);
                }
            }
        });

        socketHub.addConnectionListener(new ServerSocketConnectorListener() {
            @Override public void onNewMessage(LoggingMessage message, SocketConnectionInterface source) {}

            @Override public void onNewConnection(SocketConnectionInterface connection) {
                String topic = Channels.getPrivateConnectionChannel(connection.getConnectionID()); 
                logger.fine("Creating connection subscription channel for '{}' for new connection {}", topic, connection);
                subscriptions.addSubscription(topic, connection.getMessageDestination());
            }

            @Override public void onConnectionClosed(SocketConnectionInterface connection, String reason) {
                logger.fine("Removing all subscriptions for '{}' ({})", connection, reason);
                subscriptions.removeAllSubscriptions(connection.getMessageDestination());
            }

            @Override public void onBound(ServerSocketConnector connector) {}

            @Override public void onBindFailure(ServerSocketConnector connector, IOException e) {}
        });

    }

    @Override public void start() {}

    @Override public void stop() {}

    public SubscriptionController<Destination<LoggingMessage>, LoggingMessage> getSubscriptions() {
        return subscriptions;
    }

    public void broadcast(ChannelMessage channelMessage, SocketConnectionInterface source) {
        logger.fine("Dispatching '{}' to channel '{}'", channelMessage, Arrays.toString(channelMessage.getChannel()));
        if (source == null) {
            subscriptions.dispatch(channelMessage.getChannel(), channelMessage, null);
        }
        else {
            subscriptions.dispatch(channelMessage.getChannel(), channelMessage, source.getMessageDestination());
            serverHandlerSubscriptions.dispatch(channelMessage.getChannel(), channelMessage, source);
        }
    }

    public void broadcast(ChannelMessage channelMessage) {
        subscriptions.dispatch(channelMessage.getChannel(), channelMessage, null);
    }

    @Override public void send(ChannelMessage t) {
        broadcast(t);
    }

    @Override public void subscribe(String channel, final Destination<ChannelMessage> destination) {
        logger.fine("Subscribing '{}' to channel '{}'", destination, channel);
        Destination<LoggingMessage> counterpart = new Destination<LoggingMessage>() {
            @Override public void send(LoggingMessage t) {
                if (t instanceof ChannelMessage) {
                    ChannelMessage channelMessage = (ChannelMessage) t;
                    destination.send(channelMessage);
                }
            }
        };

        internalCounterparts.put(destination, counterpart);
        subscriptions.addSubscription(channel, counterpart);
    }

    @Override public void unsubscribe(String channel, Destination<ChannelMessage> destination) {
        subscriptions.removeSubscription(channel, internalCounterparts.remove(destination));
    }

    @Override public void subscribe(String channel, ServerMessageHandler handler) {
        serverHandlerSubscriptions.addSubscription(channel, handler);
    }

    @Override public void unsubscribe(String channel, ServerMessageHandler handler) {
        serverHandlerSubscriptions.removeSubscription(channel, handler);
    }

}