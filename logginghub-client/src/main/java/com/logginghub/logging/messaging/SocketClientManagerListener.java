package com.logginghub.logging.messaging;

public interface SocketClientManagerListener
{
    void onStateChanged(SocketClientManager.State fromState, SocketClientManager.State toState);
}
