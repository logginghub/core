package com.logginghub.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.messaging.MessageBucket;
import com.logginghub.messaging.MessagingClient;
import com.logginghub.messaging.MessagingServer;

@Ignore
public class TestClientIDs
{
    MessagingClient clientA;
    MessagingClient clientB;
    MessagingClient clientC;

    MessageBucket bucketA;
    MessageBucket bucketB;
    MessageBucket bucketC;

    MessagingServer server;

    @Before public void setup()
    {
        clientA = new MessagingClient();
        clientB = new MessagingClient();
        clientC = new MessagingClient();

        server = new MessagingServer();
        server.start();
        server.waitUntilBound();

        clientA.setName("ClientA");
        clientB.setName("ClientB");
        clientC.setName("ClientC");

        bucketA = new MessageBucket();
        bucketB = new MessageBucket();
        bucketC = new MessageBucket();

        clientA.addSocketClientListener(bucketA);
        clientB.addSocketClientListener(bucketB);
        clientC.addSocketClientListener(bucketC);

        clientA.start();
        clientA.waitUntilConnected();

        clientB.start();
        clientB.waitUntilConnected();

        clientC.start();
        clientC.waitUntilConnected();
    }

    @After public void teardown()
    {
        clientA.stop();
        clientB.stop();
        clientC.stop();
        server.stop();
    }

    @Test public void test()
    {
        assertThat(clientA.getClientID(), is(1));
        assertThat(clientB.getClientID(), is(2));
        assertThat(clientC.getClientID(), is(3));
    }
}
