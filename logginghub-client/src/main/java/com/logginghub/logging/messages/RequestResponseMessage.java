package com.logginghub.logging.messages;

public interface RequestResponseMessage extends LoggingMessage {
    int getCorrelationID();
    void setCorrelationID(int requestID);
}
