package com.logginghub.messaging;

import java.io.Serializable;

public class ConnectedMessage implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final int clientID;

    public ConnectedMessage(int clientID)
    {
        this.clientID = clientID;
    }
    
    public int getClientID()
    {
        return clientID;
    }
}
