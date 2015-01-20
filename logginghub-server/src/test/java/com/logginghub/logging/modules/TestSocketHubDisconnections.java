package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.logging.messaging.SocketConnectorListener;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.PeerServiceDiscovery;

public class TestSocketHubDisconnections {

    private SocketHub hub;
    private SocketClient clientA;
    private SocketClient clientB;
    private MutableInt clientBCounter = new MutableInt(0);
    private Bucket<LoggingMessage> clientAMasterBucket;

    private Bucket<LoggingMessage> clientCMasterBucket;
    private InMemoryHistoryModule module;

    @Before public void setup() throws ConnectorException {
        hub = SocketHub.createTestHub();
        hub.setMaximumClientSendQueueSize(10000);
        hub.start();
        hub.waitUntilBound();       

        module = new InMemoryHistoryModule();

        // Build a container
        LoggingContainer container = new LoggingContainer();
        container.getModules().add(hub);
        container.setExternallyConfigured(hub);
        
        PeerServiceDiscovery discovery = new PeerServiceDiscovery(container);

        // Add the channels module
        ChannelSubscriptionsModule channelSubscriptionsModule = new ChannelSubscriptionsModule();
        channelSubscriptionsModule.configure(new ChannelSubscriptionsConfiguration(), discovery);
        container.getModules().add(channelSubscriptionsModule);

        // And the in memory history module
        InMemoryHistoryConfiguration configuration = new InMemoryHistoryConfiguration();
        module.configure(configuration, discovery);

        clientA = hub.createClient("ClientA");
        clientB = hub.createClient("ClientB");

        clientA.setAutoSubscribe(false);
        clientB.setAutoSubscribe(false);

        clientA.connect();

        ThreadUtils.sleep(500);

        clientB.connect();

        clientAMasterBucket = new Bucket<LoggingMessage>();
        clientA.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                System.out.println("ClientABucket " + message);
                clientAMasterBucket.add(message);
            }
        });

        clientB.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                clientBCounter.increment();
            }
        });

    }

    @After public void teardown() {
        if (hub != null) {
            hub.stop();
        }

        if (clientA != null) {
            clientA.disconnect();
        }

        if (clientB != null) {
            clientB.disconnect();
        }

    }

    @Test public void test_write_queue_full() throws LoggingMessageSenderException, IOException {

        // Hook clientB up to receive
        clientB.subscribe();

        // Set clientA to not disconnect or throw stuff away so we can write at maximum speed
        clientA.getConnector().setWriteQueueOverflowPolicy(SlowSendingPolicy.block);

        clientBCounter.reset();

        final CountDownLatch latch = new CountDownLatch(1);

        // Attach a listener to clientB that makes it read slowly
        clientB.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // hub.getConnectionsList().get(1).setDebug(true);
        // clientB.setDebug(true);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(1));

        final Bucket<String> disconnectionReasons = new Bucket<String>();
        clientB.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
            @Override public void onConnectionLost(String reason) {
                disconnectionReasons.add(reason);
            }

            @Override public void onConnectionEstablished() {}
        });

        final int amount = 50000;
        for (int i = 0; i < amount; i++) {
            try {
                LogEvent event = LogEventBuilder.create(0, Logger.info, "Message " + i);
                clientA.send(new LogEventMessage(event));
            }
            catch (LoggingMessageSenderException e) {
                e.printStackTrace();
            }
        }

        // Wait a bit for the hub to have processed enough of the events to fill the write buffer
        ThreadUtils.sleep(500);

        // We need to let the reader thread back in so it can process the remainder of its
        // readbuffer, in order to go back to the socket and call read again - at that point the
        // thread will be notified of the socket being closed.
        latch.countDown();

        // sleepTime.value = 0;

        disconnectionReasons.waitForMessages(1, "100 seconds");

        // clientB should have definitely been disconnected
        ThreadUtils.untilTrue("ClientB was still in the hub connections list", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() < 2;
            }
        });

        ThreadUtils.untilTrue("ClientB was still subscribed in the hub", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getSubscribedConnections().size() == 0;
            }
        });

        ThreadUtils.untilTrue("ClientB wasn't cleanly disconnected", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return !clientB.isConnected();
            }
        });

    }

    @Test public void test_write_queue_full_with_reconnect() throws LoggingMessageSenderException, IOException {

        SocketClientManager manager = new SocketClientManager(clientB);
        manager.start();

        // Hook clientB up to receive
        clientB.subscribe();

        // Set clientA to not disconnect or throw stuff away so we can write at maximum speed
        clientA.getConnector().setWriteQueueOverflowPolicy(SlowSendingPolicy.block);

        clientBCounter.reset();
        
        final CountDownLatch latch = new CountDownLatch(1);

        // Attach a listener to clientB that makes it read slowly
        clientB.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // hub.getConnectionsList().get(1).setDebug(true);
        // clientB.setDebug(true);

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(1));

        final Bucket<String> disconnectionReasons = new Bucket<String>();
        clientB.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
            @Override public void onConnectionLost(String reason) {
                disconnectionReasons.add(reason);
            }

            @Override public void onConnectionEstablished() {}
        });

        final int amount = 50000;
        for (int i = 0; i < amount; i++) {
            try {
                LogEvent event = LogEventBuilder.create(0, Logger.info, "Message " + i);
                clientA.send(new LogEventMessage(event));
            }
            catch (LoggingMessageSenderException e) {
                e.printStackTrace();
            }
        }
        
        // Wait a bit for the hub to have processed enough of the events to fill the write buffer
        ThreadUtils.sleep(500);

        // We need to let the reader thread back in so it can process the remainder of its
        // readbuffer, in order to go back to the socket and call read again - at that point the
        // thread will be notified of the socket being closed.
        latch.countDown();

        disconnectionReasons.waitForMessages(1, "10 seconds");

        // clientB should have definitely been disconnected
        ThreadUtils.untilTrue("ClientB was still in the hub connections list", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() < 2;
            }
        });

        ThreadUtils.untilTrue("ClientB was still subscribed in the hub", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getSubscribedConnections().size() == 0;
            }
        });

        ThreadUtils.untilTrue("ClientB wasn't cleanly disconnected", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return !clientB.isConnected();
            }
        });

        // jshaw - this code is nowhere near safe enough - the reconnect looks like its happening on
        // during the notification of the close, which means it might actually reconnect much sooner
        // than we expect

        // The socket client manager should now reconnect
        ThreadUtils.untilTrue("ClientB didn't reconnect", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() == 2;
            }
        });

        manager.stop();
    }

    @Test public void test_manually_disconnect() throws LoggingMessageSenderException, IOException {

        // Hook clientB up to receive
        clientB.subscribe();

        assertThat(hub.getConnectionsList().size(), is(2));
        assertThat(hub.getSubscribedConnections().size(), is(1));

        final Bucket<String> disconnectionReasons = new Bucket<String>();
        clientB.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
            @Override public void onConnectionLost(String reason) {
                System.out.println(reason);
                disconnectionReasons.add(reason);
            }

            @Override public void onConnectionEstablished() {}
        });

        hub.disconnectAll();

        disconnectionReasons.waitForMessages(1);

        // clientB should have definitely been disconnected
        ThreadUtils.untilTrue("ClientB was still in the hub connections list", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() < 2;
            }
        });

        ThreadUtils.untilTrue("ClientB was still subscribed in the hub", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getSubscribedConnections().size() == 0;
            }
        });

        ThreadUtils.untilTrue("ClientB wasn't cleanly disconnected", 5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return !clientB.isConnected();
            }
        });

    }

}
