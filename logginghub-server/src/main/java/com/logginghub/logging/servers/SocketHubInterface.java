package com.logginghub.logging.servers;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectionInterface;

/**
 * Generalisation for the socket hub - hopefully will help refactor things out. Needs a better name
 * once it has become aparent what goes where.
 * 
 * @author James
 * 
 */
public interface SocketHubInterface {
    
    void addMessageListener(Class<? extends LoggingMessage> messageType, SocketHubMessageHandler handler);
    void removeMessageListener(Class<? extends LoggingMessage> messageType, SocketHubMessageHandler handler);
    
    void addConnectionListener(ServerSocketConnectorListener listener);
    void removeConnectionListener(ServerSocketConnectorListener listener);
    
//    void processInternalLogEvent(LogEvent event);

    void processLogEvent(LogEventMessage message, SocketConnectionInterface source);

    void addAndSubscribeLocalListener(FilteredMessageSender logger);


    void send(LogEvent event);
}
