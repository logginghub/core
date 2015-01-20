package com.logginghub.logging.messaging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

/**
 * Convenience thread that manages a SocketClient and makes sure it stays connected
 * 
 * @author James
 * 
 */
public class SocketClientManager extends WorkerThread {
    private final SocketClient client;

    private long currentReconnectionTime = 1000;
    private long reconnectionTime = 1000;
    private long reconnectionTimeRampUp = 500;
    private long reconnectionTimeMax = 5000;

    private static final Logger logger = Logger.getLoggerFor(SocketClientManager.class);
    private boolean autoSubscribe;

    public enum State {
        NotConnected,
        Connecting,
        Connected
    };

    private State currentState = State.NotConnected;

    private List<SocketClientManagerListener> listeners = new CopyOnWriteArrayList<SocketClientManagerListener>();

    public SocketClientManager(SocketClient client) {
        super("SocketClientManager");
        this.client = client;
    }

    public void addSocketClientManagerListener(SocketClientManagerListener listener) {
        listeners.add(listener);
    }

    public void removeSocketClientManagerListener(SocketClientManagerListener listener) {
        listeners.remove(listener);
    }

    private void fireStateChange(State from, State to) {
        for (SocketClientManagerListener socketClientManagerListener : listeners) {
            socketClientManagerListener.onStateChanged(from, to);
        }
    }

    @Override protected void onRun() throws Throwable {
        // TODO : this code is riddled with race conditions, and the latch.await() will keep us
        // waiting for ever I think
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            if (!client.isConnected()) {
                setState(State.Connecting);
                logger.fine("Attempting connection");
                client.connect();
            }
            setState(State.Connected);
            logger.fine("Connected");
            SocketConnector connector = client.getConnector();

            // TODO : I'm worried we might be leaking these listeners as we never remove them!
            ConnectionListener reconnectionListener = new ConnectionListener() {
                public void onConnectionClosed(String reason) {
                    // Only do this is the manager is still actually running
                    if (isRunning()) {
                        setState(State.NotConnected);
                        reconnectSleep();
                        latch.countDown();
                    }
                }
            };
            
            // TODO : if we've been asynchronously disconnected, the current connection will be null and this will blow up
            connector.getCurrentConnection().addConnectionListener(reconnectionListener);

            latch.await();
        }
        catch (ConnectorException connectorException) {
            logger.fine(connectorException, "Failed to make a hub connection : {}", connectorException.getMessage());
            // getExceptionHandler().handleException("Socket connection failed",
            // connectorException);
            setState(State.NotConnected);
            // Ignore this and just keep trying to reconnect
            reconnectSleep();
        }
    }

    private void setState(State connecting) {
        State oldState = currentState;
        currentState = connecting;
        fireStateChange(oldState, currentState);
        logger.fine("Socket client manager connection state changing from {} to {}", oldState, currentState);

        if(connecting == State.Connected) {
            currentReconnectionTime = reconnectionTime;
        }else {
            currentReconnectionTime += reconnectionTimeRampUp;
            if(currentReconnectionTime > reconnectionTimeMax) {
                currentReconnectionTime = reconnectionTimeMax;
            }
        }

    }

    private void reconnectSleep() {
        try {
            Thread.sleep(currentReconnectionTime);
        }
        catch (InterruptedException e) {}
    }

    public SocketClient getClient() {
        return client;
    }


    public void setReconnectionTime(long reconnectionTime) {
        this.reconnectionTime = reconnectionTime;
    }

    public long getReconnectionTime() {
        return reconnectionTime;
    }

    public void setReconnectionTimeMax(long reconnectionTimeMax) {
        this.reconnectionTimeMax = reconnectionTimeMax;
    }

    public void setReconnectionTimeRampUp(long reconnectionTimeRampUp) {
        this.reconnectionTimeRampUp = reconnectionTimeRampUp;
    }

    public long getReconnectionTimeMax() {
        return reconnectionTimeMax;
    }

    public long getReconnectionTimeRampUp() {
        return reconnectionTimeRampUp;
    }
}
