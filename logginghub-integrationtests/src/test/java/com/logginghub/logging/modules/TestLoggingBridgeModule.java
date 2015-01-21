package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.logging.Logger;

// TODO : fix the race conditions in the socket connector that mean we leak threads
//@RunWith(CustomRunner.class) 
public class TestLoggingBridgeModule extends BaseHub {

    @Test public void test_default_configuration_import() throws IOException, ConnectorException, LoggingMessageSenderException {

        final HubFixture hubAFixture = fixture.createSocketHub(EnumSet.noneOf(HubTestFixture.Features.class));

        HubFixture hubBFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.Bridge));
        hubBFixture.getLoggingBridgeConfiguration().setPort(hubAFixture.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubAFixture.start();
        final SocketHub hubB = hubBFixture.start();

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1;
            }
        });

        SocketClient clientA = fixture.createClient("clientA", hubA);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hubB);

        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message")));

        Bucket<LogEvent> events = fixture.createEventBucketFor(clientB);

        events.waitForMessages(1);
        assertThat(events.get(0).getMessage(), is("Test message"));
    }

    @Test public void test_import() throws IOException, ConnectorException, LoggingMessageSenderException {

        final HubFixture hubAFixture = fixture.createSocketHub(EnumSet.noneOf(HubTestFixture.Features.class));

        HubFixture hubBFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.Bridge));
        hubBFixture.getLoggingBridgeConfiguration().setImportEvents(true);
        hubBFixture.getLoggingBridgeConfiguration().setExportEvents(false);
        hubBFixture.getLoggingBridgeConfiguration().setPort(hubAFixture.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubAFixture.start();
        final SocketHub hubB = hubBFixture.start();

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1;
            }
        });

        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hubA);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hubB);

        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client A to hub A")));
        clientB.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client B to hub B")));

        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(clientA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(clientB);

        eventsB.waitForMessages(1);
        assertThat(eventsB.get(0).getMessage(), is("Test message from client A to hub A"));

        assertThat(eventsA.size(), is(0));

    }

    @Test public void test_export() throws IOException, ConnectorException, LoggingMessageSenderException {

        final HubFixture hubAFixture = fixture.createSocketHub(EnumSet.noneOf(HubTestFixture.Features.class));

        HubFixture hubBFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.Bridge));
        hubBFixture.getLoggingBridgeConfiguration().setImportEvents(false);
        hubBFixture.getLoggingBridgeConfiguration().setExportEvents(true);
        hubBFixture.getLoggingBridgeConfiguration().setPort(hubAFixture.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubAFixture.start();
        final SocketHub hubB = hubBFixture.start();

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1;
            }
        });

        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hubA);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hubB);

        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client A to hub A")));
        clientB.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client B to hub B")));

        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(clientA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(clientB);

        eventsA.waitForMessages(1);
        assertThat(eventsA.get(0).getMessage(), is("Test message from client B to hub B"));

        assertThat(eventsB.size(), is(0));
    }

    @Test public void test_import_and_export_one_way() throws IOException, ConnectorException, LoggingMessageSenderException {

        // jshaw - I'm pretty certain this test has race conditions
        
        final HubFixture hubAFixture = fixture.createSocketHub(EnumSet.noneOf(HubTestFixture.Features.class));

        HubFixture hubBFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.Bridge));
        hubBFixture.getLoggingBridgeConfiguration().setImportEvents(true);
        hubBFixture.getLoggingBridgeConfiguration().setExportEvents(true);
        hubBFixture.getLoggingBridgeConfiguration().setPort(hubAFixture.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubAFixture.start();
        hubA.setName("HubA");
        
        final SocketHub hubB = hubBFixture.start();
        hubB.setName("HubB");

        // Wait for hubB to connect to hubA
        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1;
            }
        });
        
        assertThat(hubA.getConnectionsList().size(), is(1));
        assertThat(hubB.getConnectionsList().size(), is(0));

        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hubA);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hubB);

        assertThat(hubA.getConnectionsList().size(), is(2));
        assertThat(hubB.getConnectionsList().size(), is(1));
        
        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(clientA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(clientB);
        
        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client A to hub A")));
        clientB.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message from client B to hub B")));

        eventsA.waitForMessages(1);
        assertThat(eventsA.get(0).getMessage(), is("Test message from client B to hub B"));

        eventsB.waitForMessages(1);
        assertThat(eventsB.get(0).getMessage(), is("Test message from client A to hub A"));

        assertThat(eventsA.size(), is(1));
        assertThat(eventsB.size(), is(1));
    }


    @Test public void test_import_filter() throws IOException, ConnectorException, LoggingMessageSenderException {

        final HubFixture hubAFixture = fixture.createSocketHub(EnumSet.noneOf(HubTestFixture.Features.class));
        
        HubFixture hubBFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.Bridge));
        hubBFixture.getLoggingBridgeConfiguration().setImportEvents(true);
        hubBFixture.getLoggingBridgeConfiguration().setExportEvents(false);
        hubBFixture.getLoggingBridgeConfiguration().getFilters().add(FilterConfiguration.contains("orange"));
        hubBFixture.getLoggingBridgeConfiguration().setPort(hubAFixture.getSocketHubConfiguration().getPort());

        final SocketHub hubA = hubAFixture.start();
        final SocketHub hubB = hubBFixture.start();

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return hubA.getConnectionsList().size() == 1;
            }
        });

        SocketClient clientA = fixture.createClientAutoSubscribe("clientA", hubA);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hubB);
        
        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message orange from client B to hub B")));
        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message apple from client A to hub A")));

        Bucket<LogEvent> eventsA = fixture.createEventBucketFor(clientA);
        Bucket<LogEvent> eventsB = fixture.createEventBucketFor(clientB);

        eventsB.waitForMessages(1);
        assertThat(eventsB.get(0).getMessage(), is("Test message apple from client A to hub A"));
        
        assertThat(eventsA.size(), is(0));


    }
}
