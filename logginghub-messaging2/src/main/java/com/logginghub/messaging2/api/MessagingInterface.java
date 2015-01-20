package com.logginghub.messaging2.api;

import java.util.concurrent.TimeUnit;

import com.logginghub.messaging2.Hub;
import com.logginghub.messaging2.kryo.ResponseHandler;



public interface MessagingInterface {
    
    void send(String destinationID, Object payload);
    <T> T sendRequest(String destinationID, Object payload);
    <T> T sendRequest(String destinationID, Object payload, int time, TimeUnit units);
    void sendResponse(String destinationID, int requestID, Object responsePayload);
    <T> void sendRequest(String destination, Object payload, final ResponseHandler<T> responseHandler);
    
    void addMessageListener(MessageListener messageListener);
    void removeMessageListener(MessageListener messageListener);

    void connect();
    void addConnectionPoint(Hub hub);
    String getDestinationID();
    int allocateRequestID();   
}
