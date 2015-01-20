package com.logginghub.messaging2.local;

import com.logginghub.messaging2.Destination;

public class SimpleDestination implements Destination {

    private final String destinationID;

    public SimpleDestination(String destinationID) {
        this.destinationID = destinationID;
    }

    public String getDestinationID() {
        return destinationID;
    }
    
}
