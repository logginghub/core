package com.logginghub.logging.servers;

import java.io.IOException;

import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectionInterface;

public interface ServerSocketConnectorListener
{
    void onBound(ServerSocketConnector connector);
    void onBindFailure(ServerSocketConnector connector, IOException e);
    void onNewConnection(SocketConnectionInterface connection);
    void onConnectionClosed(SocketConnectionInterface connection, String reason);
    void onNewMessage(LoggingMessage message, SocketConnectionInterface source);
}
