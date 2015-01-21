package com.logginghub.logging.modules;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ConnectionTypeMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectorListener;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by james on 10/11/14.
 */
public class TestHighAvailability extends BaseHub {

//    @BeforeTest
    public void setup() {
        setupHubFixture();
    }

//    @org.testng.annotations.Test
    @org.junit.Test
    public void test_connection_info() throws ConnectorException, InterruptedException {
        HubTestFixture.HubFixture hubFixtureA = fixture.createSocketHub();

        SocketHub hub = hubFixtureA.start();

        final SocketClient consumer = fixture.createDisconnectedClient("Client", hub);

        final CountDownLatch latch = new CountDownLatch(1);

        consumer.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
            @Override
            public void onConnectionEstablished() {

                try {
                    consumer.sendBlocking(new ConnectionTypeMessage("client connection",
                                                                    SocketConnection.CONNECTION_TYPE_HUB_BRIDGE));
                    consumer.sendBlocking(new SubscriptionRequestMessage());

                    System.out.println("Subscription request done");

                    latch.countDown();

                } catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionLost(String reason) {

            }
        });

        consumer.connect();

        latch.await(Timeout.defaultTimeout.getMillis(), TimeUnit.MILLISECONDS);

        assertThat(hub.getConnectionsList().size(), is(1));
        assertThat(hub.getConnectionsList().get(0).getConnectionDescription(), is("client connection"));

    }

//    @org.testng.annotations.Test(invocationCount = 10, threadPoolSize = 5)
    @org.junit.Test
    public void test_client_failover() throws ConnectorException, LoggingMessageSenderException {
        // 1. Start two hubs
        HubTestFixture.HubFixture hubFixtureA = fixture.createSocketHub();
        HubTestFixture.HubFixture hubFixtureB = fixture.createSocketHub();

        SocketHub hubA = hubFixtureA.start();
        SocketHub hubB = hubFixtureB.start();

        // 2. Simulate an aggregator by creating two socket client consumers connected to each
        SocketClient consumerA = fixture.createClientAutoSubscribe("ConsumerA", hubA);
        SocketClient consumerB = fixture.createClientAutoSubscribe("ConsumerB", hubB);

        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(consumerA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(consumerB);

        // 3. Create another socket client to simulate the producer
        SocketClient producer = new SocketClient("Producer");
        ConnectionPointManager connectionPointManager = producer.getConnector().getConnectionPointManager();
        connectionPointManager.clearConnectionPoints();
        connectionPointManager.addConnectionPoint(hubA.getConnectionPoint());
        connectionPointManager.addConnectionPoint(hubB.getConnectionPoint());
        producer.connect();

        // Verify it connects to the first one in the list first
        assertThat(producer.getConnector().getCurrentConnection().getSocket().getPort(), is(hubA.getPort()));

        // 4. Send an event, producer should connect to [first | second | random choice? ]
        fixture.sendEvent(producer, "Test event 1");

        // 5. Verify one consumers received it, the other one didn't
        eventsA.waitForMessages(1);
        ThreadUtils.sleep(1000);

        assertThat(eventsA.size(), is(1));
        assertThat(eventsA.get(0).getMessage(), is("Test event 1"));
        assertThat(eventsB.size(), is(0));

        // 6. Shutdown the hub the producer is connected to
        hubA.stop();

        // 7. Send another event, producer should connect to the other hub
        fixture.sendEvent(producer, "Test event 2");

        // Verify it connects to the other hub
        assertThat(producer.getConnector().getCurrentConnection().getSocket().getPort(), is(hubB.getPort()));

        // 8. Verify one consumer received it, the other one didn't
        eventsB.waitForMessages(1);
        ThreadUtils.sleep(1000);

        assertThat(eventsA.size(), is(1));
        assertThat(eventsA.get(0).getMessage(), is("Test event 1"));

        assertThat(eventsB.size(), is(1));
        assertThat(eventsB.get(0).getMessage(), is("Test event 2"));
    }

//    @org.testng.annotations.Test(invocationCount = 10, threadPoolSize = 5)
    @org.junit.Test
    public void test_ha_replication() throws ConnectorException, LoggingMessageSenderException {
        // 1. Start two hubs - configured to replicate to each other
        HubTestFixture.HubFixture hubFixtureA = fixture.createSocketHub(HubTestFixture.Features.Bridge);
        HubTestFixture.HubFixture hubFixtureB = fixture.createSocketHub(HubTestFixture.Features.Bridge);

        hubFixtureA.getSocketHubConfiguration().setOutputStats(true);
        hubFixtureB.getSocketHubConfiguration().setOutputStats(true);

        hubFixtureA.getSocketHubConfiguration().setStatsInterval("1 second");
        hubFixtureB.getSocketHubConfiguration().setStatsInterval("1 second");

        hubFixtureA.getLoggingBridgeConfiguration().setHost("localhost");
        hubFixtureB.getLoggingBridgeConfiguration().setHost("localhost");

        hubFixtureA.getLoggingBridgeConfiguration().setPort(hubFixtureB.getSocketHubConfiguration().getPort());
        hubFixtureB.getLoggingBridgeConfiguration().setPort(hubFixtureA.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubFixtureA.start();
        final SocketHub hubB = hubFixtureB.start();

        // 2. Create a socket client consumer for both hubs
        SocketClient consumerA = fixture.createClientAutoSubscribe("ConsumerA", hubA);
        SocketClient consumerB = fixture.createClientAutoSubscribe("ConsumerB", hubB);

        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(consumerA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(consumerB);

        // 3. Create another socket client to simulate the producer
        SocketClient producer = new SocketClient("Producer");
        ConnectionPointManager connectionPointManager = producer.getConnector().getConnectionPointManager();
        connectionPointManager.clearConnectionPoints();
        connectionPointManager.addConnectionPoint(hubA.getConnectionPoint());
        connectionPointManager.addConnectionPoint(hubB.getConnectionPoint());
        producer.connect();

        // Verify it connects to the first one in the list first
        assertThat(producer.getConnector().getCurrentConnection().getSocket().getPort(), is(hubA.getPort()));

        // Make sure everyone is connected and subscribed
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 3 && hubB.getConnectionsList().size() == 2;
            }
        });
        assertThat(hubA.getConnectionsList().size(), is(3));
        assertThat(hubB.getConnectionsList().size(), is(2));

        // 4. Send an event, producer should connect to [first | second | random choice? ]
        fixture.sendEvent(producer, "Test event 1");

        // 5. Verify both consumers received it
        eventsA.waitForMessages(1);
        eventsB.waitForMessages(1);

        // Make sure there are no repeated messages
        ThreadUtils.sleep(1000);

        assertThat(eventsA.size(), is(1));
        assertThat(eventsB.size(), is(1));

        assertThat(eventsA.get(0).getMessage(), is("Test event 1"));
        assertThat(eventsB.get(0).getMessage(), is("Test event 1"));

    }

//    @org.testng.annotations.Test(invocationCount = 10, threadPoolSize = 5)
    @org.junit.Test
    public void test_ha_log_files() throws ConnectorException, LoggingMessageSenderException {
        // 1. Start two hubs - configured to replicate to each other
        HubTestFixture.HubFixture hubFixtureA = fixture.createSocketHub(HubTestFixture.Features.Bridge,
                                                                        HubTestFixture.Features.TimestampVariableRollingFileLogger);
        HubTestFixture.HubFixture hubFixtureB = fixture.createSocketHub(HubTestFixture.Features.Bridge,
                                                                        HubTestFixture.Features.TimestampVariableRollingFileLogger);

        final File folder = FileUtils.createRandomTestFolderForClass(TestHighAvailability.class);

        hubFixtureA.getTimestampVariableRollingFileLoggerConfiguration().setForceFlush(true);
        hubFixtureB.getTimestampVariableRollingFileLoggerConfiguration().setForceFlush(true);

        hubFixtureA.getTimestampVariableRollingFileLoggerConfiguration().setFilename("hubA");
        hubFixtureB.getTimestampVariableRollingFileLoggerConfiguration().setFilename("hubB");

        hubFixtureA.getTimestampVariableRollingFileLoggerConfiguration().setFolder(folder.getAbsolutePath());
        hubFixtureB.getTimestampVariableRollingFileLoggerConfiguration().setFolder(folder.getAbsolutePath());

        hubFixtureA.getSocketHubConfiguration().setOutputStats(true);
        hubFixtureB.getSocketHubConfiguration().setOutputStats(true);

        hubFixtureA.getSocketHubConfiguration().setStatsInterval("1 second");
        hubFixtureB.getSocketHubConfiguration().setStatsInterval("1 second");

        hubFixtureA.getLoggingBridgeConfiguration().setHost("localhost");
        hubFixtureB.getLoggingBridgeConfiguration().setHost("localhost");

        hubFixtureA.getLoggingBridgeConfiguration().setPort(hubFixtureB.getSocketHubConfiguration().getPort());
        hubFixtureB.getLoggingBridgeConfiguration().setPort(hubFixtureA.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubFixtureA.start();
        final SocketHub hubB = hubFixtureB.start();

        // Wait until both sides are connected
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1 && hubB.getConnectionsList().size() == 1;
            }
        });

        // 2. Create another socket client to simulate the producer
        SocketClient producer = new SocketClient("Producer");
        ConnectionPointManager connectionPointManager = producer.getConnector().getConnectionPointManager();
        connectionPointManager.clearConnectionPoints();
        connectionPointManager.addConnectionPoint(hubA.getConnectionPoint());
        connectionPointManager.addConnectionPoint(hubB.getConnectionPoint());
        producer.connect();

        // 3. Send an event
        fixture.sendEvent(producer, "Test event 1");

        //        ThreadUtils.sleep(100000);

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return folder.listFiles().length == 2;
            }
        });

        // 4. Verify both hub have it in their log files
        File[] files = folder.listFiles();
        System.out.println(files[0].getAbsolutePath());

        assertThat(files.length, is(2));

        assertThat(FileUtils.readAsStringArray(files[0]).length, is(1));
        assertThat(FileUtils.readAsStringArray(files[1]).length, is(1));

        assertThat(FileUtils.readAsStringArray(files[0])[0], containsString("Test event 1"));
        assertThat(FileUtils.readAsStringArray(files[1])[0], containsString("Test event 1"));
    }


}

