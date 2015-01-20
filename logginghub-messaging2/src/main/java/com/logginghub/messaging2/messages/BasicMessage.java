package com.logginghub.messaging2.messages;

import com.logginghub.messaging2.api.Message;

public class BasicMessage implements Message {
    private Object payload;
    private String sourceID;
    private String destinationID;

    public BasicMessage() {

    }

    public BasicMessage(Object payload, String sourceID, String destinationID) {
        super();
        this.payload = payload;
        this.destinationID = destinationID;
        this.sourceID = sourceID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public String getDestinationID() {
        return destinationID;
    }

    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getSource() {
        return sourceID;
    }

    public void setSource(String source) {
        this.sourceID = source;
    }

    public String getDestination() {
        return destinationID;
    }

    public void setDestination(String destination) {
        this.destinationID = destination;
    }

    @Override public String toString() {
        return "BasicMessage [sourceID=" + sourceID + ", destinationID=" + destinationID + ", payload=" + payload + "]";
    }
    
    
}
