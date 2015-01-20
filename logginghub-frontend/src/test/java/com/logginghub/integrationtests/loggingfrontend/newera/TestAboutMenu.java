package com.logginghub.integrationtests.loggingfrontend.newera;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.Tracer;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestAboutMenu {

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
    
    @Test public void test_about_menu() throws InterruptedException {
        dsl.getFrameFixture().menuItemWithPath("Help", "About the Logging Front End").click();
    }
}

