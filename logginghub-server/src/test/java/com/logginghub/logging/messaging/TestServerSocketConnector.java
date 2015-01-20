package com.logginghub.logging.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnector;
import com.logginghub.logging.servers.ServerSocketConnector;
import com.logginghub.logging.servers.ServerSocketConnectorListener;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.SystemErrExceptionHandler;
import com.logginghub.logging.LogEventComparer;

public class TestServerSocketConnector {
    @Test public void testSocketConnector() throws IOException, LoggingMessageSenderException, InterruptedException, TimeoutException {
        int port = NetUtils.findFreePort();

        // Setup the client
        SocketConnector socketConnector = new SocketConnector();
        socketConnector.addConnectionPoint(new InetSocketAddress("localhost", port));

        // Setup the server
        final Bucket<LoggingMessage> messages = new Bucket<LoggingMessage>();

        ServerSocketConnector serverSocketConnector = new ServerSocketConnector(port, new SystemErrExceptionHandler());
        serverSocketConnector.addServerSocketConnectorListener(new ServerSocketConnectorListener() {
            public void onConnectionClosed(SocketConnectionInterface connection, String reason) {

            }

            public void onNewConnection(SocketConnectionInterface connection) {
                System.out.println(connection);
            }

            public void onNewMessage(LoggingMessage message, SocketConnectionInterface source) {
                messages.add(message);
            }

            @Override public void onBound(ServerSocketConnector connector) {}

            @Override public void onBindFailure(ServerSocketConnector connector, IOException e) {}
        });

        serverSocketConnector.start();
        serverSocketConnector.waitUntilBound();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApplication");
        LogEventMessage message = new LogEventMessage(event);
        socketConnector.send(message);

        messages.waitForMessages(1);
        LogEventMessage receivedMessage = (LogEventMessage) messages.get(0);
        LogEventComparer.assertEquals(event, receivedMessage.getLogEvent());

        serverSocketConnector.stop();
        socketConnector.disconnect();
    }
}
