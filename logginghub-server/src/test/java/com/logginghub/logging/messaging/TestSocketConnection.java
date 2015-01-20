package com.logginghub.logging.messaging;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.utils.NetUtils;

public class TestSocketConnection
{
    @Test public void repeat() throws IOException, LoggingMessageSenderException, InterruptedException {
        // There were some issues with the read thread not closing correctly, this is here to shake that down
        for(int i = 0; i < 1000; i++) {
            testSocketConnection();
        }
    }
    
    @Test
    public void testSocketConnection() throws IOException, LoggingMessageSenderException, InterruptedException
    {
        int port = NetUtils.findFreePort();

        ServerSocket acceptSocket = new ServerSocket(port);

        Socket clientSocket = new Socket("localhost", port);
        Socket serverSocket = acceptSocket.accept();

        SocketConnection client = new SocketConnection(clientSocket);
        client.start();
        
        SocketConnection server = new SocketConnection(serverSocket);

        final CountDownLatch latch = new CountDownLatch(1);

        server.addLoggingMessageListener(new LoggingMessageListener()
        {
            public void onNewLoggingMessage(LoggingMessage message)
            {
                latch.countDown();
            }
        });
        
        server.start();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApplication");
        LogEventMessage message = new LogEventMessage(event);
        client.send(message);

        assertTrue("Message didn't arrive at the server", latch.await(2, TimeUnit.SECONDS));
        
        acceptSocket.close();
        server.close();
        client.close();
    }
}
