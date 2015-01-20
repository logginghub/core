package com.logginghub.logging.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.SubscriptionResponseMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.utils.LoggingMessageBucket;
import com.logginghub.utils.BucketMatcher;

public class ValidateHub
{
    public static void main(String[] args) throws IOException
    {
        String host = "localhost";
        int port = LoggingPorts.getSocketHubDefaultPort();

        if (args.length > 0)
        {
            String firstArg = args[0];
            String[] split = firstArg.split(":");
            host = split[0];
            if (split.length > 1)
            {
                port = Integer.parseInt(split[1]);
            }
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        assertPortOpen(address);
        assertCanSendAndReceived(address);
    }

    private static void assertCanSendAndReceived(InetSocketAddress address)
    {
        try
        {
            SocketClient clientA = new SocketClient();
            SocketClient clientB = new SocketClient();

            clientA.addConnectionPoint(address);
            clientB.addConnectionPoint(address);

            LoggingMessageBucket bucketA = new LoggingMessageBucket();
            LoggingMessageBucket bucketB = new LoggingMessageBucket();

            clientA.addLoggingMessageListener(bucketA);
            clientB.addLoggingMessageListener(bucketB);

            clientA.setForceFlush(true);
            clientB.setForceFlush(true);

            clientA.connect();
            clientB.connect();

            SubscriptionRequestMessage subscriptionMessage = new SubscriptionRequestMessage();
            clientB.send(subscriptionMessage);

            bucketB.waitFor(new BucketMatcher<LoggingMessage>()
            {
                public boolean matches(LoggingMessage t)
                {
                    return t instanceof SubscriptionResponseMessage;
                }
            }, 5, TimeUnit.SECONDS);

            final UUID message = UUID.randomUUID();
            DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
            event.setMessage(message.toString());
            LoggingMessage logEventMessage = new LogEventMessage(event);
            clientA.send(logEventMessage);
            
            bucketB.waitFor(new BucketMatcher<LoggingMessage>()
            {
                public boolean matches(LoggingMessage t)
                {
                    boolean matches = false;
                    if(t instanceof LogEventMessage){
                        LogEventMessage logEventMessage = (LogEventMessage)t;
                        matches = logEventMessage.getLogEvent().getMessage().equals(message.toString());
                    }
                    return matches;
                }
            }, 5, TimeUnit.SECONDS);

            System.out.println("Send and receive check completed successfully, the hub looks to be running ok.");
            
            clientA.disconnect();
            clientB.disconnect();
        }
        catch (Exception e)
        {
            System.out.println("Send and receive check failed, " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void assertPortOpen(InetSocketAddress address)
    {
        try
        {
            Socket socket = new Socket();
            socket.setSoTimeout(1000);
            socket.connect(address);
            socket.close();
            System.out.println("Basic socket check completed successfully, host and port are ok.");
        }
        catch (IOException e)
        {
            System.out.println("Basic socket check failed, " + e.getMessage());
            e.printStackTrace();
        }
    }
}
