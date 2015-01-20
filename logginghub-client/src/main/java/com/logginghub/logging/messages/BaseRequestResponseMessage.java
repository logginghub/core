package com.logginghub.logging.messages;

public class BaseRequestResponseMessage implements RequestResponseMessage {
    private int correlationID;

    public BaseRequestResponseMessage() {
    }

    public int getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(int correlationID) {
        this.correlationID = correlationID;
    }

}
