package com.logginghub.logging.messaging;

public interface SocketConnectorListener {
    void onConnectionEstablished();
    void onConnectionLost(String reason);
}
