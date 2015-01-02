package com.logginghub.utils.net;

public interface ObjectSocketListener
{
    void onNewObject(Object object, ObjectSocketHandler source);
    void onConnectionClosed(ObjectSocketHandler source);
}
