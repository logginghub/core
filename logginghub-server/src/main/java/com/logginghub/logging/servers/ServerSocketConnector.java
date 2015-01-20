package com.logginghub.logging.servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.Out;
import com.logginghub.utils.SystemErrExceptionHandler;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.ExceptionPolicy.Policy;

/**
 * Creates a server socket and accepts connections - the sockets are passed of to SocketConnections
 * to handle the io.
 * 
 * @author admin
 * 
 */
public class ServerSocketConnector {
    private WorkerThread acceptorThread;
    private int port;
    private volatile ServerSocket serverSocket;
    private ExceptionHandler exceptionHandler = new SystemErrExceptionHandler();
    private int maximumClientSendQueueSize = SocketConnection.writeBufferDefaultSize;
    private CountDownLatch boundLatch = new CountDownLatch(1);

    private List<ServerSocketConnectorListener> listeners = new CopyOnWriteArrayList<ServerSocketConnectorListener>();
    private volatile boolean shuttingDown = false;
    private boolean isBound = false;
    private long socketBindRetryTime = Long.getLong("serverSocketConnector.socketBindRetryTime", 200);

    public ServerSocketConnector(int port, ExceptionHandler exceptionHandler) throws IOException {
        this.port = port;
        this.exceptionHandler = exceptionHandler;
    }

    public void addServerSocketConnectorListener(ServerSocketConnectorListener listener) {
        listeners.add(listener);
    }

    public void removeServerSocketConnectorListener(ServerSocketConnectorListener listener) {
        listeners.remove(listener);
    }

    public int getPort() {
        return port;
    }

    public void start() {

        if (acceptorThread != null) {
            throw new RuntimeException("You've started the server socket connector acceptor thread already.");
        }

        acceptorThread = new WorkerThread("ServerSocketConnectorAcceptorThread") {
            @Override protected void onRun() throws Throwable {
                ensureBound();
                if (!shuttingDown && serverSocket != null) {
                    accept();
                }
            }
        };

        acceptorThread.start();
    }

    protected void ensureBound() {
        if (!isBound) {
            while (!isBound && !shuttingDown) {
                try {
                    serverSocket = new ServerSocket();
                    if (port == -1) {
                        serverSocket.bind(null);
                        port = serverSocket.getLocalPort();
                    }
                    else {

                        serverSocket.bind(new InetSocketAddress(port));
                    }
                    isBound = true;
                    fireBound();
                    boundLatch.countDown();
                }
                catch (IOException e) {
                    serverSocket = null;
                    if (!shuttingDown) {
                        fireBindFailure(e);
                        ThreadUtils.sleep(socketBindRetryTime);
                    }
                }
            }
        }
    }

    public void waitUntilBound(Timeout defaultTimeout) {
        try {
            boolean completedOk = boundLatch.await(defaultTimeout.getTime(), defaultTimeout.getUnits());
            if (!completedOk) {
                throw new RuntimeException("The wait timed out; socket may not be bound");
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Thread was interupted waiting for the socket to bind", e);
        }
    }

    public void waitUntilBound() {
        try {
            boundLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Thread was interupted waiting for the socket to bind", e);
        }
    }

    private void accept() {
        try {
            Socket socket = serverSocket.accept();

            final SocketConnection connection = new SocketConnection(socket, "Handler-" + socket);

            connection.setWriteQueueMaximumSize(maximumClientSendQueueSize);
            connection.setStatusLogging(false);
            connection.getExceptionPolicy().setPolicy(Policy.Ignore);
            connection.addConnectionListener(new ConnectionListener() {
                public void onConnectionClosed(String reason) {
                    fireConnectionClosed(connection, reason);
                }
            });

            connection.addLoggingMessageListener(new LoggingMessageListener() {
                public void onNewLoggingMessage(LoggingMessage message) {
                    fireNewMessage(message, connection);
                }
            });

            fireNewConnection(connection);
            connection.start();
        }
        catch (IOException e) {
            if (!shuttingDown) {
                exceptionHandler.handleException("Exception caught from accept call, or from listener notifications", e);
            }
        }
    }

    private void fireNewMessage(LoggingMessage message, SocketConnection connection) {
        for (ServerSocketConnectorListener listener : listeners) {
            listener.onNewMessage(message, connection);
        }
    }

    private void fireBindFailure(IOException e) {
        for (ServerSocketConnectorListener listener : listeners) {
            listener.onBindFailure(this, e);
        }
    }

    private void fireBound() {
        for (ServerSocketConnectorListener listener : listeners) {
            listener.onBound(this);
        }
    }

    private void fireNewConnection(SocketConnection connection) {
        for (ServerSocketConnectorListener listener : listeners) {
            listener.onNewConnection(connection);
        }
    }

    private void fireConnectionClosed(SocketConnection connection, String reason) {
        for (ServerSocketConnectorListener listener : listeners) {
            listener.onConnectionClosed(connection, reason);
        }
    }

    public void stop() {
        boundLatch.countDown();
        shuttingDown = true;
        acceptorThread.dontRunAgain();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                Out.err("Warning - failed to close ServerSocketConnection socket");
                e.printStackTrace(System.err);
            }
        }
        acceptorThread.stop();
    }

    public void setMaximumClientSendQueueSize(int maximumClientSendQueueSize) {
        this.maximumClientSendQueueSize = maximumClientSendQueueSize;
    }

    public boolean isBound() {
        return isBound;

    }

}