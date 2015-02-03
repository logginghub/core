package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.logging.servers.ServerSocketConnectorListener;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.servers.SocketHubMessageHandler;
import com.logginghub.logging.transaction.configuration.HubStackCaptureConfiguration;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.logging.utils.LoggingMessageBucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class TestHubStackCaptureModule {

    @Test public void test_basic() throws Exception {

        HubStackCaptureModule module = new HubStackCaptureModule();
        HubStackCaptureConfiguration configuration = new HubStackCaptureConfiguration();

        //        final Bucket<LoggingMessage> outgoingEvents = new Bucket<LoggingMessage>();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        //        discovery.bind(LoggingMessageSender.class, new LoggingMessageSender() {
        //            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
        //                outgoingEvents.add(message);
        //            }
        //        });

        HackClass hackClass = new HackClass();
        discovery.bind(SocketHubInterface.class, hackClass);

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
        configuration.setSnapshotBroadcastInterval("100 ms");

        module.configure(configuration, discovery);
        module.start();

        LoggingMessageBucket outgoingMessages = hackClass.outgoingMessages;
        outgoingMessages.waitForMessages(1, "2 seconds");

        assertThat(outgoingMessages.get(0), is(instanceOf(ChannelMessage.class)));
        assertThat(((ChannelMessage) outgoingMessages.get(0)).getPayload(), is(instanceOf(StackSnapshot.class)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getEnvironment(),
                is("env1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getHost(), is("host1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceNumber(),
                is(666));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceType(), is(
                "instanceType"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTimestamp(), is(
                greaterThan(0L)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTraces().length, is(
                greaterThan(0)));
    }

    @Test public void test_respond_to_strobe() throws Exception {

        HubStackCaptureModule module = new HubStackCaptureModule();
        HubStackCaptureConfiguration configuration = new HubStackCaptureConfiguration();


        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();

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

        HackClass hackClass = new HackClass();
        discovery.bind(SocketHubInterface.class, hackClass);

        configuration.setEnvironment("env1");
        configuration.setHost("host1");
        configuration.setInstanceNumber(666);
        configuration.setInstanceType("instanceType");
        configuration.setSnapshotBroadcastInterval("100 seconds");
        configuration.setRespondToRequests(true);

        module.configure(configuration, discovery);
        module.start();

        channelMessages.send(new ChannelMessage(Channels.stackStrobeRequests, new StackStrobeRequest("*", 10, 100)));

        LoggingMessageBucket outgoingMessages = hackClass.outgoingMessages;

        outgoingMessages.waitForMessages(10, "2 seconds");

        assertThat(outgoingMessages.get(0), is(instanceOf(ChannelMessage.class)));
        assertThat(((ChannelMessage) outgoingMessages.get(0)).getPayload(), is(instanceOf(StackSnapshot.class)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getEnvironment(),
                is("env1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getHost(), is("host1"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceNumber(),
                is(666));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getInstanceType(), is(
                "instanceType"));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTimestamp(), is(
                greaterThan(0L)));
        assertThat(((StackSnapshot) ((ChannelMessage) outgoingMessages.get(0)).getPayload()).getTraces().length, is(
                greaterThan(0)));

    }

    class HackClass implements LoggingMessageSender, SocketHubInterface {
        final LogEventBucket outgoingEvents = new LogEventBucket();
        final LoggingMessageBucket outgoingMessages = new LoggingMessageBucket();

        @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
            outgoingMessages.add(message);
        }

        @Override
        public void addMessageListener(Class<? extends LoggingMessage> messageType, SocketHubMessageHandler handler) {

        }

        @Override public void removeMessageListener(Class<? extends LoggingMessage> messageType,
                                                    SocketHubMessageHandler handler) {

        }

        @Override public void addConnectionListener(ServerSocketConnectorListener listener) {

        }

        @Override public void removeConnectionListener(ServerSocketConnectorListener listener) {

        }

        @Override public void processLogEvent(LogEventMessage message, SocketConnectionInterface source) {

        }

        @Override public void addAndSubscribeLocalListener(FilteredMessageSender logger) {

        }

        @Override public void send(LogEvent event) {
            outgoingEvents.add(event);
        }
    }

}
