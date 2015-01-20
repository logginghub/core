package com.logginghub.logging.servers;

import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectionInterface;

public interface SocketHubMessageHandler {

    void handle(LoggingMessage message, SocketConnectionInterface source);

}
