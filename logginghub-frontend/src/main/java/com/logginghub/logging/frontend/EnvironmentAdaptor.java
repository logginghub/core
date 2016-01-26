package com.logginghub.logging.frontend;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.HubConnectionModel.ConnectionState;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.frontend.modules.EnvironmentNotificationListener;
import com.logginghub.logging.frontend.modules.SocketClientDirectAccessService;
import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.RequestResponseMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListAdaptor;
import com.logginghub.utils.observable.ObservablePropertyListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class EnvironmentAdaptor implements EnvironmentNotificationService, EnvironmentMessagingService, SocketClientDirectAccessService {

    private EnvironmentModel environmentModel;
    private boolean environmentConnectionEstablished = false;
    private List<EnvironmentNotificationListener> listeners = new CopyOnWriteArrayList<EnvironmentNotificationListener>();

    public EnvironmentAdaptor(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;

        ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
        hubConnectionModels.addListenerAndNotifyCurrent(new ObservableListAdaptor<HubConnectionModel>() {
            @Override
            public void onAdded(final HubConnectionModel hubConnectionModel) {

                hubConnectionModel.getConnectionState().addListenerAndNotifyCurrent(new ObservablePropertyListener<ConnectionState>() {
                    @Override
                    public void onPropertyChanged(ConnectionState oldValue, ConnectionState newValue) {
                        updateState(hubConnectionModel);
                    }
                });
            }
        });

    }

    private void updateState(final HubConnectionModel t) {
        ConnectionState connectionState = t.getConnectionState().get();
        if (connectionState == ConnectionState.Connected) {
            environmentConnectionEstablished = true;

            for (EnvironmentNotificationListener listener : listeners) {
                listener.onEnvironmentConnectionEstablished();
                listener.onHubConnectionEstablished(null);
            }
        }
    }

    @Override public void addListener(EnvironmentNotificationListener environmentNotificationListener) {
        listeners.add(environmentNotificationListener);
    }

    @Override public void removeListener(EnvironmentNotificationListener environmentNotificationListener) {
        listeners.remove(environmentNotificationListener);
    }

    @Override public boolean isEnvironmentConnectionEstablished() {
        return environmentConnectionEstablished;
    }

    @Override public <T> void send(final RequestResponseMessage request, final Destination<LoggingMessage> destination) {

        final SocketClient client = getClient();

        try {
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

        }
        catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException(e, "Failed to send message");
        }

    }

    private SocketClient getClient() {
        SocketClient client = null;
        ObservableList<HubConnectionModel> hubConnectionModels = environmentModel.getHubConnectionModels();
        if (hubConnectionModels.size() > 0) {
            HubConnectionModel hubConnectionModel = hubConnectionModels.get(0);
            client = hubConnectionModel.getSocketClientManager().getClient();
        }

        return client;
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

        try {
            SocketClient client = getClient();
            request.setCorrelationID(client.getNextCorrelationID());
            client.addLoggingMessageListener(listener);
            client.send(request);
        }
        catch (LoggingMessageSenderException e) {
            throw new FormattedRuntimeException(e, "Failed to send message");
        }

        return listener;

    }

    @Override public void stopStreaming(Object streamingToken) {
        SocketClient client = getClient();
        client.removeLoggingMessageListener((LoggingMessageListener) streamingToken);
    }

    @Override public Future<Boolean> subscribe(String channel, Destination<ChannelMessage> destination) {
        final SocketClient client = getClient();
        Future<Boolean> addSubscription = client.addSubscription(channel, destination);
        return addSubscription;

    }

    @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
        final SocketClient client = getClient();
        client.send(message);
    }

    public void addLogEventListener(LogEventListener logEventListener) {
        getClient().addLogEventListener(logEventListener);
    }

    public void removeLogEventListener(LogEventListener logEventListener) {
        getClient().removeLogEventListener(logEventListener);
    }

    @Override public void subscribeToPrivateChannel(Destination<ChannelMessage> destination) {
        final SocketClient client = getClient();
        client.subscribe(Channels.getPrivateConnectionChannel(client.getConnectionID()), destination);
    }

    @Override public void unsubscribeFromPrivateChannel(Destination<ChannelMessage> destination) {
        final SocketClient client = getClient();
        client.unsubscribe(Channels.getPrivateConnectionChannel(client.getConnectionID()), destination);
    }

    @Override public int getConnectionID() {
        return getClient().getConnectionID();
    }

    @Override public List<SocketClient> getDirectAccess() {
        // TODO : support multiple hubs, and multiple environments?
        List<SocketClient> clients = new ArrayList<SocketClient>();
        clients.add(getClient());
        return clients;
    }

   
}
