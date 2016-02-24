package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.LoggingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.logginghub.logging.LogEventBuilder.*;
import static com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder.*;

public class TestExternalProcessFiltering {

    private SwingFrontEndDSL dsl;
    private int port;

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }

    @Before
    public void create() throws IOException {
        Out.out("--------------------------------------------------------------------------------------------------------");
        LoggingHelper.setupFineLoggingWithFullClassnames();

        port = NetUtils.findFreePort();

    }

    @After
    public void teardown() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Test
    public void test_meta_data_filter_external() throws InterruptedException {

        LoggingFrontendConfiguration configuration;
        configuration = newConfiguration().localRPCPort(port)
                                          .environment(newEnvironment("default").columnSettings("Metadata", 1, 100, "key")
                                                                                .customFilter("Key", "key", "Contains", 100, ""))
                                          .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent infoLogEvent = logEvent().setMessage("Info message").setLevel(Level.INFO.intValue()).metadata("key", "A").toLogEvent();
        LogEvent warningLogEvent = logEvent().setMessage("Warning message").setLevel(Level.WARNING.intValue()).metadata("key", "B").toLogEvent();

        dsl.publishEvent(infoLogEvent);
        dsl.publishEvent(warningLogEvent);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "A");
        dsl.assertTableCell(1, 1, "B");

        // Set the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"A\"}}}");

        dsl.waitForTableRows(1);

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, infoLogEvent);

        dsl.assertTableCell(0, 1, "A");

        // Clear the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"\"}}}");

        dsl.waitForTableRows(2);

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "A");
        dsl.assertTableCell(1, 1, "B");
    }

    @Test
    public void test_meta_data_filter_external_date_filter() throws InterruptedException {

        LoggingFrontendConfiguration configuration;
        configuration = newConfiguration().localRPCPort(port)
                                          .environment(newEnvironment("default").columnSettings("Metadata", 1, 180, "key", "Date")
                                                                                .customDateFilter("Key",
                                                                                                  "key",
                                                                                                  "GreaterThanOrEquals",
                                                                                                  200,
                                                                                                  TimeUtils.parseTime("01/01/2016")))
                                          .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent infoLogEvent = logEvent().setMessage("Info message")
                                          .setLevel(Level.INFO.intValue())
                                          .metadata("key", Long.toString(TimeUtils.parseTime("02/01/2016 10:01:00")))
                                          .toLogEvent();
        LogEvent warningLogEvent = logEvent().setMessage("Warning message")
                                             .setLevel(Level.WARNING.intValue())
                                             .metadata("key", Long.toString(TimeUtils.parseTime("03/01/2016 10:02:00")))
                                             .toLogEvent();

        dsl.publishEvent(infoLogEvent);
        dsl.publishEvent(warningLogEvent);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "02/01/2016 10:01:00.000");
        dsl.assertTableCell(1, 1, "03/01/2016 10:02:00.000");

        // Set the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"05/01/2016\"}}}");

        dsl.waitForTableRows(0);
        dsl.assertLogEventTableSize(0);

        // Relax the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"03/01/2016\"}}}");

        dsl.waitForTableRows(1);
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, warningLogEvent);

        dsl.assertTableCell(0, 1, "03/01/2016 10:02:00.000");

        // Clear the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"\"}}}");

        dsl.waitForTableRows(2);

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "02/01/2016 10:01:00.000");
        dsl.assertTableCell(1, 1, "03/01/2016 10:02:00.000");
    }

    @Test
    public void test_meta_data_filter_external_with_choices() throws InterruptedException {

        LoggingFrontendConfiguration configuration;
        configuration = newConfiguration().localRPCPort(port)
                                          .environment(newEnvironment("default").columnSettings("Metadata", 1, 100, "key")
                                                                                .customFilter("Key", "key", "Contains", 100, "", "A,B,C"))
                                          .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent infoLogEvent = logEvent().setMessage("Info message").setLevel(Level.INFO.intValue()).metadata("key", "A").toLogEvent();
        LogEvent warningLogEvent = logEvent().setMessage("Warning message").setLevel(Level.WARNING.intValue()).metadata("key", "B").toLogEvent();

        dsl.publishEvent(infoLogEvent);
        dsl.publishEvent(warningLogEvent);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "A");
        dsl.assertTableCell(1, 1, "B");

        // Set the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"C\"}}}");

        dsl.waitForTableRows(0);
        dsl.assertLogEventTableSize(0);

        // Clear the filter
        dsl.setFilterExternal("{ \"default\": { \"filters\":{ \"Key\":\"\"}}}");

        dsl.waitForTableRows(2);

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "A");
        dsl.assertTableCell(1, 1, "B");
    }

    @Test
    public void test_meta_data_filter_internal() throws InterruptedException {

        LoggingFrontendConfiguration configuration;
        configuration = newConfiguration().environment(newEnvironment("default").columnSettings("Metadata", 1, 100, "key")
                                                                                .customFilter("Key", "key", "Contains", 100, "")).toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent infoLogEvent = logEvent().setMessage("Info message").setLevel(Level.INFO.intValue()).metadata("key", "A").toLogEvent();
        LogEvent warningLogEvent = logEvent().setMessage("Warning message").setLevel(Level.WARNING.intValue()).metadata("key", "B").toLogEvent();

        dsl.publishEvent(infoLogEvent);
        dsl.publishEvent(warningLogEvent);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, infoLogEvent);
        dsl.assertLogEventInTable(1, warningLogEvent);

        dsl.assertTableCell(0, 1, "A");
        dsl.assertTableCell(1, 1, "B");

        dsl.setCustomFilter("key", "A");

        dsl.waitForTableRows(1);

        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, infoLogEvent);

        dsl.assertTableCell(0, 1, "A");
    }


}
