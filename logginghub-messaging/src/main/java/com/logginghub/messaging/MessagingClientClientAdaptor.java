package com.logginghub.messaging;

import com.logginghub.messaging.MessagingClient.Status;


/**
 * Adapter class for the ObjectSocketClientListener interface.
 * 
 * @author James
 * 
 */
public class MessagingClientClientAdaptor implements MessagingClientClientListener
{
    public void onStatusChanged(Status oldStatus, Status newStatus)
    {}

    public void onNewMessage(Message message)
    {
    }
}
