package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JMenuItemFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Tracer;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestRepositoryBrowserDefault {

    private SwingFrontEndDSL dsl;
    
    @Before public void create() throws IOException {
        Tracer.enable();
        Tracer.autoIndent(true);
        
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))                                                                                        
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void cleanUp() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }
    
    @Test public void test_open_repo_hidden_by_default() throws InterruptedException {
        FrameFixture frame = dsl.getFrameFixture();
        JMenuItemFixture menuItemWithPath = frame.menuItemWithPath("Edit", "Repository Search");
        assertThat(menuItemWithPath.target.isVisible(), is(false));
    }

}
