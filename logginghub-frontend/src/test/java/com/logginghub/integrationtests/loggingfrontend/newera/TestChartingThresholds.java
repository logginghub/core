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

public class TestChartingThresholds {

    private SwingFrontEndDSL dsl;

    @After public void cleanup() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_thresholds_and_y_axis_lock() {

        ChunkerConfiguration chunkerConfiguration2 = LoggingFrontendConfigurationBuilder.chunker(1000)
                                                                                        .addParser(LoggingFrontendConfigurationBuilder.parser("{host}/{source}/{label}")
                                                                                                                                      .addPattern("Order [orderID] placed in {time} ms", false))
                                                                                        .getChunkerConfiguration();

        PageConfiguration pageConfiguration2 = LoggingFrontendConfigurationBuilder.page("Env 3 Page 1 ", 3, 3)
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (mean)")
                                                                                                                               .addMatcher("*/time/Mean")
                                                                                                                               .dataPoints(8)
                                                                                                                               .warningThreshold(10)
                                                                                                                               .severeThreshold(20)
                                                                                                                               .yAxisLock(100))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (median)").addMatcher("*/time/Median"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (mode)").addMatcher("*/time/Mode"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (stddev)").addMatcher("*/time/StandardDeviation"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order time (90th %ile)").addMatcher("*/time/Percentile90"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order count").addMatcher("*/time/Count"))
                                                                                  .addChart(LoggingFrontendConfigurationBuilder.chart("Order total").addMatcher("*/time/TotalCount"))
                                                                                  .getPageConfiguration();

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1"))
                                                                                        .setPopoutCharting(true)
                                                                                        .setShowOldCharting(true)
                                                                                        .toConfiguration();

        ChartingConfiguration chartingConfigurationEnv2 = configuration.getEnvironments().get(0).getChartingConfiguration();
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

        int delay = 1;
        // Fire off some events at the environments
        dsl.publishEvent("env1", timeBase += 0, "Order 1232 placed in 5 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1233 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1234 placed in 15 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1235 placed in 20 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1236 placed in 30 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 40 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 70 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 100 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 150 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 80 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1237 placed in 40 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 30 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 20 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.publishEvent("env1", timeBase += 1000, "Order 1238 placed in 10 ms");
        dsl.debugSleep(delay);
        dsl.debugSleep(delay);

    }

}
