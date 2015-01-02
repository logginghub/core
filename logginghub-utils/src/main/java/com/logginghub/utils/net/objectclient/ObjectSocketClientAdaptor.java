package com.logginghub.utils.net.objectclient;

import com.logginghub.utils.net.objectclient.ObjectSocketClient.Status;

/**
 * Adapter class for the ObjectSocketClientListener interface.
 * 
 * @author James
 * 
 */
public class ObjectSocketClientAdaptor implements ObjectSocketClientListener
{
    public void onStatusChanged(Status oldStatus, Status newStatus)
    {}

    public void onNewObject(Object object)
    {}
}
