package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnector;
import com.logginghub.logging.modules.StackCaptureConfiguration;
import com.logginghub.logging.modules.StackCaptureModule;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;

public class TestStackCaptureModule {

    private StackCaptureModule module;
    private Bucket<LoggingMessage> outgoingMessages;
    private StackCaptureConfiguration configuration;
    private Multiplexer<LoggingMessage> incommingMessages;

    @Before public void setup() throws LoggingMessageSenderException {

        SocketConnector connector = Mockito.mock(SocketConnector.class);

        // Wire up the incomming messages stream
        incommingMessages = new Multiplexer<LoggingMessage>();
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final LoggingMessageListener t = (LoggingMessageListener) invocation.getArguments()[0];
                incommingMessages.addDestination(new Destination<LoggingMessage>() {
                    public void send(LoggingMessage message) {
                        t.onNewLoggingMessage(message);
                    }
                });
                return null;
            }
        }).when(connector).addLoggingMessageListener(Mockito.any(LoggingMessageListener.class));
        
        // And connect a bucket to the outgoing messages
        outgoingMessages = new Bucket<LoggingMessage>();
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                LoggingMessage t = (LoggingMessage) invocation.getArguments()[0];
                outgoingMessages.add(t);
                return null;
            }
        }).when(connector).send(Mockito.any(LoggingMessage.class));

        // Build the client with the stubbed connector
        SocketClient client = new SocketClient(connector);
        
        module = new StackCaptureModule(client, client);
        configuration = new StackCaptureConfiguration();
        configuration.setEnvironment("env1");
        configuration.setHost("host1");
        configuration.setInstanceNumber(666);
        configuration.setInstanceType("instanceType");
        configuration.setSnapshotInterval("100 ms");

    }

    @Test public void test_basic() throws Exception {

        module.configure(configuration, null);
        module.start();

        // Clear the subscription request message
        outgoingMessages.clear();
        outgoingMessages.waitForMessages(1, "1000 second");

        assertThat(outgoingMessages.get(0), is(instanceOf(ChannelMessage.class)));
        assertThat(((ChannelMessage) outgoingMessages.get(0)).getPayload(), is(instanceOf(StackSnapshot.class)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getEnvironment(), is("env1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getHost(), is("host1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceNumber(), is(666));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceType(), is("instanceType"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTimestamp(), is(greaterThan(0L)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTraces().length, is(greaterThan(0)));
    }

    @Test public void test_strobe() throws Exception {

        configuration.setSnapshotInterval("100 seconds");
        module.configure(configuration, null);
        module.start();

        // Clear the subscription request message
        outgoingMessages.clear();

        incommingMessages.send(new ChannelMessage(Channels.strobeRequests, new StackStrobeRequest("*", 10, 100)));

        outgoingMessages.waitForMessages(10, "2 seconds");

        assertThat(outgoingMessages.get(0), is(instanceOf(ChannelMessage.class)));
        assertThat(((ChannelMessage) outgoingMessages.get(0)).getPayload(), is(instanceOf(StackSnapshot.class)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getEnvironment(), is("env1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getHost(), is("host1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceNumber(), is(666));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceType(), is("instanceType"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTimestamp(), is(greaterThan(0L)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTraces().length, is(greaterThan(0)));

    }

}
