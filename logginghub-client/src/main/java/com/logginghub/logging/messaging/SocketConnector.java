package com.logginghub.logging.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.AbstractLoggingMessageSource;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.interfaces.LoggingMessageSource;
import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.utils.ConnectionPointManager;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

/**
 * Class that is in charge of making and maintaining socket connections seamlessly.
 * 
 * @author admin
 */
public class SocketConnector extends AbstractLoggingMessageSource implements LoggingMessageSource, LoggingMessageSender, LoggingMessageListener,
                ConnectionListener {

    private List<SocketConnectorListener> listeners = new CopyOnWriteArrayList<SocketConnectorListener>();
    private List<LoggingMessage> sendWhenConnected = new CopyOnWriteArrayList<LoggingMessage>();
    private Object connectionLock = new Object();
    private SocketConnection currentConnection = null;
    private ConnectionPointManager connectionPointManager = new ConnectionPointManager();
    private boolean forceFlush = false;
    private String name;
    private int writeQueueMaximumSize = 10000;
    private SlowSendingPolicy writeQueueOverflowPolicy = SlowSendingPolicy.discard;

    private static final Logger logger = Logger.getLoggerFor(SocketConnector.class);
    private boolean debug = false;
    private int connectionAttempts = 0;
    private int sent;

    public SocketConnector() {
        this("");
    }

    public SocketConnector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SocketConnection getCurrentConnection() {
        return currentConnection;
    }

    public ConnectionPointManager getConnectionPointManager() {
        return connectionPointManager;
    }

    public void addConnectionPoint(InetSocketAddress connectionPoint) {
        logger.debug("New connection point added : {}", connectionPoint);
        connectionPointManager.addConnectionPoint(connectionPoint);
    }

    public void removeConnectionPoint(InetSocketAddress connectionPoint) {
        connectionPointManager.removeConnectionPoint(connectionPoint);
    }

//    public void sendDirectWithNoQueue(LoggingMessage message) throws LoggingMessageSenderException {
//        ensureConnectedBeforeSendingMessage();
//        currentConnection.sendDirectWithNoQueue(message);
//    }

    private void ensureConnectedBeforeSendingMessage() throws LoggingMessageSenderException {
        synchronized (connectionLock) {
            if (currentConnection == null) {
                try {
                    connect();
                }
                catch (ConnectorException ce) {
                    throw new LoggingMessageSenderException("The connection attempt failed, so we are unable to send the message", ce);
                }
            }
        }
    }

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        if (debug) {
            debug("sending '{}'", message);
        }
        sent++;
        ensureConnectedBeforeSendingMessage();
        currentConnection.send(message);
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    public void connect() throws ConnectorException {
        synchronized (connectionLock) {
            if(isConnected()){
                return;
            }
            connectionAttempts++;
            InetSocketAddress nextConnectionPoint = connectionPointManager.getNextConnectionPoint();
            int retriesMax = connectionPointManager.getConnectionPoints().size();

            if (retriesMax == 0) {
                retriesMax = 1;
            }

            int retries = 0;

            SocketConnection connection = null;

            while (connection == null && retries < retriesMax) {
                try {
                    Socket socket = new Socket();

                    InetSocketAddress inetSocketAddress = new InetSocketAddress(nextConnectionPoint.getHostName(), nextConnectionPoint.getPort());
                    logger.trace("Connecting to {}...", inetSocketAddress);
                    socket.connect(inetSocketAddress);
                    logger.trace("Connection successful");

//                    Debug.out("CONNECTED : " + socket);

                    connection = new SocketConnection(socket, name);
                    connection.addLoggingMessageListener(this);
                    connection.addConnectionListener(this);
                    connection.setForceFlush(forceFlush);
                    connection.setWriteQueueMaximumSize(writeQueueMaximumSize);
                    connection.setWriteQueueOverflowPolicy(writeQueueOverflowPolicy);
                    connection.start();
                    connection.setDebug(debug);

                }
                catch (IOException e) {
                    logger.trace("Connection failed {}", e.getMessage());
                    // Try the next one
                    retries++;
                }
            }

            if (connection == null) {
                throw new ConnectorException("SocketConnector failed to establish a connection with any of the " +
                                             retriesMax +
                                             " connection points provided");
            }
            else {
                currentConnection = connection;

                for (SocketConnectorListener socketConnectorListener : listeners) {
                    socketConnectorListener.onConnectionEstablished();
                }

                for (LoggingMessage message : sendWhenConnected) {
                    try {
                        send(message);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void disconnect() {
        if (currentConnection != null) {
            currentConnection.close();
            currentConnection = null;
        }
    }

    public boolean isConnected() {
        return currentConnection != null;
    }

    public void onNewLoggingMessage(LoggingMessage message) {
        fireNewMessage(message);
    }

    public void close() {
        if (currentConnection != null) {
            currentConnection.close();
        }
    }

    public void replaceConnectionList(List<InetSocketAddress> newConnectionPointList) {
        InetSocketAddress currentConnectionPoint = connectionPointManager.getCurrentConnectionPoint();

        connectionPointManager.clearConnectionPoints();
        connectionPointManager.addConnectionPoints(newConnectionPointList);

        if (currentConnectionPoint != null) {
            if (newConnectionPointList.contains(currentConnectionPoint)) {
                // All good, no need to reconnect
            }
            else {
                if (currentConnection != null) {
                    currentConnection.close();
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////
    // ConnectionListner implementation
    // //////////////////////////////////////////////////////////////////

    public void onConnectionClosed(String reason) {
        currentConnection = null;

        for (SocketConnectorListener socketConnectorListener : listeners) {
            socketConnectorListener.onConnectionLost(reason);
        }
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
        if (currentConnection != null) {
            currentConnection.setForceFlush(forceFlush);
        }
    }

    public void flush() {
        if (currentConnection != null) {
            currentConnection.flush();
        }
    }

    public void setWriteQueueMaximumSize(int writeQueueMaximumSize) {
        this.writeQueueMaximumSize = writeQueueMaximumSize;
        if (currentConnection != null) {
            currentConnection.setWriteQueueMaximumSize(writeQueueMaximumSize);
        }
    }

    public void setWriteQueueOverflowPolicy(SlowSendingPolicy policy) {
        this.writeQueueOverflowPolicy = policy;
        if (currentConnection != null) {
            currentConnection.setWriteQueueOverflowPolicy(policy);
        }
    }

    public void sendWhenConnected(LoggingMessage message) {
        if (isConnected()) {
            try {
                send(message);
            }
            catch (LoggingMessageSenderException e) {
                e.printStackTrace();
            }
        }

        sendWhenConnected.add(message);

    }

    public void addSocketConnectorListener(SocketConnectorListener listener) {
        listeners.add(listener);
    }

    public void removeSocketConnectorListener(SocketConnectorListener listener) {
        listeners.remove(listener);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        if (currentConnection != null) {
            currentConnection.setDebug(debug);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String format, Object... params) {
        if (debug) {
            Out.out("{} | {}", name, StringUtils.format(format, params));
        }
    }

    public int getConnectionAttempts() {
        return connectionAttempts;
    }

    public int getSent() {
        return sent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConnectionID() {
        int connectionID = -1;
        if (currentConnection != null) {
            connectionID = currentConnection.getConnectionID();
        }
        return connectionID;

    }
}
