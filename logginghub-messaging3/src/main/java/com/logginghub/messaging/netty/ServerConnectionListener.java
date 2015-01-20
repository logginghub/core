package com.logginghub.messaging.netty;

import java.net.InetSocketAddress;


public interface ServerConnectionListener {
    void onBound(InetSocketAddress address);
    void onBindFailure(InetSocketAddress address, Exception e);
    void onNewConnection(ServerHandler serverHandler);
    void onDisconnection(ServerHandler serverHandler);
}
