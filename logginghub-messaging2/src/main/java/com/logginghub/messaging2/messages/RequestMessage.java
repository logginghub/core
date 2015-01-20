package com.logginghub.messaging2.messages;


public class RequestMessage extends BasicMessage {

    private int requestID;

    public RequestMessage(int requestID, Object requestPayload) {
        this.requestID = requestID;
        setPayload(requestPayload);
    }

    public RequestMessage() {}

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }
}
