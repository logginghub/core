package com.logginghub.integrationtests.loggingfrontend.oldera;

import org.fest.swing.data.TableCell;
import org.fest.swing.fixture.JTableFixture;
import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;

public class HighlighterTest extends BaseSwing {

    @Override protected LoggingFrontendConfiguration getConfiguration() {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .highlighter("blue", "0x0000ff", false)
                                                                                                                                        .highlighter("green", "0x00ff00", false))
                                                                                        .toConfiguration();

        return configuration;

    }

    @Test public void test() throws InterruptedException {
        dsl.publishEvent("This is a message without highlighting");
        dsl.publishEvent("This is a message with green highlighting");
        dsl.publishEvent("This is a message with blue highlighting");
        dsl.waitForBatch();
        
        JTableFixture table = getFrameFixture().table("logEventTable");
        table.backgroundAt(TableCell.row(1).column(0)).requireEqualTo("00ff00");
        table.backgroundAt(TableCell.row(2).column(0)).requireEqualTo("0000ff");

    }
}
