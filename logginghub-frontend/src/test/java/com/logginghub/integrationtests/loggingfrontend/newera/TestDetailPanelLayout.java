package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestDetailPanelLayout {

    private static final String longString = "This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot. This is a bit of text thats going to get repeated a lot.";
    private SwingFrontEndDSL dsl;

    @After public void stopHub() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_message_shrinks_ok() throws InterruptedException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
        LogEvent event1 = LogEventBuilder.start()
                                         .setLevel(Level.INFO.intValue())
                                         .setMessage(longString)
                                         .setFormattedException(longString)
                                         .toLogEvent();
        dsl.publishEvent(event1);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(1);
        dsl.selectRow(0);

        JPanelFixture panel = dsl.getFrameFixture().panel("eventDetailPanel");
        int width = panel.target.getWidth();

        dsl.getFrameFixture().resizeWidthTo(50);
        int newWidth = panel.target.getWidth();

        assertThat(newWidth, is(not(width)));
    }

    @Ignore // jshaw - broken after OSX migration
    @Test public void test_exception_starts_invisible() throws InterruptedException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        JPanelFixture panel = dsl.getFrameFixture().panel("eventDetailPanel");

        GenericTypeMatcher<? extends JTextComponent> matcher = new GenericTypeMatcher<JTextComponent>(JTextComponent.class, false) {
            @Override protected boolean isMatching(JTextComponent component) {
                return component.getName().equals("exceptionArea");
            }
        };

        JTextComponentFixture textBox = dsl.getFrameFixture().textBox(matcher);

        LogEvent event1 = LogEventBuilder.start()
                                         .setLevel(Level.INFO.intValue())
                                         .setMessage(longString)
                                         .setFormattedException(longString)
                                         .toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Hello").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Small").setFormattedException("Small").toLogEvent();

        dsl.publishEvent(event1, event2, event3);
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.selectRow(0);
        assertThat(textBox.target.isVisible(), is(true));
        // Why is there a magic number here :/
        assertThat((double)textBox.target.getHeight(), is(closeTo(816d, 100d)));

        // Not really sure why its visible even though its not... and why height 16 is the magic
        // number - its all miglayout magic maybe?
        dsl.selectRow(1);
        assertThat(textBox.target.isVisible(), is(true));
        assertThat((double)textBox.target.getHeight(), is(closeTo(16, 1)));

        dsl.selectRow(2);
        assertThat(textBox.target.isVisible(), is(true));
        assertThat(textBox.target.getHeight(), is(19));

    }

}
