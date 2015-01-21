package com.logginghub.integrationtests.logging;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.messages.*;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.logging.modules.ChannelSubscriptionsModule;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.*;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class TestSocketClient {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SocketHub hub;
    private SocketClient client;

    private DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
    private DefaultLogEvent event2 = LogEventFactory.createFullLogEvent1();
    private DefaultLogEvent event3 = LogEventFactory.createFullLogEvent1();

    private LogEventMessage message1 = new LogEventMessage(event1);
    private LogEventMessage message2 = new LogEventMessage(event2);
    private LogEventMessage message3 = new LogEventMessage(event3);

    @After
    public void cleanup() {
        FileUtils.closeQuietly(hub, client);
    }

    @Test
    public void test_connect_with_hub() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());
        client.connect();
        assertThat(client.isConnected(), is(true));

        assertThat(client.getConnectionID(), is(0));

    }

    @Test
    public void test_connect_without_hub() throws IOException, ConnectorException {

        client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress("localhost", NetUtils.findFreePort()));

        try {
            client.connect();
            fail("Connect should have failed");
        } catch (ConnectorException ce) {
            assertThat(ce.getMessage(),
                       containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(client.isConnected(), is(false));
    }

    @Test
    public void test_connect_when_hub_comes_back() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();

        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());

        try {
            client.connect();
            fail("Connect should have failed");
        } catch (ConnectorException ce) {
            assertThat(ce.getMessage(),
                       containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(client.isConnected(), is(false));

        hub.start();
        hub.waitUntilBound();
        client.connect();
        assertThat(client.isConnected(), is(true));
    }

    @Test
    public void test_subscription_when_disconnected() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();

        // TODO : we need to move this out into a separate class that builds a "standard" hub
        // configuration with all the modules
        final ChannelSubscriptionsModule channelSubscriptionsModule = new ChannelSubscriptionsModule();
        ConfigurableServiceDiscovery disco = new ConfigurableServiceDiscovery();
        disco.bind(SocketHubInterface.class, hub);
        channelSubscriptionsModule.configure(new ChannelSubscriptionsConfiguration(), disco);
        channelSubscriptionsModule.start();
        // end hack

        client = new SocketClient();
        client.setDebug(true);
        client.addConnectionPoint(hub.getConnectionPoint());

        try {
            client.connect();
            fail("Connect should have failed");
        } catch (ConnectorException ce) {
            assertThat(ce.getMessage(),
                       containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(client.isConnected(), is(false));

        client.subscribe("channel", new Destination<ChannelMessage>() {
            @Override
            public void send(ChannelMessage t) {

            }
        });

        hub.start();
        hub.waitUntilBound();
        client.connect();
        assertThat(client.isConnected(), is(true));

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return channelSubscriptionsModule.getSubscriptions().getDestinations("channel").size() == 1;
            }
        });

    }

    @Test
    public void test_connect_when_hub_goes_down() throws IOException, ConnectorException {

        // Fire up the hub
        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        // Connect the client
        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());
        client.connect();
        assertThat(client.isConnected(), is(true));

        // Kill the hub and make sure the client gets disconnected (possible
        // race condition?)
        hub.stop();

        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !client.isConnected();
            }
        });
        assertThat(client.isConnected(), is(false));

        // Attempt to connect whilst its down
        try {
            client.connect();
            fail("Connect should have failed");
        } catch (ConnectorException ce) {
            assertThat(ce.getMessage(),
                       containsString("failed to establish a connection with any of the 1 connection points"));
        }
        assertThat(client.isConnected(), is(false));

        // Restart and successfully connect
        hub.start();
        hub.waitUntilBound();

        client.connect();
        assertThat(client.isConnected(), is(true));
    }

    @Test
    public void test_connect_with_autosubcribe_on() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.setAutoSubscribe(true);
        client.addConnectionPoint(hub.getConnectionPoint());
        client.connect();
        LogEventBucket bucket = new LogEventBucket();
        client.addLogEventListener(bucket);
        assertThat(client.isConnected(), is(true));

        List<FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
        assertThat(subscribedConnections.size(), is(1));

        SocketConnection source = Mockito.mock(SocketConnection.class);
        hub.onNewMessage(message1, source);

        bucket.waitForMessages(1);
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getMessage(), is(event1.getMessage()));

    }

    @Test
    public void test_connect_with_autosubcribe_off() throws IOException, ConnectorException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.setAutoSubscribe(false);
        client.addConnectionPoint(hub.getConnectionPoint());
        client.connect();
        LogEventBucket bucket = new LogEventBucket();
        client.addLogEventListener(bucket);
        assertThat(client.isConnected(), is(true));

        List<FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
        assertThat(subscribedConnections.size(), is(0));

        SocketConnection source = Mockito.mock(SocketConnection.class);
        hub.onNewMessage(message1, source);

        ThreadUtils.sleep(1000);
        assertThat(bucket.size(), is(0));
    }

    @Test
    public void test_connect_and_subscribe() throws IOException, ConnectorException, LoggingMessageSenderException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.setAutoSubscribe(false);
        client.addConnectionPoint(hub.getConnectionPoint());
        client.connect();
        LogEventBucket bucket = new LogEventBucket();
        client.addLogEventListener(bucket);
        assertThat(client.isConnected(), is(true));

        // We are not subscribed at this point
        List<FilteredMessageSender> subscribedConnections = hub.getSubscribedConnections();
        assertThat(subscribedConnections.size(), is(0));

        SocketConnection source = Mockito.mock(SocketConnection.class);
        hub.onNewMessage(message1, source);

        ThreadUtils.sleep(1000);
        assertThat(bucket.size(), is(0));

        // Now subscribe
        client.subscribe();

        assertThat(subscribedConnections.size(), is(1));
        hub.onNewMessage(message1, source);

        bucket.waitForMessages(1);
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getMessage(), is(event1.getMessage()));
        bucket.clear();

        // Unsubscribe again
        client.unsubscribe();

        assertThat(subscribedConnections.size(), is(0));
        hub.onNewMessage(message1, source);

        ThreadUtils.sleep(1000);
        assertThat(bucket.size(), is(0));
    }

    @Ignore/*
            * cant get the queue to disconnect reliably, might be very ill concieved as the hub
            * isn't actually stuck
            */
    @Test
    public void test_write_queue_overflow_with_disconnect_policy() throws
                                                                   IOException,
                                                                   ConnectorException,
                                                                   LoggingMessageSenderException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());
        client.setWriteQueueMaximumSize(2);
        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.disconnect);
        client.connect();
        assertThat(client.isConnected(), is(true));

        // Block up the hub
        final Lock lock = new ReentrantLock();

        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
            public void send(LoggingMessage message) throws LoggingMessageSenderException {
                hubBucket.add(message);
                try {
                    lock.lockInterruptibly();
                    lock.unlock();
                } catch (InterruptedException e) {
                }
            }

            @Override
            public int getLevelFilter() {
                return Level.ALL.intValue();
            }

            @Override
            public int getConnectionType() {
                return 0;
            }

            @Override
            public void send(LogEvent t) {
                try {
                    send(new LogEventMessage(t));
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }
        });

        // Freeze the hub
        lock.lock();

        // Send some messages
        client.send(message1);
        client.send(message2);
        assertThat(client.isConnected(), is(true));
        client.flush();

        // This one should tip us over the edge
        client.send(message3);
        assertThat(client.isConnected(), is(false));
    }

    @Test
    public void test_write_queue_overflow_with_discard_policy() throws
                                                                IOException,
                                                                ConnectorException,
                                                                LoggingMessageSenderException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());
        client.setWriteQueueMaximumSize(2);
        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.discard);
        client.connect();

        assertThat(client.isConnected(), is(true));

        // Block up the hub
        final Lock lock = new ReentrantLock();

        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
            public void send(LoggingMessage message) throws LoggingMessageSenderException {
                hubBucket.add(message);
                try {
                    lock.lockInterruptibly();
                    lock.unlock();
                } catch (InterruptedException e) {
                }
            }

            @Override
            public int getLevelFilter() {
                return Level.ALL.intValue();
            }

            @Override
            public int getConnectionType() {
                return 0;
            }

            @Override
            public void send(LogEvent t) {
                try {
                    send(new LogEventMessage(t));
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }
        });

        // Freeze the hub
        lock.lock();

        // Send some messages
        client.send(message1);
        client.send(message2);
        assertThat(client.isConnected(), is(true));
        client.flush();

        // This one should tip us over the edge
        client.send(message3);
        assertThat(client.isConnected(), is(true));

        // Unlock the hub
        lock.unlock();

        hubBucket.waitForMessages(2);
        assertThat(hubBucket.size(), is(2));
        assertThat(hubBucket.get(0), is(instanceOf(LogEventMessage.class)));
        assertThat(hubBucket.get(1), is(instanceOf(LogEventMessage.class)));

        // We should have discarded event2 as it was the earliest one on the
        // queue when we blocked
        assertThat(((LogEventMessage) hubBucket.get(0)).getLogEvent().getMessage(), is(event1.getMessage()));
        assertThat(((LogEventMessage) hubBucket.get(1)).getLogEvent().getMessage(), is(event3.getMessage()));
    }

    @Test
    public void test_write_queue_overflow_with_blocking_policy() throws
                                                                 IOException,
                                                                 ConnectorException,
                                                                 LoggingMessageSenderException {

        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        client = new SocketClient();
        client.addConnectionPoint(hub.getConnectionPoint());
        client.setWriteQueueMaximumSize(2);
        client.setWriteQueueOverflowPolicy(SlowSendingPolicy.block);
        client.connect();

        assertThat(client.isConnected(), is(true));

        // Block up the hub
        final Lock lock = new ReentrantLock();

        final Bucket<LoggingMessage> hubBucket = new Bucket<LoggingMessage>();
        hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
            public void send(LoggingMessage message) throws LoggingMessageSenderException {
                hubBucket.add(message);
                try {
                    lock.lockInterruptibly();
                    lock.unlock();
                } catch (InterruptedException e) {
                }
            }

            @Override
            public int getLevelFilter() {
                return Level.ALL.intValue();
            }

            @Override
            public int getConnectionType() {
                return 0;
            }

            @Override
            public void send(LogEvent t) {
                try {
                    send(new LogEventMessage(t));
                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }
        });

        // Freeze the hub
        lock.lock();

        // Send some messages
        client.send(message1);
        client.send(message2);
        assertThat(client.isConnected(), is(true));
        client.flush();

        // As the send method is going to block, we need to fire it off in
        // another thread
        Future<Boolean> future = ThreadUtils.execute(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                client.send(message3);
                return true;
            }
        });

        // Make sure it blocked
        assertThat(future.isDone(), is(false));

        // Unlock the hub
        lock.unlock();

        hubBucket.waitForMessages(3);
        assertThat(hubBucket.size(), is(3));
        assertThat(hubBucket.get(0), is(instanceOf(LogEventMessage.class)));
        assertThat(hubBucket.get(1), is(instanceOf(LogEventMessage.class)));
        assertThat(hubBucket.get(2), is(instanceOf(LogEventMessage.class)));

        // Double check the send unblocked correctly
        assertThat(future.isDone(), is(true));

        // We should have discarded event2 as it was the earliest one on the
        // queue when we blocked
        assertThat(((LogEventMessage) hubBucket.get(0)).getLogEvent().getMessage(), is(event1.getMessage()));
        assertThat(((LogEventMessage) hubBucket.get(1)).getLogEvent().getMessage(), is(event2.getMessage()));
        assertThat(((LogEventMessage) hubBucket.get(2)).getLogEvent().getMessage(), is(event3.getMessage()));
    }


    @Test
    public void test_blocking_subscription() throws
                                             IOException,
                                             ConnectorException,
                                             LoggingMessageSenderException,
                                             TimeoutException,
                                             InterruptedException {
        HubTestFixture fixture = new HubTestFixture();

        HubTestFixture.HubFixture hubFixture = fixture.createSocketHub();
        hubFixture.start();

        SocketHub hub = hubFixture.getHub();

        final SocketClient client = fixture.createDisconnectedClient("client", hub);

        assertThat(hub.getConnectionsList().size(), is(0));

        client.setAutoSubscribe(false);
        client.setAutoGlobalSubscription(false);

        client.connect();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return client.isConnected();
            }
        });

        assertThat(hub.getConnectionsList().size(), is(1));
        assertThat(hub.getSubscribedConnections().size(), is(0));

        SubscriptionResponseMessage subscriptionResponseMessage = client.sendBlocking(new SubscriptionRequestMessage());
        assertThat(subscriptionResponseMessage, is(not(nullValue())));

        assertThat(hub.getSubscribedConnections().size(), is(1));

        fixture.stop();
    }

    @Test
    public void test_blocking_connnection_info() throws
                                                 IOException,
                                                 ConnectorException,
                                                 LoggingMessageSenderException,
                                                 TimeoutException,
                                                 InterruptedException {
        HubTestFixture fixture = new HubTestFixture();

        HubTestFixture.HubFixture hubFixture = fixture.createSocketHub();
        hubFixture.start();

        SocketHub hub = hubFixture.getHub();

        final SocketClient client = fixture.createDisconnectedClient("client", hub);

        assertThat(hub.getConnectionsList().size(), is(0));

        client.setAutoSubscribe(false);
        client.setAutoGlobalSubscription(false);

        client.connect();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return client.isConnected();
            }
        });

        assertThat(hub.getConnectionsList().size(), is(1));
        assertThat(hub.getConnectionsList().get(0).getConnectionType(), is(SocketConnection.CONNECTION_TYPE_NORMAL));
        assertThat(hub.getConnectionsList().get(0).getConnectionDescription(), is(""));

        RequestResponseMessage responseMessage = client.sendBlocking(new ConnectionTypeMessage("Hub bridge connection",
                                                                                               SocketConnection.CONNECTION_TYPE_HUB_BRIDGE));

        assertThat(responseMessage, is(not(nullValue())));

        assertThat(hub.getConnectionsList().get(0).getConnectionType(),
                   is(SocketConnection.CONNECTION_TYPE_HUB_BRIDGE));
        assertThat(hub.getConnectionsList().get(0).getConnectionDescription(), is("Hub bridge connection"));

        fixture.stop();
    }


    @Test
    public void test_cant_double_connect() throws
                                           IOException,
                                           ConnectorException,
                                           LoggingMessageSenderException,
                                           TimeoutException,
                                           InterruptedException {

        HubTestFixture fixture = new HubTestFixture();

        HubTestFixture.HubFixture hubFixture = fixture.createSocketHub();
        hubFixture.start();

        SocketHub hub = hubFixture.getHub();

        final SocketClient client = fixture.createDisconnectedClient("client", hub);

        client.connect();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return client.isConnected();
            }
        });

        assertThat(hub.getConnectionsList().size(), is(1));

        client.connect();

        ThreadUtils.sleep(1000);

        assertThat(hub.getConnectionsList().size(), is(1));
    }
}


