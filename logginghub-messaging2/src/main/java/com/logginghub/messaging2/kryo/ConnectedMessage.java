package com.logginghub.messaging2.kryo;

public class ConnectedMessage {
    private String destinationID;

    public ConnectedMessage(){
        
    }
    
    public ConnectedMessage(String destinationID) {
        super();
        this.destinationID = destinationID;
    }
    
    public String getDestinationID() {
        return destinationID;
    }
}
