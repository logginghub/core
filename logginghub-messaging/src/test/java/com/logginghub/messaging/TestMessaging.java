package com.logginghub.messaging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.messaging.Message;
import com.logginghub.messaging.MessageBucket;
import com.logginghub.messaging.MessagingClient;
import com.logginghub.messaging.MessagingServer;

@Ignore
public class TestMessaging
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
        clientB.start();
        clientC.start();

        clientA.waitUntilConnected();
        clientB.waitUntilConnected();
        clientC.waitUntilConnected();
    }

    @After public void teardown()
    {
        clientA.stop();
        clientB.stop();
        clientC.stop();
        server.stop();
    }

    @Test public void test() throws IOException
    {
        assertThat(bucketA.size(), is(0));
        assertThat(bucketB.size(), is(0));
        assertThat(bucketC.size(), is(0));

        String testMessage = "This is a broadcast object";
        String responseMessage = "This is a point-to-point response";
        
        // Broadcast a message to ClientB and ClientC
        clientA.broadcast(testMessage);
        
        bucketB.waitForMessages(1);
        bucketC.waitForMessages(1);

        assertThat(bucketA.size(), is(0));
        assertThat(bucketB.size(), is(1));
        assertThat(bucketC.size(), is(1));

        assertThat((String)bucketB.get(0).getPayload(), is(testMessage));
        assertThat((String)bucketC.get(0).getPayload(), is(testMessage));
        
        // Get clientB to respond to the message and send a reply to clientA
        Message message = bucketB.get(0);        
        clientB.reply(message, responseMessage);
        
        bucketA.waitForMessages(1);
        assertThat(bucketA.size(), is(1));
        assertThat(bucketB.size(), is(1));
        assertThat(bucketC.size(), is(1));

        assertThat((String)bucketA.get(0).getPayload(), is(responseMessage));
    }
}

