package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;

public class TestInMemoryHistoryWithRealHub {

    private SocketHub hub;
    private SocketClient clientA;
    private SocketClient clientB;
    private SocketClient clientC;
    private MutableInt clientBCounter = new MutableInt(0);
    private Bucket<LoggingMessage> clientAMasterBucket;

    private Bucket<LoggingMessage> clientCMasterBucket;
    private InMemoryHistoryModule module;
    

    @Before public void setup() throws ConnectorException {
        hub = SocketHub.createTestHub();
        hub.setMaximumClientSendQueueSize(100000);
        hub.start();

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
        module.start();

        clientA = hub.createClient("ClientA");
        clientB = hub.createClient("ClientB");
        clientC = hub.createClient("ClientC");

        clientA.setAutoSubscribe(false);
        clientB.setAutoSubscribe(false);
        clientC.setAutoSubscribe(false);

        clientA.connect();
        clientB.connect();
        clientC.connect();

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

        clientCMasterBucket = new Bucket<LoggingMessage>();
        clientC.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                clientCMasterBucket.add(message);
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

        if (clientC != null) {
            clientC.disconnect();
        }
        
        module.stop();
    }

    @Ignore
    @Test public void test_one_million_entries() throws LoggingMessageSenderException {

        clientB.subscribe();

        clientA.getConnector().setWriteQueueOverflowPolicy(SlowSendingPolicy.block);

        clientBCounter.reset();

        final int amount = 1000000;        
        for (int i = 0; i < amount; i++) {
            try {
                LogEvent event = LogEventBuilder.create(0, Logger.info, "Message " + i);
                clientA.send(new LogEventMessage(event));
            }
            catch (LoggingMessageSenderException e) {
                e.printStackTrace();
            }
        }

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return clientBCounter.value == amount;
            }
        });

        assertThat(clientBCounter.value, is(amount));

        HistoricalDataRequest request = new HistoricalDataRequest();
        request.setStart(0);
        request.setEnd(1000);

        final MutableInt value = new MutableInt(0);
        clientB.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                HistoricalDataResponse response = (HistoricalDataResponse) message;
                DefaultLogEvent[] events = response.getEvents();
                // System.out.println("=====================");
                // for (DefaultLogEvent defaultLogEvent : events) {
                // System.out.println(defaultLogEvent);
                // }
                // System.out.println("=====================");
                value.value += events.length;
                // System.out.println(value);
            }
        });

        clientB.send(request);

        ThreadUtils.untilTrue("Events didn't arrive fast enough", 30000, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return value.value >= amount;

            }
        });

        assertThat(value.value, is(amount));

    }

    @Test public void test_index_updates() throws LoggingMessageSenderException, InterruptedException, ExecutionException, TimeoutException {
        
        clientB.subscribe();

        clientA.getConnector().setWriteQueueOverflowPolicy(SlowSendingPolicy.block);

        clientBCounter.reset();

        final MutableInt counter = new MutableInt(0);
        Future<Boolean> subscription = clientC.addSubscription(Channels.historyUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                SerialisableObject payload = t.getPayload();
                HistoricalIndexResponse response = (HistoricalIndexResponse)payload;
                counter.increment(response.getElements().length);
            }
        });
        
        subscription.get(Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

        final int amount = 10000;        
        for (int i = 0; i < amount; i++) {
            try {
                LogEvent event = LogEventBuilder.create(i * 10, Logger.info, "Message " + i);
                clientA.send(new LogEventMessage(event));
            }
            catch (LoggingMessageSenderException e) {
                e.printStackTrace();
            }
        }
        

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return clientBCounter.value == amount;
            }
        });

        
        assertThat(clientBCounter.value, is(amount));

        final int expectedIndexes = (amount / 100) -1;
        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return counter.value == expectedIndexes;
            }
        });

        
    }
    
    
}
