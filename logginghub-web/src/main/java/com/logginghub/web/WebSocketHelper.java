package com.logginghub.web;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import com.logginghub.utils.logging.Logger;

public class WebSocketHelper {

    private static final Logger logger = Logger.getLoggerFor(WebSocketHelper.class);
    private List<WebSocketListener> listeners = new CopyOnWriteArrayList<WebSocketListener>();

    public void onOpen(Connection connection) {
        logger.info("Websockets connectio open : {}", connection);
        for (WebSocketListener webSocketListener : listeners) {
            webSocketListener.onOpen(connection);
        }
    }

    public void addListener(WebSocketListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }

    public void onClose(Connection connection, int closeCode, String message) {
        logger.info("Websockets connection closed : connection {} code {} message '{}'", connection, closeCode, message);
        for (WebSocketListener webSocketListener : listeners) {
            webSocketListener.onClosed(connection, closeCode, message);
        }
    }

    public void onMessage(Connection connection, String data) {
        logger.info("Websockets message received : connection '{}' : '{}'", connection, data);
        for (WebSocketListener webSocketListener : listeners) {
            webSocketListener.onMessage(connection, data);
        }
    }

}
