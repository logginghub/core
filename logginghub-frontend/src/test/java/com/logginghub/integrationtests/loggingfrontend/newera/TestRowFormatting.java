package com.logginghub.integrationtests.loggingfrontend.newera;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;

import org.fest.swing.data.TableCell;
import org.fest.swing.fixture.ColorFixture;
import org.junit.After;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.ColourUtils;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestRowFormatting {

    private SwingFrontEndDSL dsl;

    @After public void stopHub() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_default_format() throws InterruptedException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.FINEST.intValue()).setMessage("Message 1 : finest").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.FINER.intValue()).setMessage("Message 2 : finer").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.FINE.intValue()).setMessage("Message 3 : fine").toLogEvent();
        LogEvent event4 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 4 : info").toLogEvent();
        LogEvent event5 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 5 : warning").toLogEvent();
        LogEvent event6 = LogEventBuilder.start().setLevel(Level.SEVERE.intValue()).setMessage("Message 6 : severe").toLogEvent();

        dsl.setLevelFilter(Level.ALL);

        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        dsl.publishEvent(event4);
        dsl.publishEvent(event5);
        dsl.publishEvent(event6);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(6);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        dsl.assertLogEventInTable(3, event4);
        dsl.assertLogEventInTable(4, event5);
        dsl.assertLogEventInTable(5, event6);

        dsl.selectRow(3);
        
        ColorFixture backgroundAt = dsl.getTable().backgroundAt(TableCell.row(3).column(1));
        backgroundAt.requireEqualTo(Color.cyan);
    }

    @Test public void test_selected_font_bold_and_default_background_with_row_highlighter() throws InterruptedException {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default").highlighter("info", "Green", false))
                                                                                        .selectedRowFormat(LoggingFrontendConfigurationBuilder.rowFormat().font("Arial-bold-14").backgroundColour(""))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.FINEST.intValue()).setMessage("Message 1 : finest").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.FINER.intValue()).setMessage("Message 2 : finer").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.FINE.intValue()).setMessage("Message 3 : fine").toLogEvent();
        LogEvent event4 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 4 : info").toLogEvent();
        LogEvent event5 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 5 : warning").toLogEvent();
        LogEvent event6 = LogEventBuilder.start().setLevel(Level.SEVERE.intValue()).setMessage("Message 6 : severe").toLogEvent();

        dsl.setLevelFilter(Level.ALL);

        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        dsl.publishEvent(event4);
        dsl.publishEvent(event5);
        dsl.publishEvent(event6);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(6);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        dsl.assertLogEventInTable(3, event4);
        dsl.assertLogEventInTable(4, event5);
        dsl.assertLogEventInTable(5, event6);

        dsl.selectRow(3);

        ColorFixture backgroundAt = dsl.getTable().backgroundAt(TableCell.row(3).column(1));
        backgroundAt.requireEqualTo(ColourUtils.parseColor("Green"));
    }

    @Test public void test_selected_font_bold() throws InterruptedException {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .selectedRowFormat(LoggingFrontendConfigurationBuilder.rowFormat().font("Arial-bold-14"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.FINEST.intValue()).setMessage("Message 1 : finest").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.FINER.intValue()).setMessage("Message 2 : finer").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.FINE.intValue()).setMessage("Message 3 : fine").toLogEvent();
        LogEvent event4 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 4 : info").toLogEvent();
        LogEvent event5 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 5 : warning").toLogEvent();
        LogEvent event6 = LogEventBuilder.start().setLevel(Level.SEVERE.intValue()).setMessage("Message 6 : severe").toLogEvent();

        dsl.setLevelFilter(Level.ALL);

        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        dsl.publishEvent(event4);
        dsl.publishEvent(event5);
        dsl.publishEvent(event6);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(6);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        dsl.assertLogEventInTable(3, event4);
        dsl.assertLogEventInTable(4, event5);
        dsl.assertLogEventInTable(5, event6);

        dsl.selectRow(3);

        ColorFixture backgroundAt = dsl.getTable().backgroundAt(TableCell.row(3).column(1));
        backgroundAt.requireEqualTo(Color.cyan);
    }

    @Test public void test_selected_border_not_colour_change() throws InterruptedException {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .selectedRowFormat(LoggingFrontendConfigurationBuilder.rowFormat()
                                                                                                                                              .font("Arial-bold-12")
                                                                                                                                              .backgroundColour("")
                                                                                                                                              .borderColour("black")
                                                                                                                                              .borderLineWidth(1))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.FINEST.intValue()).setMessage("Message 1 : finest").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.FINER.intValue()).setMessage("Message 2 : finer").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.FINE.intValue()).setMessage("Message 3 : fine").toLogEvent();
        LogEvent event4 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 4 : info").toLogEvent();
        LogEvent event5 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 5 : warning").toLogEvent();
        LogEvent event6 = LogEventBuilder.start().setLevel(Level.SEVERE.intValue()).setMessage("Message 6 : severe").toLogEvent();

        dsl.setLevelFilter(Level.ALL);

        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        dsl.publishEvent(event4);
        dsl.publishEvent(event5);
        dsl.publishEvent(event6);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(6);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        dsl.assertLogEventInTable(3, event4);
        dsl.assertLogEventInTable(4, event5);
        dsl.assertLogEventInTable(5, event6);

        dsl.selectRow(3);

        ColorFixture backgroundAt = dsl.getTable().backgroundAt(TableCell.row(3).column(1));
        backgroundAt.requireEqualTo(new Color(247, 247, 254));
    }

}
