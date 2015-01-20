package com.logginghub.logging.utils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.UnsubscriptionRequestMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.utils.LoggingMessageBucket;

/**
 * Standalone version of one of the unit tests which can be used to validate a
 * hub is running. By default it will check localhost on the default hub port,
 * but you can provide the host and port as command line arguments if you want
 * to check a different instance.
 * 
 * @author James
 * 
 */
public class HubChecker
{
    public static void main(String[] args)
                    throws LoggingMessageSenderException, InterruptedException
    {
        String host = "localhost";
        int port = LoggingPorts.getSocketHubDefaultPort();

        if (args.length > 1)
        {
            host = args[0];
        }
        if (args.length > 2)
        {
            port = Integer.parseInt(args[1]);
        }

        InetSocketAddress address = new InetSocketAddress(host, port);

        SocketClient clientA = new SocketClient();
        SocketClient clientB = new SocketClient();

        clientA.addConnectionPoint(address);
        clientB.addConnectionPoint(address);

        LoggingMessageBucket bucketA = new LoggingMessageBucket();
        LoggingMessageBucket bucketB = new LoggingMessageBucket();

        clientA.addLoggingMessageListener(bucketA);
        clientB.addLoggingMessageListener(bucketB);

        LogEvent event = LogEventFactory.createFullLogEvent1();
        LoggingMessage logEventMessage = new LogEventMessage(event);
        clientA.send(logEventMessage);

        assertEquals(0, bucketA.getEvents().size());
        assertEquals(0, bucketB.getEvents().size());

        SubscriptionRequestMessage subscriptionMessage = new SubscriptionRequestMessage();
        clientB.send(subscriptionMessage);

        bucketB.waitForMessages(1, 2000, TimeUnit.SECONDS);

        assertEquals(0, bucketA.getEvents().size());
        assertEquals(1, bucketB.getEvents().size());

        clientA.send(logEventMessage);

        bucketB.waitForMessages(2, 2000, TimeUnit.SECONDS);

        assertEquals(0, bucketA.getEvents().size());
        assertEquals(2, bucketB.getEvents().size());

        UnsubscriptionRequestMessage unsubscriptionMessage = new UnsubscriptionRequestMessage();
        clientB.send(unsubscriptionMessage);

        bucketB.waitForMessages(3, 2000, TimeUnit.SECONDS);

        assertEquals(0, bucketA.getEvents().size());
        assertEquals(3, bucketB.getEvents().size());

        clientA.send(logEventMessage);

        Thread.sleep(1000);

        assertEquals(0, bucketA.getEvents().size());
        assertEquals(3, bucketB.getEvents().size());

        clientA.disconnect();
        clientB.disconnect();

        System.out.println("Hub check complete, it looks to be running fine");
    }

    private static void assertEquals(int a, int b)
    {
        throw new RuntimeException(String.format("Assertion failed : %d != %d",
                                                 a,
                                                 b));
    }
}
