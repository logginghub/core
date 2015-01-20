package com.logginghub.logging.frontend.model;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.interfaces.LogEventSource;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the active connection to a particular hub.
 *
 * @author James
 */
public class HubConnectionModel extends ObservableModel implements LogEventSource, LogEventListener, Closeable {

    public enum Fields implements FieldEnumeration {
        Name, Host, Port, ConnectionState, Channel
    }

    public enum ConnectionState {
        Connected, NotConnected, AttemptingConnection
    }

    private LogEventMultiplexer multiplexer = new LogEventMultiplexer();
    private SocketClientManager socketClientManager;
    private boolean overrideTime = false;

    private List<InetSocketAddress> clusteredConnectionPoints = new ArrayList<InetSocketAddress>();

    public HubConnectionModel() {
        set(Fields.Name, "no name");
        set(Fields.Host, "no host");
        set(Fields.Port, "-1");
    }

    public void setOverrideTime(boolean overrideTime) {
        this.overrideTime = overrideTime;
    }

    public String getName() {
        return get(Fields.Name);
    }

    public String getHost() {
        return get(Fields.Host);
    }

    public int getPort() {
        return getInt(Fields.Port);
    }

    public ConnectionState getConnectionState() {
        return (ConnectionState) getObject(Fields.ConnectionState);
    }

    public void setConnectionState(ConnectionState state) {
        set(Fields.ConnectionState, state);
    }

    @Override public void addLogEventListener(LogEventListener listener) {
        multiplexer.addLogEventListener(listener);
    }

    @Override public void removeLogEventListener(LogEventListener listener) {
        multiplexer.removeLogEventListener(listener);
    }

    @Override public void onNewLogEvent(LogEvent event) {
        if (overrideTime) {
            DefaultLogEvent dle = (DefaultLogEvent) event;
            dle.setLocalCreationTimeMillis(System.currentTimeMillis());
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
