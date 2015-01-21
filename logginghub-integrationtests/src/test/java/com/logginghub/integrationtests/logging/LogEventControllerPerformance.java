package com.logginghub.integrationtests.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.filter.Filter;

public class LogEventControllerPerformance {

    @Ignore // Takes too long
    @Test public void test_add_and_throw_away() {

        LogEventContainerController controller = new LogEventContainerController();

        LogEvent event = LogEventFactory.createFullLogEvent1();
        Filter<LogEvent> filter = new Filter<LogEvent>() {
            @Override public boolean passes(LogEvent event) {
                return true;
            }
        };

        boolean isPlaying = true;

        int repeats = 10;
        
        SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();

        for (int repeat = 0; repeat < repeats; repeat++) {
            int items = 5000000;

            controller.setThreshold(LoggingUtils.sizeof(event) * 1000);

            Stopwatch sw = Stopwatch.start("Add");
            for (int i = 0; i < items; i++) {
                controller.add(event, filter, isPlaying);
            }
                        
            sw.stopAndDump();
            stats.addValue(sw.getDurationMillis());
        }

        stats.doCalculations();
        Out.out("{} ( +/- {} )", stats.getMean(), stats.getAbsoluteDeviation());
        
        assertThat(stats.getMean(), is(lessThan(1900d)));
        assertThat(stats.getAbsoluteDeviation(), is(lessThan(50d)));
        
    }
    
    @Ignore // Takes too long!
    @Test public void test_refilter() {

        LogEventContainerController controller = new LogEventContainerController();

        LogEvent event = LogEventFactory.createFullLogEvent1();
        Filter<LogEvent> filter = new Filter<LogEvent>() {
            @Override public boolean passes(LogEvent event) {
                return true;
            }
        };

        boolean isPlaying = true;

        int repeats = 10;
        
        SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();

        for (int repeat = 0; repeat < repeats; repeat++) {
            int items = 5000000;

            controller.setThreshold(LoggingUtils.sizeof(event) * 1000);

            for (int i = 0; i < items; i++) {
                controller.add(event, filter, isPlaying);
            }
                        
            Stopwatch sw = Stopwatch.start("Refilter");
            controller.refilter(filter);
            sw.stopAndDump();
            stats.addValue(sw.getDurationMillis());
        }

        stats.doCalculations();
        Out.out("{} ( +/- {} )", stats.getMean(), stats.getAbsoluteDeviation());
        
        assertThat(stats.getMean(), is(lessThan(10d)));
        assertThat(stats.getAbsoluteDeviation(), is(lessThan(2d)));
        
    }

}
