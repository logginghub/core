package com.logginghub.logging.servers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.modules.ChannelSubscriptionsModule;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.module.PeerServiceDiscovery;

public class TestNewSubscriptions {

    private SocketHub hub;
    private SocketClient clientA;
    private SocketClient clientB;
    private SocketClient clientC;
    private Bucket<LoggingMessage> clientAMasterBucket;
    private Bucket<LoggingMessage> clientBMasterBucket;
    private Bucket<LoggingMessage> clientCMasterBucket;
    private ChannelSubscriptionsModule module;

    @Before public void setup() throws ConnectorException {
        hub = SocketHub.createTestHub();
        hub.start();
        hub.waitUntilBound();

        module = new ChannelSubscriptionsModule();
        ChannelSubscriptionsConfiguration configuration = new ChannelSubscriptionsConfiguration();
        LoggingContainer container = new LoggingContainer();
        container.getModules().add(hub);
        container.setExternallyConfigured(hub);

        module.configure(configuration, new PeerServiceDiscovery(container));

        clientA = hub.createClient("ClientA");
        clientB = hub.createClient("ClientB");
        clientC = hub.createClient("ClientC");

        clientA.setRespondToPings(false);
        clientB.setRespondToPings(false);
        clientC.setRespondToPings(false);

        clientA.setAutoSubscribe(false);
        clientB.setAutoSubscribe(false);
        clientC.setAutoSubscribe(false);

        clientAMasterBucket = new Bucket<LoggingMessage>();
        clientA.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                clientAMasterBucket.add(message);
            }
        });

        clientBMasterBucket = new Bucket<LoggingMessage>();
        clientB.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                clientBMasterBucket.add(message);
            }
        });

        clientCMasterBucket = new Bucket<LoggingMessage>();
        clientC.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                clientCMasterBucket.add(message);
            }
        });

        clientA.connect();
        clientB.connect();
        clientC.connect();

        // Clear the new connected messages
        clientAMasterBucket.waitForMessages(1);
        clientBMasterBucket.waitForMessages(1);
        clientCMasterBucket.waitForMessages(1);

        clientAMasterBucket.clear();
        clientBMasterBucket.clear();
        clientCMasterBucket.clear();
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

        if (clientC != null) {
            clientC.disconnect();
        }
    }

    @Test public void test_add_subscription() throws ConnectorException, LoggingMessageSenderException {

        final Bucket<LoggingMessage> clientASubscriptionBucket = new Bucket<LoggingMessage>();
        clientA.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientASubscriptionBucket.add(t);
            }
        });

        // Wait for the subscription response
        clientAMasterBucket.waitForMessages(1);
        clientAMasterBucket.clear();

        // Double check nothing has auto-subscribed
        assertThat(hub.getSubscribedConnections().size(), is(0));

        // Send a message that shouldn't end up anywhere
        clientB.send(new LogEventMessage(LogEventBuilder.start().toLogEvent()));

        // Send a message that should arrive
        clientB.send(new ChannelMessage("channel", LogEventBuilder.start().toLogEvent()));

        clientASubscriptionBucket.waitForMessages(1);

        assertThat(clientASubscriptionBucket.size(), is(1));
        assertThat(clientAMasterBucket.size(), is(1));
        assertThat(clientBMasterBucket.size(), is(0));
        assertThat(clientCMasterBucket.size(), is(0));

        clientASubscriptionBucket.clear();
        clientAMasterBucket.clear();

        // Subscribe clientC as well
        final Bucket<LoggingMessage> clientCSubscriptionBucket = new Bucket<LoggingMessage>();
        clientC.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientCSubscriptionBucket.add(t);
            }
        });

        // Wait for the subscription response
        clientCMasterBucket.waitForMessages(1);
        clientCMasterBucket.clear();

        // Send a message that should arrive
        clientB.send(new ChannelMessage("channel", LogEventBuilder.start().toLogEvent()));

        clientASubscriptionBucket.waitForMessages(1);
        clientCSubscriptionBucket.waitForMessages(1);

        assertThat(clientASubscriptionBucket.size(), is(1));
        assertThat(clientAMasterBucket.size(), is(1));
        assertThat(clientBMasterBucket.size(), is(0));
        assertThat(clientCMasterBucket.size(), is(1));

        clientASubscriptionBucket.clear();
        clientCSubscriptionBucket.clear();
        clientAMasterBucket.clear();
        clientBMasterBucket.clear();
        clientCMasterBucket.clear();

        // Subscribe clientB as well
        final Bucket<LoggingMessage> clientBSubscriptionBucket = new Bucket<LoggingMessage>();
        clientB.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientBSubscriptionBucket.add(t);
            }
        });

        // Wait for the subscription response
        clientBMasterBucket.waitForMessages(1);
        clientBMasterBucket.clear();

        // Send a message that should arrive but not get sent to clientB as it originate the message
        clientB.send(new ChannelMessage("channel", LogEventBuilder.start().toLogEvent()));

        clientASubscriptionBucket.waitForMessages(1);
        clientCSubscriptionBucket.waitForMessages(1);

        assertThat(clientASubscriptionBucket.size(), is(1));
        assertThat(clientAMasterBucket.size(), is(1));
        assertThat(clientBMasterBucket.size(), is(0));
        assertThat(clientCMasterBucket.size(), is(1));
    }

    @Test public void test_add_second_level_subscription() throws LoggingMessageSenderException {

        final Bucket<ChannelMessage> clientASubscriptionBucket = new Bucket<ChannelMessage>();
        final Bucket<ChannelMessage> clientBSubscriptionBucket = new Bucket<ChannelMessage>();

        clientA.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientASubscriptionBucket.add(t);
            }
        });

        clientB.addSubscription("channel/secondLevel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientBSubscriptionBucket.add(t);
            }
        });

        clientAMasterBucket.waitForMessages(1);
        clientBMasterBucket.waitForMessages(1);

        clientAMasterBucket.clear();
        clientBMasterBucket.clear();

        clientC.send(new ChannelMessage("channel", LogEventBuilder.start().setMessage("Message to channel").toLogEvent()));
        clientC.send(new ChannelMessage("channel/secondLevel", LogEventBuilder.start().setMessage("Message to channel/secondLevel").toLogEvent()));

        Timeout.defaultTimeout.debugMode();

        clientASubscriptionBucket.waitForMessages(2);
        clientBSubscriptionBucket.waitForMessages(1);

        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel"));
        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(1).getPayload()).getMessage(), is("Message to channel/secondLevel"));

        assertThat(((DefaultLogEvent) clientBSubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel/secondLevel"));
    }

    @Test public void test_subscriptions_are_cleared_after_disconnect() throws LoggingMessageSenderException {
        final Bucket<ChannelMessage> clientASubscriptionBucket = new Bucket<ChannelMessage>();
        final Bucket<ChannelMessage> clientBSubscriptionBucket = new Bucket<ChannelMessage>();

        clientA.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientASubscriptionBucket.add(t);
            }
        });

        clientB.addSubscription("channel/secondLevel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientBSubscriptionBucket.add(t);
            }
        });

        clientAMasterBucket.waitForMessages(1);
        clientBMasterBucket.waitForMessages(1);

        clientAMasterBucket.clear();
        clientBMasterBucket.clear();

        clientC.send(new ChannelMessage("channel", LogEventBuilder.start().setMessage("Message to channel").toLogEvent()));
        clientC.send(new ChannelMessage("channel/secondLevel", LogEventBuilder.start().setMessage("Message to channel/secondLevel").toLogEvent()));

        clientASubscriptionBucket.waitForMessages(2);
        clientBSubscriptionBucket.waitForMessages(1);

        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel"));
        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(1).getPayload()).getMessage(), is("Message to channel/secondLevel"));

        assertThat(((DefaultLogEvent) clientBSubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel/secondLevel"));

        assertThat(module.getSubscriptions().getDestinations("channel", "secondLevel").size(), is(2));
        assertThat(module.getSubscriptions().getDestinations("channel").size(), is(1));

        clientB.disconnect();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() == 2;
            }
        });

        assertThat(module.getSubscriptions().getDestinations("channel", "secondLevel").size(), is(1));
        assertThat(module.getSubscriptions().getDestinations("channel").size(), is(1));

        clientA.disconnect();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() == 1;
            }
        });

        assertThat(module.getSubscriptions().getDestinations("channel", "secondLevel").size(), is(0));
        assertThat(module.getSubscriptions().getDestinations("channel").size(), is(0));

    }

    @Test public void test_subscriptions_are_reestablished_after_reconnect() throws LoggingMessageSenderException, InterruptedException,
                    ExecutionException, TimeoutException {

        SocketClientManager managerA = new SocketClientManager(clientA);
        managerA.start();

        final Bucket<ChannelMessage> clientASubscriptionBucket = new Bucket<ChannelMessage>();

        Future<Boolean> future = clientA.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientASubscriptionBucket.add(t);
            }
        });

        future.get(Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

        clientC.send(new ChannelMessage("channel", LogEventBuilder.start().setMessage("Message to channel").toLogEvent()));

        clientASubscriptionBucket.waitForMessages(1, "10 seconds");
        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel"));
        clientASubscriptionBucket.clear();

        hub.disconnectAll();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() == 0;
            }
        });

        // Client A should reconnect automatically
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hub.getConnectionsList().size() == 1;
            }
        });

        // And the subscription should get recreated
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return module.getSubscriptions().getDestinations("channel", "secondLevel").size() == 1;
            }
        });

        // Calling send will automatically make a new connection
        clientC.send(new ChannelMessage("channel", LogEventBuilder.start().setMessage("Message to channel").toLogEvent()));

        // The message /should/ have made it back to clientA as the subscription should have been
        // re-added when it reconnectedS
        clientASubscriptionBucket.waitForMessages(1);
        assertThat(((DefaultLogEvent) clientASubscriptionBucket.get(0).getPayload()).getMessage(), is("Message to channel"));
        clientASubscriptionBucket.clear();

        managerA.stop();

    }

    @Test public void test_sender_doesnt_get_own_messages() throws ConnectorException, LoggingMessageSenderException {

        final Bucket<LoggingMessage> clientASubscriptionBucket = new Bucket<LoggingMessage>();
        final Bucket<LoggingMessage> clientCSubscriptionBucket = new Bucket<LoggingMessage>();
        final Bucket<LoggingMessage> clientBSubscriptionBucket = new Bucket<LoggingMessage>();

        clientA.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientASubscriptionBucket.add(t);
            }
        });

        clientC.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientCSubscriptionBucket.add(t);
            }
        });

        clientB.addSubscription("channel", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                clientBSubscriptionBucket.add(t);
            }
        });

        // Wait for the subscription response
        clientAMasterBucket.waitForMessages(1);
        clientBMasterBucket.waitForMessages(1);
        clientCMasterBucket.waitForMessages(1);

        clientAMasterBucket.clear();
        clientBMasterBucket.clear();
        clientCMasterBucket.clear();

        // Send a message that should arrive but not get sent to clientB as it originate the message
        clientB.send(new ChannelMessage("channel", LogEventBuilder.start().toLogEvent()));

        clientASubscriptionBucket.waitForMessages(1);
        clientCSubscriptionBucket.waitForMessages(1);

        assertThat(clientASubscriptionBucket.size(), is(1));
        assertThat(clientAMasterBucket.size(), is(1));
        assertThat(clientBMasterBucket.size(), is(0));
        assertThat(clientCMasterBucket.size(), is(1));
    }

}
