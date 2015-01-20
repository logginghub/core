package com.logginghub.messaging2.api;

public interface Message {

    String getSource();
    void setSource(String source);

    String getDestination();
    void setDestination(String destination);
    
    Object getPayload();
}
