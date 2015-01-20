package com.logginghub.messaging;

import java.io.Serializable;

public class Message implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Routing
    {
        PointToPoint,
        Broadcast,
        SendToServer
    };

    private Serializable payload;
    private int requestResponseMapping = -1;
    private Routing routing = Routing.SendToServer;
    private int sourceClientID;
    private int destinationClientID;

    public void setPayload(Serializable payload)
    {
        this.payload = payload;
    }

    @SuppressWarnings("unchecked") public <T extends Serializable> T getPayload()
    {
        return (T) payload;
    }

    public void setRequestResponseMapping(int requestResponseMapping)
    {
        this.requestResponseMapping = requestResponseMapping;
    }

    public int getRequestResponseMapping()
    {
        return requestResponseMapping;
    }

    public void setRouting(Routing routing)
    {
        this.routing = routing;
    }

    public Routing getRouting()
    {
        return routing;
    }

    public int getSourceClientID()
    {
        return sourceClientID;
    }

    public void setDestinationClientID(int destinationClientID)
    {
        this.destinationClientID = destinationClientID;
    }

    public void setSourceClientID(int sourceClientID)
    {
        this.sourceClientID = sourceClientID;
    }

    public int getDestinationClientID()
    {
        return destinationClientID;
    }

    @Override public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("payload=%s routing=%s",
                                     payload.getClass().getSimpleName(),
                                     this.routing));

        builder.append(String.format(" source=%d", this.sourceClientID));
        builder.append(String.format(" destination=%s", this.destinationClientID));
        if(requestResponseMapping > 0){
            builder.append(String.format(" rrm=%d", this.requestResponseMapping));
        }

        return builder.toString();
    }
}
