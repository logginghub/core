package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Test;

import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.logging.frontend.configuration.ChartingConfiguration;
import com.logginghub.logging.frontend.configuration.ChunkerConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.frontend.configuration.PageConfiguration;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestChartingPopout {

    private SwingFrontEndDSL dsl;

    @After public void cleanup() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_no_pop_out_by_default() {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        assertThat(dsl.getSwingFrontEnd().getChartingPopoutFrameOld(), is(nullValue()));
    }

    @Test public void test_pop_out_visible_from_config() {

        ChunkerConfiguration chunkerConfiguration1 = LoggingFrontendConfigurationBuilder.chunker(1000)
                                                                                        .addParser(LoggingFrontendConfigurationBuilder.parser("{host}/{source}/{label}")
                                                                                                                                      .addPattern("User '[user]' loaded page '[page]' in {time} ms - {size} byte were returned",
                                                                                                                                                  false))
                                                                                        .getChunkerConfiguration();

        ChunkerConfiguration chunkerConfiguration2 = LoggingFrontendConfigurationBuilder.chunker(1000)
                                                                                        .addParser(LoggingFrontendConfigurationBuilder.parser("{host}/{source}/{label}")
                                                                                                                                      .addPattern("Order [orderID] placed in {time} ms",
                                                                                                                                                  false))
                                                                                        .getChunkerConfiguration();

        PageConfiguration pageConfiguration1 = LoggingFrontendConfigurationBuilder.page("new page", 1, 1)
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Page loading time").addMatcher("*/time/Mean"))
                                                                                  .getPageConfiguration();

        PageConfiguration pageConfiguration2 = LoggingFrontendConfigurationBuilder.page("Env 3 Page 1 ", 3, 3)
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (mean)").addMatcher("*/time/Mean"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (median)").addMatcher("*/time/Median"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (mode)").addMatcher("*/time/Mode"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (stddev)").addMatcher("*/time/StandardDeviation"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (90th %ile)").addMatcher("*/time/Percentile90"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order count").addMatcher("*/time/Count"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order total").addMatcher("*/time/TotalCount"))
                                                                                  .getPageConfiguration();

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2"))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env3"))
                                                                                        .setPopoutCharting(true)
                                                                                        .toConfiguration();
        
        configuration.setShowOldCharting(true);

        ChartingConfiguration chartingConfigurationEnv1 = configuration.getEnvironments().get(0).getChartingConfiguration();
        chartingConfigurationEnv1.getPages().add(pageConfiguration1);
        chartingConfigurationEnv1.getParserConfiguration().getChunkingParsers().add(chunkerConfiguration1);
        
        
        ChartingConfiguration chartingConfigurationEnv2 = configuration.getEnvironments().get(2).getChartingConfiguration();
        chartingConfigurationEnv2.getPages().add(pageConfiguration2);
        chartingConfigurationEnv2.getParserConfiguration().getChunkingParsers().add(chunkerConfiguration2);

        

        dsl = SwingFrontEndDSL.createDSL(configuration);

        SwingFrontEnd swingFrontEnd = dsl.getSwingFrontEnd();
        JFrame chartingPopoutFrame = swingFrontEnd.getChartingPopoutFrameOld();
        // FrameFixture chartingFixture = new
        // FrameFixture(dsl.getFrameFixture().robot, swingFrontEnd);

        assertThat(chartingPopoutFrame, is(not(nullValue())));
        assertThat(chartingPopoutFrame.isVisible(), is(true));

        Logger.setRootLevel(Logger.fine);

        long timeBase = TimeUtils.parseTime("10:00:00 1/1/2013");

        // Fire off some events at the environments
        dsl.publishEvent("env1", timeBase + 0, "User 'dave' loaded page 'index.html' in 1 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 500, "User 'dave' loaded page 'index.html' in 2 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 1000, "User 'dave' loaded page 'index.html' in 3 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 1500, "User 'dave' loaded page 'index.html' in 4 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 2000, "User 'dave' loaded page 'index.html' in 3 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 2500, "User 'dave' loaded page 'index.html' in 2 ms - 345 byte were returned");
        dsl.publishEvent("env1", timeBase + 3000, "User 'dave' loaded page 'index.html' in 1 ms - 345 byte were returned");

        dsl.publishEvent("env3", timeBase + 0, "Order 1232 placed in 10 ms");
        dsl.publishEvent("env3", timeBase + 500, "Order 1233 placed in 20 ms");
        dsl.publishEvent("env3", timeBase + 1000, "Order 1234 placed in 30 ms");
        dsl.publishEvent("env3", timeBase + 1500, "Order 1235 placed in 40 ms");
        dsl.publishEvent("env3", timeBase + 2000, "Order 1236 placed in 30 ms");
        dsl.publishEvent("env3", timeBase + 2500, "Order 1237 placed in 20 ms");
        dsl.publishEvent("env3", timeBase + 3000, "Order 1238 placed in 10 ms");
    }

}
