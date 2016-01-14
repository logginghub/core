package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.utils.ThreadUtils;
import org.fest.swing.fixture.JLabelFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@Ignore // jshaw - broken
public class TestDashboard {

    private SwingFrontEndDSL dsl;

    @After
    public void cleanup() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Before
    public void create() throws IOException {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .setShowDashboard(true)
                                                                                        .environment(newEnvironment("Pricing"))
                                                                                        .environment(newEnvironment("Trading"))
                                                                                        .environment(newEnvironment("Risk"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @Test
    public void test_dashboard_clears_when_detail_tab_clears() throws InterruptedException {
        LoggingFrontendModel model = dsl.getSwingFrontEnd().getModel();
        EnvironmentModel pricingModel = model.getEnvironment("Pricing");

        pricingModel.onNewLogEvent(LogEventBuilder.start().setLevel(Level.WARNING.intValue()).toLogEvent());

        dsl.waitForBatch("Pricing");
        dsl.waitForSwingQueueToFlush();
        pricingModel.updateEachSecond();

        final JLabelFixture label = dsl.getFrameFixture()
                                       .panel("DashboardPanel")
                                       .panel("EnvironmentSummaryPanel-Pricing")
                                       .panel("warningIndicatorResizingLabel")
                                       .label("valueResizingLabel");
        ThreadUtils.untilTrue(1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return label.target.getText().equals("1");
            }
        });

        dsl.clearEvents("Pricing");

        dsl.waitForSwingQueueToFlush();
        pricingModel.updateEachSecond();
        dsl.waitForSwingQueueToFlush();

        assertThat(label.target.getText(), is("0"));
    }

    @Test
    public void test_dashboard_clicking() throws InterruptedException {

        dsl.setLevelFilter("Pricing", Level.INFO);
        LoggingFrontendModel model = dsl.getSwingFrontEnd().getModel();
        EnvironmentModel pricingModel = model.getEnvironment("Pricing");

        pricingModel.onNewLogEvent(LogEventBuilder.start().setLevel(Level.WARNING.intValue()).toLogEvent());

        dsl.selectTab("Dashboard");
        final JLabelFixture label = dsl.getFrameFixture()
                                       .panel("DashboardPanel")
                                       .panel("EnvironmentSummaryPanel-Pricing")
                                       .panel("warningIndicatorResizingLabel")
                                       .label("valueResizingLabel");
        ThreadUtils.untilTrue(1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return label.target.getText().equals("1 (+1)");
            }
        });
        assertThat(label.target.getText(), is("1 (+1)"));

        label.doubleClick();

        dsl.ensureTabSelected("Pricing");
        dsl.ensureLevelFilter("Pricing", Level.WARNING);
    }

    @Test
    public void test_dashboard_updates() throws InterruptedException {
        dsl.getSwingFrontEnd().stopModelUpdateTimer();
        LoggingFrontendModel model = dsl.getSwingFrontEnd().getModel();
        EnvironmentModel pricingModel = model.getEnvironment("Pricing");

        pricingModel.onNewLogEvent(LogEventBuilder.start().setLevel(Level.WARNING.intValue()).toLogEvent());

        // Problem - the events get pushed through the swing hierarchy on the
        // event queue, so if we force the model to update now it'll miss the
        // message
        // So we need to wait for the swing event queue to flush
        dsl.waitForBatch("Pricing");
        dsl.waitForSwingQueueToFlush();

        // This update will set both values to 1
        pricingModel.updateEachSecond();

        final JLabelFixture label = dsl.getFrameFixture()
                                       .panel("DashboardPanel")
                                       .panel("EnvironmentSummaryPanel-Pricing")
                                       .panel("warningIndicatorResizingLabel")
                                       .label("valueResizingLabel");
        final JLabelFixture perSecondLabel = dsl.getFrameFixture()
                                                .panel("DashboardPanel")
                                                .panel("EnvironmentSummaryPanel-Pricing")
                                                .panel("warningIndicatorResizingLabel")
                                                .label("valueResizingLabel");

        ThreadUtils.untilTrue(5, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return label.target.getText().equals("1 (+1)");
            }
        });

        assertThat(perSecondLabel.target.getText(), is("1 (+1)"));

        // This update will reset the per second value to zero
        pricingModel.updateEachSecond();

        ThreadUtils.untilTrue(1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return perSecondLabel.target.getText().equals("1");
            }
        });
    }

    @Test
    public void test_dashboard_updates_when_rows_removed() throws InterruptedException {

        LoggingFrontendModel model = dsl.getSwingFrontEnd().getModel();
        EnvironmentModel pricingModel = model.getEnvironment("Pricing");

        pricingModel.onNewLogEvent(LogEventBuilder.start().setLevel(Level.WARNING.intValue()).toLogEvent());

        final JLabelFixture label = dsl.getFrameFixture()
                                       .panel("DashboardPanel")
                                       .panel("EnvironmentSummaryPanel-Pricing")
                                       .panel("warningIndicatorResizingLabel")
                                       .label("valueResizingLabel");
        ThreadUtils.untilTrue(1, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return label.target.getText().equals("1 (+1)");
            }
        });
        assertThat(label.target.getText(), is("1 (+1)"));

        dsl.clickTableRow("Pricing", 0, 0);
        dsl.pressDelete("Pricing");

        dsl.waitForSwingQueueToFlush();
        pricingModel.updateEachSecond();
        dsl.waitForSwingQueueToFlush();

        assertThat(label.target.getText(), is("0"));
    }
}
