package com.logginghub.messaging2.messages;


public class ResponseMessage extends RequestMessage {

    public ResponseMessage() {
        super();
    }

    public ResponseMessage(int requestID, Object requestPayload) {
        super(requestID, requestPayload);
    }

}
