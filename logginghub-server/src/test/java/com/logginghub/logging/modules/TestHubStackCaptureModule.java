package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.modules.HubStackCaptureModule;
import com.logginghub.logging.transaction.configuration.HubStackCaptureConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestHubStackCaptureModule {

    @Test public void test_basic() throws Exception {

        HubStackCaptureModule module = new HubStackCaptureModule();
        HubStackCaptureConfiguration configuration = new HubStackCaptureConfiguration();

        final Bucket<LoggingMessage> outgoingMessages = new Bucket<LoggingMessage>();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(LoggingMessageSender.class, new LoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                outgoingMessages.add(message);
            }
        });

        final Multiplexer<ChannelMessage> channelMessages = new Multiplexer<ChannelMessage>();

        discovery.bind(ChannelMessagingService.class, new ChannelMessagingService() {
            @Override public void unsubscribe(String channel, Destination<ChannelMessage> destination) {
                channelMessages.addDestination(channelMessages);
            }

            @Override public void subscribe(String channel, Destination<ChannelMessage> destination) {
                channelMessages.removeDestination(channelMessages);
            }

            @Override public void send(ChannelMessage message) {}
        });

        configuration.setEnvironment("env1");
        configuration.setHost("host1");
        configuration.setInstanceNumber(666);
        configuration.setInstanceType("instanceType");
        configuration.setSnapshotInterval("100 ms");

        module.configure(configuration, discovery);
        module.start();

        outgoingMessages.waitForMessages(1, "200 ms");

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

        HubStackCaptureModule module = new HubStackCaptureModule();
        HubStackCaptureConfiguration configuration = new HubStackCaptureConfiguration();

        final Bucket<LoggingMessage> outgoingMessages = new Bucket<LoggingMessage>();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(LoggingMessageSender.class, new LoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                outgoingMessages.add(message);
            }
        });

        final Multiplexer<ChannelMessage> channelMessages = new Multiplexer<ChannelMessage>();

        discovery.bind(ChannelMessagingService.class, new ChannelMessagingService() {
            @Override public void unsubscribe(String channel, Destination<ChannelMessage> destination) {
                channelMessages.removeDestination(destination);
            }

            @Override public void subscribe(String channel, Destination<ChannelMessage> destination) {
                channelMessages.addDestination(destination);
            }

            @Override public void send(ChannelMessage message) {}
        });

        configuration.setEnvironment("env1");
        configuration.setHost("host1");
        configuration.setInstanceNumber(666);
        configuration.setInstanceType("instanceType");
        configuration.setSnapshotInterval("100 seconds");

        module.configure(configuration, discovery);
        module.start();

        channelMessages.send(new ChannelMessage(Channels.strobeRequests, new StackStrobeRequest("*", 10, 100)));

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
