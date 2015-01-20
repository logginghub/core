package com.logginghub.logging.repository;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventCollection;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventCollectionMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.RepositoryRequestMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.filter.Filter;

public class SocketRepository implements LoggingMessageListener {
    private SocketClientManager socketClientManager;
    private SocketClient socketClient;
    private int port = LoggingPorts.getSocketHubDefaultPort();
    private LinkedList<LogEvent> events = new LinkedList<LogEvent>();
    private Executor executor = Executors.newCachedThreadPool();

    // TODO : enable multiple connection points, added from the launcher based
    // on a csv -D param?

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public synchronized void start() {
        if (socketClientManager == null) {
            socketClient = new SocketClient();
            socketClient.addLoggingMessageListener(this);
            socketClient.addConnectionPoint(new InetSocketAddress("localhost", port));
            socketClientManager = new SocketClientManager(socketClient);
            socketClientManager.setDaemon(false);
            socketClientManager.start();
        }
    }

    public void onNewLoggingMessage(LoggingMessage message) {
        processMessage(message);
    }

    private void processMessage(LoggingMessage message) {
        if (message instanceof LogEventMessage) {
            LogEvent logEvent = ((LogEventMessage) message).getLogEvent();
            processEvent(logEvent);
        }
        else if (message instanceof LogEventCollectionMessage) {
            LogEventCollection logEventCollection = ((LogEventCollectionMessage) message).getLogEventCollection();
            for (LogEvent logEvent : logEventCollection) {
                processEvent(logEvent);
            }
        }
        else if (message instanceof RepositoryRequestMessage) {
            RepositoryRequestMessage request = (RepositoryRequestMessage) message;
            // processRequest(message.getFromID(), request.getCorrelationID(), request.getFilter());
        }
    }

    private void processRequest(final int fromID, final int requestID, final Filter<LogEvent> filter) {
        executor.execute(new Runnable() {
            public void run() {
                executeRequest(fromID, requestID, filter);
            }
        });
    }

    protected void executeRequest(int fromID, int requestID, Filter<LogEvent> filter) {
        synchronized (events) {
            for (LogEvent event : events) {
                if (filter.passes(event)) {
                    LogEventMessage message = new LogEventMessage(event);
                    // message.setToID(fromID);
                    // message.setCorrelationID(requestID);
                    try {
                        socketClient.send(message);
                    }
                    catch (LoggingMessageSenderException e) {
                        // Hmmm what to do now...
                    }
                }
            }
        }
    }

    private void processEvent(LogEvent logEvent) {
        synchronized (events) {
            events.addFirst(logEvent);
        }
    }
}
