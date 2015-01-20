package com.logginghub.web;

import org.eclipse.jetty.websocket.WebSocket.Connection;

public interface WebSocketListener {
    void onClosed(Connection connection, int closeCode, String message);
    void onOpen(Connection connection);
    void onMessage(Connection connection, String data);
}
