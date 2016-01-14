package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.Tracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;

public class TestLockingPerEnvironment {

    private SwingFrontEndDSL dsl;

    @Before public void create() throws IOException {
        Tracer.enable();
        Tracer.autoIndent(true);

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .setShowDashboard(true)
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("AutoLocking").setAutoLocking(true))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("NotAutoLocking").setAutoLocking(false))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Ignore // jshaw - broken after OSX migration
    @Test public void test_auto_locking_environment() throws InterruptedException {

        dsl.assertAutoLocking("AutoLocking", true);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();

        dsl.publishEvent("AutoLocking", event1);
        dsl.publishEvent("AutoLocking", event2);
        dsl.publishEvent("AutoLocking", event3);

        dsl.waitForBatch("AutoLocking");

        dsl.assertLogEventTableSize("AutoLocking", 3);
        dsl.assertLogEventInTable("AutoLocking", 0, event1);
        dsl.assertLogEventInTable("AutoLocking", 1, event2);
        dsl.assertLogEventInTable("AutoLocking", 2, event3);

        dsl.clearEvents("AutoLocking");
        dsl.assertLogEventTableSize("AutoLocking", 1);
        dsl.assertLogEventInTable("AutoLocking", 0, event2);
    }

    @Ignore // jshaw - broken after OSX migration
    @Test public void test_not_auto_locking_environment() throws InterruptedException {

        dsl.assertAutoLocking("NotAutoLocking", false);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();

        dsl.publishEvent("NotAutoLocking", event1);
        dsl.publishEvent("NotAutoLocking", event2);
        dsl.publishEvent("NotAutoLocking", event3);

        dsl.waitForBatch("NotAutoLocking");

        dsl.assertLogEventTableSize("NotAutoLocking", 3);
        dsl.assertLogEventInTable("NotAutoLocking", 0, event1);
        dsl.assertLogEventInTable("NotAutoLocking", 1, event2);
        dsl.assertLogEventInTable("NotAutoLocking", 2, event3);

        dsl.clearEvents("NotAutoLocking");
        dsl.assertLogEventTableSize("NotAutoLocking", 0);
    }
}
