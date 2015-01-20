package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import javax.swing.JSplitPane;

import org.fest.swing.fixture.JSplitPaneFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestVerticalDetailView {

    private SwingFrontEndDSL dsl;

    @Before public void create() throws IOException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Test public void test_changing_orientation() throws InterruptedException {

        JSplitPaneFixture scrollPane = dsl.getFrameFixture().splitPane("eventDetailsSplitPane");
        assertThat(scrollPane.target.getOrientation(), is(JSplitPane.VERTICAL_SPLIT));
        
        dsl.setDetailViewOrientation(false);
        assertThat(scrollPane.target.getOrientation(), is(JSplitPane.HORIZONTAL_SPLIT));
        
        dsl.setDetailViewOrientation(true);        
        assertThat(scrollPane.target.getOrientation(), is(JSplitPane.VERTICAL_SPLIT));
    }
    
}
