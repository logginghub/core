package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Tracer;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestDummySource {

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
    
    @Test public void test_toggling_dummy_source() throws InterruptedException {

        dsl.setDummySource(false);
        
        dsl.assertLogEventTableSize(0);
        
        dsl.setDummySource(true);
        
        ThreadUtils.sleep(500);
        
        dsl.setDummySource(false);
        
        int logEventTableSize = dsl.getLogEventTableSize();
        assertThat(logEventTableSize, is(greaterThan(0)));
        
        ThreadUtils.sleep(500);
        
        assertThat(dsl.getLogEventTableSize(), is(logEventTableSize));
        
        // Make sure it'll come back on once stopped
        dsl.setDummySource(true);
        
        ThreadUtils.sleep(500);
        
        dsl.setDummySource(false);
        
        assertThat(dsl.getLogEventTableSize(), is(greaterThan(logEventTableSize)));
    }

}
