package com.logginghub.integrationtests.loggingfrontend.newera;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Out;
import com.logginghub.utils.logging.LoggingHelper;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestHubFiltering {

    private SocketHub hub;
    private SwingFrontEndDSL dsl;

    @Parameterized.Parameters public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }

    @Before public void create() throws IOException {
        Out.out("--------------------------------------------------------------------------------------------------------");
        LoggingHelper.setupFineLoggingWithFullClassnames();

        hub = SocketHub.createTestHub();
        hub.useRandomPort();
        hub.start();
        hub.waitUntilBound();

        Out.out("hub bound on {}", hub.getPort());

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default").hub("localhost", hub.getPort()))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        hub.close();
        dsl.getFrameFixture().cleanUp();
    }

    @Test public void test_with_server_side_filtering_off() throws InterruptedException {
        // Make sure the frontend is connected to the hub
        dsl.assertConnected("localhost", hub.getPort());

        // Set the hub filtering to off
        dsl.setHubFiltering(false);

        // Set the level to warning
        dsl.setLevelFilter(Level.WARNING);

        // Send a warning level log event into the hub, make sure the front end
        // gets it
        LogEvent warningLogEvent = LogEventBuilder.start().setMessage("Warning message").setLevel(Level.WARNING.intValue()).toLogEvent();
        dsl.sendEventToHub(hub, warningLogEvent);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

        // Send an info level log event into the hub, make sure the front end
        // doesn't display it
        LogEvent infoLogEvent = LogEventBuilder.start().setMessage("Info message").setLevel(Level.INFO.intValue()).toLogEvent();
        dsl.sendEventToHub(hub, infoLogEvent);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

        // make sure its in the filtered set
        // Set the level to info
        dsl.setLevelFilter(Level.INFO);
        dsl.waitForQuickFilter();

        // Make sure both events are visible
        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, warningLogEvent);
        dsl.assertLogEventInTable(1, infoLogEvent);
    }

    @Test public void test_with_server_side_filtering_on() throws InterruptedException {

        // Make sure the frontend is connected to the hub
        dsl.assertConnected("localhost", hub.getPort());

        // Set the hub filtering to off
        dsl.setHubFiltering(true);

        // Set the level to warning
        dsl.setLevelFilter(Level.WARNING);
        dsl.waitForQuickFilter();

        // Send a warning level log event into the hub, make sure the front end gets it
        LogEvent warningLogEvent = LogEventBuilder.start().setMessage("Warning message").setLevel(Level.WARNING.intValue()).toLogEvent();
        dsl.sendEventToHub(hub, warningLogEvent);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

        // Send an info level log event into the hub, make sure the front end doesn't display it
        LogEvent infoLogEvent = LogEventBuilder.start().setMessage("Info message").setLevel(Level.INFO.intValue()).toLogEvent();
        dsl.sendEventToHub(hub, infoLogEvent);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

        // make sure its in the filtered set Set the level to info
        dsl.setLevelFilter(Level.INFO);
        dsl.waitForQuickFilter();       
        
        // Make sure the info one isn't visible (it wont have been sent by the hub)
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

    }
}
