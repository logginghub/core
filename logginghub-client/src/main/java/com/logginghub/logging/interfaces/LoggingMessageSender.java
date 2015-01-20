package com.logginghub.logging.interfaces;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LoggingMessage;

public interface LoggingMessageSender {
    void send(LoggingMessage message) throws LoggingMessageSenderException;
}
