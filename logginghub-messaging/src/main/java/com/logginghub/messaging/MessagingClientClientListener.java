package com.logginghub.messaging;

import com.logginghub.messaging.MessagingClient.Status;


/**
 * Provides an observable interface into the ObjectSocketClient
 * @author James
 *
 */
public interface MessagingClientClientListener extends MessageListener
{
    void onStatusChanged(Status oldStatus, Status newStatus);
}
