package com.logginghub.integrationtests.loggingfrontend.newera;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestQuickFilterSingle {

    private SwingFrontEndDSL dsl;

    private LogEvent event1 = SwingFrontEndDSL.createEvent("default", Logger.info, "apple orange");
    private LogEvent event2 = SwingFrontEndDSL.createEvent("default", Logger.warning, "apple banana");
    private LogEvent event3 = SwingFrontEndDSL.createEvent("default", Logger.severe, "pear cherry");
    private LogEvent event4 = SwingFrontEndDSL.createEvent("default", Logger.warning, "orange pear");
    private LogEvent event5 = SwingFrontEndDSL.createEvent("default", Logger.info, "cherry apple");

    @Before public void before() {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @Test public void test_standard_filter() {

        LogEvent event = dsl.publishEvent("default", Logger.info, "Message 1");
        dsl.waitForBatch();

        dsl.assertLogEventTable(event);

        dsl.setQuickFilter("default", "1");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event);

        dsl.setQuickFilter("default", "2");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable();

        dsl.backspace();
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event);
    }

    @Test public void test_level_filter() {
        dsl.publishEventAndWaitForBatch(event1, event2, event3, event4, event5);
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);

        dsl.setLevelFilter(Level.INFO);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);

        dsl.setLevelFilter(Level.WARNING);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event2, event3, event4);

        dsl.setLevelFilter(Level.SEVERE);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event3);

        dsl.setLevelFilter(Level.WARNING);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event2, event3, event4);

        dsl.setLevelFilter(Level.ALL);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
    }

    @Test public void test_level_and_text_filter() {
        dsl.publishEventAndWaitForBatch(event1, event2, event3, event4, event5);

        dsl.setLevelFilter(Level.WARNING);
        dsl.setQuickFilter("default", "apple");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event2);

        dsl.setLevelFilter(Level.ALL);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event5);

        dsl.setQuickFilter("default", "");
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
    }

    @Test public void test_regex() {
        dsl.publishEventAndWaitForBatch(event1, event2, event3, event4, event5);

        dsl.setQuickFilter("default", ".*apple.*");
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable();

        dsl.setQuickFilterRegex("default", 0, true);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event5);

        dsl.setQuickFilterRegex("default", 0, false);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable();

        dsl.setQuickFilter("default", ".*");
        dsl.setQuickFilterRegex("default", 0, true);
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
    }

    @Test public void test_plus_minus_single_filter() {

        dsl.publishEventAndWaitForBatch(event1, event2, event3, event4, event5);
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);

        dsl.setQuickFilter("default", "apple");
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event5);

        dsl.setQuickFilter("default", "+apple");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event1, event2, event5);

        dsl.setQuickFilter("default", "-apple");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event3, event4);

        dsl.setQuickFilter("default", "+apple -banana");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event1, event5);

        dsl.setQuickFilter("default", "+apple +orange");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event1);

        dsl.setQuickFilter("default", "-orange -cherry");
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event2);
    }

    @Test public void test_select_and_remove_all() {
        dsl.publishEventAndWaitForBatch(event1, event2, event3, event4, event5);
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);

        dsl.setQuickFilter("default", "apple");
        dsl.waitForQuickFilter();
        dsl.assertLogEventTable(event1, event2, event5);

        // Hack to make the shift key stay down... (google it...)
        if (OSUtils.isWindows()) {
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
        }
        dsl.pressModifiedKeys(KeyEvent.VK_SHIFT, KeyEvent.VK_HOME);
        dsl.backspace();
        dsl.waitForQuickFilter();

        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
    }

    @After public void after() throws IOException {
        dsl.shutdown();
    }

}
