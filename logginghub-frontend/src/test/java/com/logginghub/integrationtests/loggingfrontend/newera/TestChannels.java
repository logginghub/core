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

public class TestChannels {

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
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .setChannel("events/channel1")
                                                                                                                                        .hub("localhost",
                                                                                                                                             hub.getPort()))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        hub.close();
        dsl.getFrameFixture().cleanUp();
    }

    @Test public void test_channels() throws InterruptedException, IOException {
        
        // Make sure the frontend is connected to the hub
        dsl.assertConnected("localhost", hub.getPort());

        LogEvent event1 = LogEventBuilder.start().setMessage("Channel event").setLevel(Level.INFO.intValue()).setChannel("events").toLogEvent();
        LogEvent event2 = LogEventBuilder.start()
                                         .setMessage("Channel event")
                                         .setLevel(Level.INFO.intValue())
                                         .setChannel("events/channel1")
                                         .toLogEvent();
        LogEvent event3 = LogEventBuilder.start()
                                         .setMessage("Channel event")
                                         .setLevel(Level.INFO.intValue())
                                         .setChannel("events/channel2")
                                         .toLogEvent();
        
        dsl.sendEventToHub(hub, event1);
        dsl.sendEventToHub(hub, event2);
        dsl.sendEventToHub(hub, event3);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, event2);

        // Bound the hub and make sure the subscription is restored
        hub.stop();
        hub.start();
        
        dsl.assertConnected("localhost", hub.getPort());
        
        dsl.sendEventToHub(hub, event1);
        dsl.sendEventToHub(hub, event2);
        dsl.sendEventToHub(hub, event3);
        dsl.waitForBatch();
        
        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, event2);
        dsl.assertLogEventInTable(1, event2);
    }

}
