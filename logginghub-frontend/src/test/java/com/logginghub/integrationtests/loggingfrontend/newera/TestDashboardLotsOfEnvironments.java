package com.logginghub.integrationtests.loggingfrontend.newera;

import org.junit.Test;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestDashboardLotsOfEnvironments {

    private SwingFrontEndDSL dsl;

    @Test public void test_dashboard_font_size_with_lots_of_envs() throws InterruptedException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .setShowDashboard(true)
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env1"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env2"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env3"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env4"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env5"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env6"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env7"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env8"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("Env9"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
//        dsl.debugSleep();
        dsl.shutdown();
    }
}
