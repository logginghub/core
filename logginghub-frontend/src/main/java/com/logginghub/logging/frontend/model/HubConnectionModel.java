package com.logginghub.logging.frontend.model;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the active connection to a particular hub.
 *
 * @author James
 */
public class HubConnectionModel extends Observable implements LogEventSource, LogEventListener, Closeable {

    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableProperty<String> host = createStringProperty("host", "");
    private ObservableInteger port = createIntProperty("port", -1);
    private ObservableProperty<ConnectionState> connectionState = createProperty("connectionState", ConnectionState.class, ConnectionState.NotConnected);
    private ObservableProperty<String> channel = createStringProperty("channel", "");
    private ObservableProperty<Boolean> overrideTime = createBooleanProperty("overrideTime", false);


    public enum ConnectionState {
        Connected, NotConnected, AttemptingConnection
    }

    private LogEventMultiplexer multiplexer = new LogEventMultiplexer();
    private SocketClientManager socketClientManager;

    private List<InetSocketAddress> clusteredConnectionPoints = new ArrayList<InetSocketAddress>();

    public HubConnectionModel() {
        getName().set("no name");
        getHost().set("no host");
        getPort().set(-1);
    }


    public ObservableProperty<ConnectionState> getConnectionState() {
        return connectionState;
    }

    public ObservableProperty<String> getChannel() {
        return channel;
    }

    public ObservableProperty<String> getHost() {
        return host;
    }

    public ObservableProperty<String> getName() {
        return name;
    }

    public ObservableInteger getPort() {
        return port;
    }

    @Override public void addLogEventListener(LogEventListener listener) {
        multiplexer.addLogEventListener(listener);
    }

    @Override public void removeLogEventListener(LogEventListener listener) {
        multiplexer.removeLogEventListener(listener);
    }

    public ObservableProperty<Boolean> getOverrideTime() {
        return overrideTime;
    }

    @Override public void onNewLogEvent(LogEvent event) {
        if (overrideTime.get()) {
            DefaultLogEvent dle = (DefaultLogEvent) event;
            dle.setOriginTime(System.currentTimeMillis());
        }
        multiplexer.onNewLogEvent(event);
    }

    public SocketClientManager getSocketClientManager() {
        return socketClientManager;
    }

    public void setSocketClientManager(SocketClientManager socketClientManager) {
        // james - not sure I want this to be a observable enum field thing?
        // Still it might be useful if things want to listen for when its wired
        // in, but shoulnd't they be listening for the connected state change
        // anyway?
        this.socketClientManager = socketClientManager;
    }

    public void close() {
        if (socketClientManager != null) {
            socketClientManager.stop();
            SocketClient client = socketClientManager.getClient();
            if (client != null) {
                client.close();
            }
        }
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        clusteredConnectionPoints.add(inetSocketAddress);
    }

    public List<InetSocketAddress> getClusteredConnectionPoints() {
        return clusteredConnectionPoints;
    }
}
