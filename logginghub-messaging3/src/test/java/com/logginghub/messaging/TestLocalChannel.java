package com.logginghub.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.messaging.Level2AsyncClient;
import com.logginghub.messaging.Level2AsyncServer;
import com.logginghub.messaging.MessagingChannel;
import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.RequestContext;
import com.logginghub.messaging.netty.WrappedMessageListener;

@Ignore // TODO : it dies randomly from ANT
public class TestLocalChannel {

    private Level2AsyncClient clientA;
    private Level2AsyncClient clientB;
    private Level2AsyncClient clientC;
    private Level2AsyncServer server;

    @Before public void setup() {
        clientA = new Level2AsyncClient("ClientA");
        clientB = new Level2AsyncClient("ClientB");
        clientC = new Level2AsyncClient("ClientB");

        server = new Level2AsyncServer("Server");

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);
        clientC.addConnectionPoint(server);

        server.bind().await();
        clientA.connect().await();
        clientB.connect().await();
        clientC.connect().await();
    }

    @After public void teardown() {
        // FileUtils.closeQuietly(clientA, clientB, clientC, server);
    }

    @Test public void test() {
        clientA.subscribe("clientA").await();
        clientB.setHomeChannel("clientB").await();

        clientA.addLocalChannelListener("clientA", "subChannel1", new WrappedMessageListener() {
            public <T> void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender) {
                requestContext.reply("sub1:" + message.getPayload().toString());
            }
        });

        clientA.addLocalChannelListener("clientA", "subChannel2", new WrappedMessageListener() {
            public <T> void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender) {
                requestContext.reply("sub2:" + message.getPayload().toString());
            }
        });

        assertThat(clientB.sendRequest("clientA", "subChannel1", "Hello!", String.class).awaitResponse(), is("sub1:Hello!"));
        assertThat(clientB.sendRequest("clientA", "subChannel2", "Hello!", String.class).awaitResponse(), is("sub2:Hello!"));

    }
    
    @Test public void test_file_exchange_simulation() {
        clientA.subscribe("clientA").await();
        clientB.setHomeChannel("clientB").await();

        
        clientA.addLocalChannelListener("clientA", "fileServer", new WrappedMessageListener() {
            public <T> void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender) {
                requestContext.reply("sub1:" + message.getPayload().toString());
            }
        });

        clientA.addLocalChannelListener("clientA", "subChannel2", new WrappedMessageListener() {
            public <T> void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender) {
                requestContext.reply("sub2:" + message.getPayload().toString());
            }
        });

        assertThat(clientB.sendRequest("clientA", "subChannel1", "Hello!", String.class).awaitResponse(), is("sub1:Hello!"));
        assertThat(clientB.sendRequest("clientA", "subChannel2", "Hello!", String.class).awaitResponse(), is("sub2:Hello!"));

    }

    
    @Test public void test_message_channel() {
        
        clientA.setHomeChannel("clientA").await();
        clientB.setHomeChannel("clientB").await();
        
        clientB.bindToChannel("clientB", "services/echo", new WrappedMessageListener() {
            public <T> void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender) {
                requestContext.reply("sub1:" + message.getPayload().toString());
            }
        });
        
        MessagingChannel channel = clientA.getHelper().openChannel("clientB", "services/echo");
        
        String response = channel.sendRequest("hello", String.class).awaitResponse();
        assertThat(response, is("sub1:hello"));
        
        
    }

}
