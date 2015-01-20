package com.logginghub.logging.interfaces;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.utils.Destination;

public interface ChannelMessagingService {
    void send(ChannelMessage message) throws LoggingMessageSenderException;

    void subscribe(String channel, Destination<ChannelMessage> destination);
    void unsubscribe(String channel, Destination<ChannelMessage> destination);       
}
