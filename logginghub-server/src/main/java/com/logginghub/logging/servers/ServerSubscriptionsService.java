package com.logginghub.logging.servers;


import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;

public interface ServerSubscriptionsService {
    void send(ChannelMessage message) throws LoggingMessageSenderException;
    void subscribe(String channel, ServerMessageHandler handler);
    void unsubscribe(String channel, ServerMessageHandler handler);
}
