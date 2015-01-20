package com.logginghub.messaging2.kryo;

public class ConnectionMessage {
    private String destinationID;

    public ConnectionMessage(){
        
    }
    
    public ConnectionMessage(String destinationID) {
        super();
        this.destinationID = destinationID;
    }
    
    public String getDestinationID() {
        return destinationID;
    }
}
