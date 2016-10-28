package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.Tracer;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestAutoScroll {

    private SwingFrontEndDSL dsl;

    @Before public void create() throws IOException {
        Tracer.enable();
        Tracer.autoIndent(true);

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Test public void test_row_locking_doesnt_remove() throws InterruptedException {

        JTableFixture table = dsl.getTable();
        Container parent = table.target.getParent();
        assertThat(parent, is(instanceOf(JViewport.class)));
        JViewport viewport = (JViewport) parent;
        Container parent2 = viewport.getParent();
        assertThat(parent2, is(instanceOf(JScrollPane.class)));
        JScrollPane pane = (JScrollPane) parent2;
        JScrollBar verticalScrollBar = pane.getVerticalScrollBar();
        assertThat(verticalScrollBar.getValue(), is(0));

        int events = 100;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(events);
        int value = verticalScrollBar.getValue();
        assertThat(value, is(greaterThan(0)));

        dsl.setAutoScroll(false);

        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(events * 2);
        assertThat(verticalScrollBar.getValue(), is(value));

        dsl.setAutoScroll(true);

        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(events * 3);
        assertThat(verticalScrollBar.getValue(), is(greaterThan(value)));
    }

    @Test public void test_scroll_includes_last_element() throws InterruptedException {

        JTableFixture table = dsl.getTable();
        Container parent = table.target.getParent();
        assertThat(parent, is(instanceOf(JViewport.class)));
        JViewport viewport = (JViewport) parent;
        Container parent2 = viewport.getParent();
        assertThat(parent2, is(instanceOf(JScrollPane.class)));
        JScrollPane pane = (JScrollPane) parent2;
        JScrollBar verticalScrollBar = pane.getVerticalScrollBar();
        assertThat(verticalScrollBar.getValue(), is(0));

        int events = 100;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(events);
        int value = verticalScrollBar.getValue();
        assertThat(value, is(greaterThan(0)));

        int scrollMax = verticalScrollBar.getMaximum();

        Rectangle visibleRect = table.target.getVisibleRect();

        int viewBottom = visibleRect.y + visibleRect.height;

        assertThat(viewBottom, is(scrollMax));
    }

}
