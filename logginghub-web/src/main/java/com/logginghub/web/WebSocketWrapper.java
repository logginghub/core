package com.logginghub.web;

import org.eclipse.jetty.websocket.WebSocket;

import com.logginghub.utils.logging.Logger;

public class WebSocketWrapper implements WebSocket.OnTextMessage {

    private static final Logger logger = Logger.getLoggerFor(WebSocketWrapper.class);
    private WebSocketHelper helper;
    private Connection connection;

    public WebSocketWrapper(WebSocketHelper helper) {
        this.helper = helper;
    }

    @Override public void onOpen(Connection connection) {
        this.connection = connection;
        helper.onOpen(connection);        
    }

    @Override public void onClose(int closeCode, String message) {
        helper.onClose(connection, closeCode, message);
    }

    @Override public void onMessage(String data) {
        helper.onMessage(connection, data);        
    }

}
