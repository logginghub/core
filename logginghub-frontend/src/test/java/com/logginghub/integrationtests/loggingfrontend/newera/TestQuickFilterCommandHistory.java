package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.frontend.PathHelper;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestQuickFilterCommandHistory {

    private SwingFrontEndDSL dsl;

    @Before public void before() {
        File folder = FileUtils.createRandomFolder("target/test/testQuickFilter");
        PathHelper.setLogViewerSettingsPath(folder);
    }
    
    @Test public void test_empty_single_env() throws IOException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertQuickFilterPopupWindowVisible(false);

        dsl.focusOnQuickFilter();
        dsl.downArrow();

        dsl.assertQuickFilterPopupWindowVisible(true);

        dsl.escape();

        dsl.assertQuickFilterPopupWindowVisible(false);
    }


    @Ignore // jshaw - broken after OSX migration
    @Test public void test_empty_multiple_env() throws IOException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.focusOnQuickFilter("env1");
        dsl.assertQuickFilterPopupWindowVisible(false);

        dsl.focusOnQuickFilter("env2");
        dsl.assertQuickFilterPopupWindowVisible(false);

        dsl.downArrow();
        dsl.assertQuickFilterPopupWindowVisible(true);

        dsl.focusOnQuickFilter("env1");
        dsl.assertQuickFilterPopupWindowVisible(false);

    }

    @Test public void test_add_item_multiple_env() throws IOException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.focusOnQuickFilter("env1");

        dsl.downArrow();
        dsl.assertQuickFilterPopupWindowVisible(true);
        dsl.assertQuickFilterPopupItemCount(0);
        dsl.escape();

        dsl.enterText("filter1");
        dsl.enter();

        dsl.downArrow();
        dsl.assertQuickFilterPopupWindowVisible(true);
        dsl.assertQuickFilterPopupItemCount(1);
        dsl.escape();

        dsl.enterText("filter1");
        dsl.enter();

        dsl.downArrow();
        dsl.assertQuickFilterPopupWindowVisible(true);
        dsl.assertQuickFilterPopupItemCount(1);
        dsl.assertQuickFilterPopupItem(0, "filter1");
        dsl.escape();

        dsl.enterText("filter2");
        dsl.enter();

        dsl.downArrow();
        dsl.assertQuickFilterPopupWindowVisible(true);
        dsl.assertQuickFilterPopupItemCount(2);
        dsl.assertQuickFilterPopupItem(0, "filter1");
        dsl.assertQuickFilterPopupItem(1, "filter2");
        dsl.escape();
    }

    @Test public void test_add_item_multiple_env_persisted() throws IOException {
        
        

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.focusOnQuickFilter("env1");
        dsl.enterText("filter1");
        dsl.enter();
        dsl.enterText("filter2");
        dsl.enter();
        
        dsl.downArrow();
        dsl.assertQuickFilterPopupItemCount(2);
        dsl.assertQuickFilterPopupItem(0, "filter1");
        dsl.assertQuickFilterPopupItem(1, "filter2");

        dsl.shutdown();

        dsl = BaseSwing.createDSL(configuration);

        dsl.focusOnQuickFilter("env1");
        
        dsl.downArrow();
        dsl.assertQuickFilterPopupItemCount(2);
        dsl.assertQuickFilterPopupItem(0, "filter1");
        dsl.assertQuickFilterPopupItem(1, "filter2");
    }

    @Ignore // jshaw - broken after OSX migration
    @Test public void test_items_in_env_config() throws IOException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1")
                                                                                                                                        .addQuickFilter("filter1")
                                                                                                                                        .addQuickFilter("filter2"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2").addQuickFilter("filter3"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.focusOnQuickFilter("env1");
        dsl.downArrow();
        dsl.assertQuickFilterPopupItemCount(2);
        dsl.assertQuickFilterPopupItem(0, "filter1");
        dsl.assertQuickFilterPopupItem(1, "filter2");

        dsl.focusOnQuickFilter("env2");
        dsl.downArrow();
        dsl.assertQuickFilterPopupItemCount(1);
        dsl.assertQuickFilterPopupItem(0, "filter3");
    }

    @After public void cleanup() throws IOException {
        if (dsl != null) {
            dsl.getFrameFixture().cleanUp();
        }
    }

}
