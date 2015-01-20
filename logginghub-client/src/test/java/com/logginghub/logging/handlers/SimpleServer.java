package com.logginghub.logging.handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

/**
 * A simple remote log event listener. Connect to its port and send it log events, it'll collect
 * them up and provide some convenient methods for dealing with them. Very handy for testing
 * handlers and publishers.
 * 
 * @author admin
 */
public class SimpleServer extends WorkerThread {
    private ServerSocket serverSocket;
    private SocketConnection clientSocketConnection;
    private CountDownLatch boundLatch = new CountDownLatch(1);
    private int port = 666;
    private LogEventBucket bucket;

    private static final Logger logger = Logger.getLoggerFor(SimpleServer.class);

    public SimpleServer() {
        super("SimpleServer");
        bucket = new LogEventBucket();
    }

    /**
     * @return a localhost connection point for this server
     */
    public InetSocketAddress getConnectionPoint() {
        return new InetSocketAddress("localhost", port);
    }

    public void onRun() {
        try {
            if (serverSocket == null) {
                logger.debug("Binding to server socket on port {}...", port);
                serverSocket = new ServerSocket(port);
                logger.debug("Socket bound on port {}", port);
                boundLatch.countDown();
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to bind to server socket port " + port, e);
        }

        Socket clientSocket = null;

        try {
            logger.trace("Waiting to accept a new client socket...");
            clientSocket = serverSocket.accept();
            logger.trace("Client socket accepted : {}", clientSocket);
        }
        catch (IOException e) {
            serverSocket = null;
        }

        if (clientSocket != null) {
            try {
                clientSocketConnection = new SocketConnection(clientSocket);
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to create socket connection", e);
            }

            clientSocketConnection.addLoggingMessageListener(new LoggingMessageListener() {
                public void onNewLoggingMessage(LoggingMessage message) {
                    if (message instanceof LogEventMessage) {
                        logger.trace("LogEventMessage received from client");
                        LogEventMessage lev = (LogEventMessage) message;
                        bucket.add(lev.getLogEvent());
                    }
                }
            });
            clientSocketConnection.start();
            logger.debug("Client socket connection started");
        }
    }

    public int getListeningPort() {
        return port;
    }

    public void setListeningPort(int port) {
        this.port = port;
    }

    public LogEventBucket getBucket() {
        return bucket;
    }

    public void shutdown() {

        // jshaw - if you call either of these next two things without killing the thread first, you risk
        // it coming back a reopening them before we get to the stop call. We can't call stop
        // straight away as that joins the thread - so we need to indicate to the thread it
        // shouldn't run again if though we aren't joining it until these things are closed.
        
        dontRunAgain();
        
        if (clientSocketConnection != null) {
            clientSocketConnection.close();
            clientSocketConnection = null;
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to shutdown server socket");
            }
        }

        stop();
    }

    public void waitUntilBound() throws InterruptedException {
        boundLatch.await();
    }
}
