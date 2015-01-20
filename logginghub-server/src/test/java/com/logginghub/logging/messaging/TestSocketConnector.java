package com.logginghub.logging.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnector;
import com.logginghub.utils.OneWayExchanger;
import com.logginghub.logging.LogEventComparer;

public class TestSocketConnector
{
    @Test
    public void testSocketConnector() throws IOException,
                                     LoggingMessageSenderException,
                                     InterruptedException,
                                     TimeoutException
    {
        int port = 34534;

        final ServerSocket acceptSocket = new ServerSocket(port);

        SocketConnector socketConnector = new SocketConnector();
        socketConnector.addConnectionPoint(new InetSocketAddress("localhost", port));

        final OneWayExchanger<LoggingMessage> socketExchanger = new OneWayExchanger<LoggingMessage>();
                        
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Socket serverSocket = acceptSocket.accept();
                    
                    SocketConnection server = new SocketConnection(serverSocket);

                    server.addLoggingMessageListener(new LoggingMessageListener()
                    {
                        public void onNewLoggingMessage(LoggingMessage message)
                        {
                            socketExchanger.set(message);
                        }
                    });
                    
                    server.start();
                }
                catch (IOException e)
                {
                    socketExchanger.failed(e);
                }
            }
        });
        thread.start();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApplication");
        LogEventMessage message = new LogEventMessage(event);
        socketConnector.send(message);

        LoggingMessage received = socketExchanger.get();
        LogEventMessage receivedMessage = (LogEventMessage)received;
        LogEventComparer.assertEquals(event, receivedMessage.getLogEvent());
        
        thread.join();
        acceptSocket.close();
        socketConnector.close();
    }
}
