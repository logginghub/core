package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeModel;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeView;
import com.logginghub.logging.frontend.views.logeventdetail.time.TimeViewComponent;
import com.logginghub.utils.FixedTimeProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestDetailedLogEventTablePanel {

    private SwingFrontEndDSL dsl;

    @Before public void create() throws IOException {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void cleanup() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }

    @Ignore // jshaw - not working on os x
    @Test public void test_time_view_scrolling() throws Exception {

        long startTime = System.currentTimeMillis();

        TimeView timeView = dsl.getDetailedLogEventTablePanel("default").getTimeView();
        TimeViewComponent timeViewComponent = timeView.getTimeViewComponent();
        FixedTimeProvider timeProvider = new FixedTimeProvider(startTime);
        timeView.setTimeProvider(timeProvider);

        TimeModel model = timeViewComponent.getController().getModel();
        assertThat(model.getAutoscroll().asBoolean(), is(true));

        long startViewTime = model.getViewStart().longValue();

        int updates = 1000;
        long time = startTime;
        for (int i = 0; i < updates; i++) {
            time += (i * 1000);
            timeProvider.setTime(time);

            if (i % 10 == 0) {
                dsl.sleep(5);
            }
        }
        
        // We need to make sure those updates have all played through
        dsl.sleep(500);

        long endViewTime = model.getViewStart().longValue();
        assertThat(endViewTime, is(greaterThan(startViewTime)));

        // Select something on the chart
        assertThat(model.getViewStart().longValue(), is(endViewTime));
        dsl.dragTimeRange("default", 100, 9, 300, 9);
        assertThat(model.getViewStart().longValue(), is(endViewTime));

        // Make sure autoscroll is now off
        assertThat(model.getAutoscroll().asBoolean(), is(false));

        // Make sure the view doesn't move
        for (int i = 0; i < updates; i++) {
            time += (i * 1000);
            timeProvider.setTime(time);

            if (i % 10 == 0) {
                dsl.sleep(5);
            }
        }

        // We need to make sure those updates have all played through
        dsl.sleep(500);
        
        assertThat(model.getViewStart().longValue(), is(endViewTime));
        
        // Right-click should clear the selection and go back to "play" mode, with autoscroll
        dsl.clearTimeRange("default");

        // Make sure autoscroll is now on
        assertThat(model.getAutoscroll().asBoolean(), is(true));

        // The screen should have updated back to now
        assertThat(model.getViewStart().longValue(), is(greaterThan(endViewTime)));
        
        endViewTime = model.getViewStart().longValue();
        
        // And we should scroll again
        for (int i = 0; i < updates; i++) {
            time += (i * 1000);
            timeProvider.setTime(time);

            if (i % 10 == 0) {
                dsl.sleep(5);
            }
        }
        
        assertThat(model.getViewStart().longValue(), is(greaterThan(endViewTime)));
    }
}
