package com.logginghub.logging.servers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import com.logginghub.logging.messaging.SocketConnectionInterface;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.messages.ConnectedMessage;
import com.logginghub.logging.messages.EventSubscriptionRequestMessage;
import com.logginghub.logging.messages.EventSubscriptionResponseMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messages.SubscriptionResponseMessage;
import com.logginghub.logging.messages.UnsubscriptionRequestMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.servers.ServerSocketConnector;
import com.logginghub.logging.servers.ServerSocketConnectorListener;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.MutableBoolean;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerStream;

public class TestSocketHub {

    @Test public void test_socket_hub_filtering_starts_with() throws IOException {

        DefaultLogEvent eventA = LogEventBuilder.start().setMessage("starts with this message").toLogEvent();
        DefaultLogEvent eventB = LogEventBuilder.start().setMessage("Doesn't start with this message").toLogEvent();
        DefaultLogEvent eventC = LogEventBuilder.start().setMessage("Event contains animals (fish) in the message").toLogEvent();
        DefaultLogEvent eventD = LogEventBuilder.start().setMessage("Event contains animals (dogs) in the message").toLogEvent();

        SocketHub hub = new SocketHub();

        Bucket<LogEvent> logEvents = new Bucket<LogEvent>();
        hub.addDestination(logEvents);
        hub.addFilter(FilterConfiguration.startsWith("starts with"));

        hub.onNewMessage(new LogEventMessage(eventA), null);
        hub.onNewMessage(new LogEventMessage(eventB), null);
        hub.onNewMessage(new LogEventMessage(eventC), null);
        hub.onNewMessage(new LogEventMessage(eventD), null);

        assertThat(logEvents.size(), is(3));
        assertThat(logEvents.get(0).getMessage(), is("Doesn't start with this message"));
        assertThat(logEvents.get(1).getMessage(), is("Event contains animals (fish) in the message"));
        assertThat(logEvents.get(2).getMessage(), is("Event contains animals (dogs) in the message"));

        logEvents.clear();
        hub.addFilter(FilterConfiguration.contains("fish"));

        hub.onNewMessage(new LogEventMessage(eventA), null);
        hub.onNewMessage(new LogEventMessage(eventB), null);
        hub.onNewMessage(new LogEventMessage(eventC), null);
        hub.onNewMessage(new LogEventMessage(eventD), null);

        assertThat(logEvents.size(), is(2));
        assertThat(logEvents.get(0).getMessage(), is("Doesn't start with this message"));
        assertThat(logEvents.get(1).getMessage(), is("Event contains animals (dogs) in the message"));

        logEvents.clear();
        hub.addFilter(FilterConfiguration.regex(".*animal.*"));

        hub.onNewMessage(new LogEventMessage(eventA), null);
        hub.onNewMessage(new LogEventMessage(eventB), null);
        hub.onNewMessage(new LogEventMessage(eventC), null);
        hub.onNewMessage(new LogEventMessage(eventD), null);

        assertThat(logEvents.size(), is(1));
        assertThat(logEvents.get(0).getMessage(), is("Doesn't start with this message"));

        hub.close();

    }

    @Test public void test_test_hubs_ports_not_conflicting() throws IOException {

        final MutableBoolean hasWarning = new MutableBoolean(false);
        Logger.root().addStream(new LoggerStream() {
            @Override public void onNewLogEvent(com.logginghub.utils.logging.LogEvent event) {
                if (event.getLevel() > Logger.info) {
                    hasWarning.value = true;
                }
            }
        });

        SocketHub hub1 = SocketHub.createTestHub();
        SocketHub hub2 = SocketHub.createTestHub();
        SocketHub hub3 = SocketHub.createTestHub();

        hub1.start();
        hub2.start();
        hub3.start();

        hub1.stop();
        hub2.stop();
        hub3.stop();

        assertThat(hasWarning.value, is(false));
    }

    @Test public void test_hub_with_port_bound() throws IOException {

        SocketHub hub = SocketHub.createTestHub();

        // Maliciously bind another server socket on the hub's port
        ServerSocket socket = new ServerSocket();
        socket.bind(new InetSocketAddress(hub.getPort()));

        // Start up the hub, this should result in some failures
        hub.start();

        final Bucket<IOException> failBucket = new Bucket<IOException>();
        final Bucket<ServerSocketConnector> boundBucket = new Bucket<ServerSocketConnector>();

        ServerSocketConnectorListener listener = new ServerSocketConnectorListener() {
            @Override public void onNewMessage(LoggingMessage message, SocketConnectionInterface source) {}

            @Override public void onNewConnection(SocketConnectionInterface connection) {}

            @Override public void onConnectionClosed(SocketConnectionInterface connection, String reason) {}

            @Override public void onBindFailure(ServerSocketConnector connector, IOException e) {
                failBucket.add(e);
            }

            @Override public void onBound(ServerSocketConnector connector) {
                boundBucket.add(connector);
            }
        };

        ServerSocketConnector serverSocketConnector = hub.getServerSocketConnector();
        serverSocketConnector.addServerSocketConnectorListener(listener);

        failBucket.waitForMessages(1);
        IOException ioException = failBucket.get(0);

        if (OSUtils.isWindows()) {
            assertThat(ioException.getMessage(), containsString("JVM_Bind"));
        }
        else {
            assertThat(ioException.getMessage(), containsString("Address already in use"));
        }
        
        assertThat(serverSocketConnector.isBound(), is(false));

        // Close our rogue server
        socket.close();

        serverSocketConnector.waitUntilBound(Timeout.defaultTimeout);

        assertThat(boundBucket.size(), is(1));

        hub.close();
    }

    @Test public void test_success_broadcast_with_local_listener() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        FilteredMessageSender mockLocal = Mockito.mock(FilteredMessageSender.class);
        SocketConnection mockClientA = Mockito.mock(SocketConnection.class);
        SocketConnection mockClientB = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(mockClientA);
        hub.onNewConnection(mockClientB);
        hub.addAndSubscribeLocalListener(mockLocal);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(1));

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage logEventMessage = new LogEventMessage(event1);
        SubscriptionRequestMessage subscriptionRequestMessage = new SubscriptionRequestMessage();

        hub.onNewMessage(subscriptionRequestMessage, mockClientA);
        Mockito.verify(mockClientA, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));

        assertThat(hub.getSubscribedConnections().size(), is(2));

        hub.onNewMessage(logEventMessage, mockClientB);

        Mockito.verify(mockClientA, Mockito.times(1)).send(event1);
        Mockito.verify(mockClientB, Mockito.never()).send(event1);
        Mockito.verify(mockLocal, Mockito.times(1)).send(event1);

        hub.close();
    }

    @Test public void test_closables_called() throws IOException {
        SocketHub testHub = SocketHub.createTestHub();

        Closeable mock = Mockito.mock(Closeable.class);
        testHub.addCloseable(mock);

        testHub.start();
        testHub.stop();

        Mockito.verify(mock, Mockito.times(1)).close();
    }

    @Test public void test_create_test_hub() {
        SocketHub testHub = SocketHub.createTestHub();
        assertThat(testHub.getPort(), is(not(LoggingPorts.getSocketHubDefaultPort())));

        InetSocketAddress connectionPoint = testHub.getConnectionPoint();
        assertThat(connectionPoint.getHostName(), is("localhost"));
        assertThat(connectionPoint.getPort(), is(testHub.getPort()));
    }

    @Test public void test_success_broadcast() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(Mockito.any(ConnectedMessage.class));
        Mockito.verify(clientB, Mockito.times(1)).send(Mockito.any(ConnectedMessage.class));
        
        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage logEventMessage = new LogEventMessage(event1);
        SubscriptionRequestMessage subscriptionRequestMessage = new SubscriptionRequestMessage();

        hub.onNewMessage(subscriptionRequestMessage, clientA);
        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));

        assertThat(hub.getSubscribedConnections().size(), is(1));

        hub.onNewMessage(logEventMessage, clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        hub.close();

    }

    @Test public void test_success_broadcast_doesnt_send_to_self() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage logEventMessage = new LogEventMessage(event1);
        SubscriptionRequestMessage subscriptionRequestMessage = new SubscriptionRequestMessage();

        hub.onNewMessage(subscriptionRequestMessage, clientA);
        hub.onNewMessage(subscriptionRequestMessage, clientB);

        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));
        Mockito.verify(clientB, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));

        assertThat(hub.getSubscribedConnections().size(), is(2));

        hub.onNewMessage(logEventMessage, clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        hub.close();

    }

    @Test public void test_success_connection_closed() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage logEventMessage = new LogEventMessage(event1);
        SubscriptionRequestMessage subscriptionRequestMessage = new SubscriptionRequestMessage();

        hub.onNewMessage(subscriptionRequestMessage, clientA);
        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));

        assertThat(hub.getSubscribedConnections().size(), is(1));

        hub.onNewMessage(logEventMessage, clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        hub.onConnectionClosed(clientA, "Does it matter?");

        assertThat(hub.getConnectionsList().size(), is(1));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        hub.onNewMessage(logEventMessage, clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        hub.close();

    }

    @Test public void test_success_global_subscriptions() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        // Subscribe
        SubscriptionRequestMessage subscriptionRequestMessage = new SubscriptionRequestMessage();
        hub.onNewMessage(subscriptionRequestMessage, clientA);
        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(SubscriptionResponseMessage.class));

        assertThat(hub.getSubscribedConnections().size(), is(1));

        // Send a message in
        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage logEventMessage = new LogEventMessage(event1);
        hub.onNewMessage(logEventMessage, clientB);

        // Make sure only the subscribed connection gets the message
        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        // Unsubscribe
        UnsubscriptionRequestMessage unsubscribeRequestMessage = new UnsubscriptionRequestMessage();
        hub.onNewMessage(unsubscribeRequestMessage, clientA);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        // Make sure neither connection gets the message
        hub.onNewMessage(logEventMessage, clientB);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.never()).send(event1);

        hub.close();
    }

    @Test public void test_success_channel_subscriptions() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);
        SocketConnection clientC = Mockito.mock(SocketConnection.class);
        SocketConnection sender = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);
        hub.onNewConnection(clientC);
        hub.onNewConnection(sender);

        assertThat(hub.getConnectionsList().size(), is(4));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        // Subscribe client A globally
        EventSubscriptionRequestMessage globalSubscriptionRequestMessage = new EventSubscriptionRequestMessage();
        hub.onNewMessage(globalSubscriptionRequestMessage, clientA);
        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));
        assertThat(hub.getSubscribedConnections().size(), is(1));

        // Subscribe client B to a specific channel
        EventSubscriptionRequestMessage EventSubscriptionRequestMessage = new EventSubscriptionRequestMessage(1, true, "events/specific");
        hub.onNewMessage(EventSubscriptionRequestMessage, clientB);
        Mockito.verify(clientB, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));
        assertThat(hub.getSubscribedConnections().size(), is(1));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send a message in
        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        event1.setChannel("events/specific");
        LogEventMessage logEventMessage = new LogEventMessage(event1);

        hub.onNewMessage(logEventMessage, sender);

        // Make sure only the subscribed connection gets the message
        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.times(1)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        // Unsubscribe the global listener
        EventSubscriptionRequestMessage globalUnsubscribeRequestMessage = new EventSubscriptionRequestMessage();
        globalUnsubscribeRequestMessage.setSubscribe(false);
        hub.onNewMessage(globalUnsubscribeRequestMessage, clientA);

        assertThat(hub.getConnectionsList().size(), is(4));
        assertThat(hub.getSubscribedConnections().size(), is(0));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.times(2)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        // Unsubscribe the channel
        EventSubscriptionRequestMessage channelRequestMessage = new EventSubscriptionRequestMessage(1, false, "events/specific");
        hub.onNewMessage(channelRequestMessage, clientB);

        assertThat(hub.getConnectionsList().size(), is(4));
        assertThat(hub.getSubscribedConnections().size(), is(0));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(0));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientA, Mockito.times(1)).send(event1);
        Mockito.verify(clientB, Mockito.times(2)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        hub.close();
    }

    @Test public void test_channel_subscribe_twice() throws LoggingMessageSenderException, IOException {
        SocketHub hub = new SocketHub();

        SocketConnection clientB = Mockito.mock(SocketConnection.class);
        SocketConnection clientC = Mockito.mock(SocketConnection.class);
        SocketConnection sender = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientB);
        hub.onNewConnection(clientC);
        hub.onNewConnection(sender);

        // Subscribe client B to a specific channel
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, true, "events/specific"), clientB);
        Mockito.verify(clientB, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send a message in
        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        event1.setChannel("events/specific");
        LogEventMessage logEventMessage = new LogEventMessage(event1);

        hub.onNewMessage(logEventMessage, sender);

        // Make sure only the subscribed connection gets the message
        Mockito.verify(clientB, Mockito.times(1)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        // Subscribe to the same channel again - this shouldn't do anything
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, true, "events/specific"), clientB);
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientB, Mockito.times(2)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        // Unsubscribe the channel
        EventSubscriptionRequestMessage channelRequestMessage = new EventSubscriptionRequestMessage(1, false, "events/specific");
        hub.onNewMessage(channelRequestMessage, clientB);
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(0));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientB, Mockito.times(2)).send(event1);
        Mockito.verify(clientC, Mockito.never()).send(event1);

        hub.close();
    }

    @Test public void test_channel_unsubscribe_twice() throws LoggingMessageSenderException, IOException {
        SocketHub hub = new SocketHub();

        SocketConnection clientB = Mockito.mock(SocketConnection.class);
        SocketConnection sender = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientB);
        hub.onNewConnection(sender);

        // Subscribe client B to a specific channel
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, true, "events/specific"), clientB);
        Mockito.verify(clientB, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send a message in
        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        event1.setChannel("events/specific");
        LogEventMessage logEventMessage = new LogEventMessage(event1);

        hub.onNewMessage(logEventMessage, sender);

        // Make sure only the subscribed connection gets the message
        Mockito.verify(clientB, Mockito.times(1)).send(event1);

        // Unsubscribe the channel
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, false, "events/specific"), clientB);
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(0));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientB, Mockito.times(1)).send(event1);

        // Unsubscribe a second time
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, false, "events/specific"), clientB);
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(0));

        // Send another message in
        hub.onNewMessage(logEventMessage, sender);

        Mockito.verify(clientB, Mockito.times(1)).send(event1);

        hub.close();
    }

    @Test public void test_unsubscribe_sub_channel() throws IOException, LoggingMessageSenderException {
        // TODO : implement me?
    }

    @Test public void test_sub_channel() throws IOException, LoggingMessageSenderException {
        SocketHub hub = new SocketHub();

        assertThat(hub.getConnectionsList().size(), is(0));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SocketConnection clientA = Mockito.mock(SocketConnection.class);
        SocketConnection clientB = Mockito.mock(SocketConnection.class);
        SocketConnection sender = Mockito.mock(SocketConnection.class);

        hub.onNewConnection(clientA);
        hub.onNewConnection(clientB);
        hub.onNewConnection(sender);

        assertThat(hub.getConnectionsList().size(), is(3));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        // Subscribe client A to a top level channel and client B to a sub channel
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, true, "events/"), clientA);
        hub.onNewMessage(new EventSubscriptionRequestMessage(1, true, "events/specific"), clientB);

        Mockito.verify(clientA, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));
        Mockito.verify(clientB, Mockito.times(2)).send(Mockito.any(EventSubscriptionResponseMessage.class));

        // Make sure its subscribed to all of those channels
        assertThat(hub.getChannelSubscribedConnections("events").size(), is(1));
        assertThat(hub.getChannelSubscribedConnections("events/specific").size(), is(1));

        // Send a message in on the specific channel
        DefaultLogEvent specificEvent = LogEventBuilder.start().setChannel("events/specific").toLogEvent();
        LogEventMessage message = new LogEventMessage(specificEvent);
        hub.onNewMessage(message, sender);

        // Make sure both the subscribed connections gets the message
        Mockito.verify(clientA, Mockito.times(1)).send(specificEvent);
        Mockito.verify(clientB, Mockito.times(1)).send(specificEvent);

        // Send a message in on the general channel
        DefaultLogEvent genericEvent = LogEventBuilder.start().setChannel("events").toLogEvent();
        LogEventMessage message2 = new LogEventMessage(genericEvent);
        hub.onNewMessage(message2, sender);

        // Make sure both the subscribed connections gets the message
        Mockito.verify(clientA, Mockito.times(1)).send(genericEvent);
        Mockito.verify(clientB, Mockito.times(0)).send(genericEvent);

        hub.close();
    }

}
