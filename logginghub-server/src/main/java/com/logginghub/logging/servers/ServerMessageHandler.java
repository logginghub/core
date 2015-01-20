package com.logginghub.logging.servers;

import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.LoggingMessage;

public interface ServerMessageHandler {
    void onMessage(LoggingMessage message, LoggingMessageSender source);
}
