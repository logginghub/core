package com.logginghub.utils.net.objectclient;

import com.logginghub.utils.net.objectclient.ObjectSocketClient.Status;

/**
 * Provides an observable interface into the ObjectSocketClient
 * @author James
 *
 */
public interface ObjectSocketClientListener
{
    void onStatusChanged(Status oldStatus, Status newStatus);
    void onNewObject(Object object);
}
