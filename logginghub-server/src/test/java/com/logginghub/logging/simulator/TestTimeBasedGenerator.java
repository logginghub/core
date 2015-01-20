package com.logginghub.logging.simulator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.simulator.TimeBasedGenerator;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;

@Ignore// This is a shitty test, its based on some sleep calls working out well from a timing
       // perspective. This doesn't fit with our rules on unit tests.
public class TestTimeBasedGenerator {

    @Test public void testGetCount() throws Exception {
        TimeBasedGenerator generator = new TimeBasedGenerator();
        FixedTimeProvider time = new FixedTimeProvider(0);
        generator.setTimeProvider(time);

        assertThat(generator.getCount(), is(1970 + 1 + 1 + 0 + 0 + 0));
    }

    @Test public void testGetCountChars() throws Exception {
        TimeBasedGenerator generator = new TimeBasedGenerator();
        FixedTimeProvider time = new FixedTimeProvider(0);
        generator.setTimeProvider(time);

        assertThat(generator.getCountChars(), is(1 + 9 + 7 + 0 + 1 + 1 + 0 + 0 + 0));
    }

    @Test public void testGenerator() {
        TimeBasedGenerator generator = new TimeBasedGenerator();
        FixedTimeProvider time = new FixedTimeProvider(0);
        generator.setTimeProvider(time);

        LogEventBucket bucket = new LogEventBucket();
        generator.getEventMultiplexer().addDestination(bucket);

        generator.startStatsThread();
        generator.startTargetRateThread();
        generator.startGeneratorThread();

        // generator.getEventMultiplexer().addDestination(new Destination<LogEvent>() {
        // @Override public void send(LogEvent t) {
        // System.out.println(t.getMessage());
        // }
        // });

        ThreadUtils.sleep(2000);
        assertThat((double) bucket.size(), is(closeTo(38, 10)));

        generator.stop();
    }

    @Test public void testGeneratorScaled() {
        TimeBasedGenerator generator = new TimeBasedGenerator();
        FixedTimeProvider time = new FixedTimeProvider(0);
        generator.setTimeProvider(time);
        generator.setScaleFactor(100);

        LogEventBucket bucket = new LogEventBucket();
        generator.getEventMultiplexer().addDestination(bucket);

        // generator.getEventMultiplexer().addDestination(new Destination<LogEvent>() {
        // @Override public void send(LogEvent t) {
        // System.out.println(t.getMessage());
        // }
        // });

        generator.startStatsThread();
        generator.startTargetRateThread();
        generator.startGeneratorThread();

        ThreadUtils.sleep(2000);
        assertThat((double) bucket.size(), is(closeTo(3800, 1000)));

        generator.stop();
    }

    @Test public void testRandomisationRanges() {

        TimeBasedGenerator generator = new TimeBasedGenerator();
        generator.setupRanges();

        assertThat(generator.getDuration(0d), is(1d));
        assertThat(generator.getDuration(50d), is(2.5d));
        assertThat(generator.getDuration(99.9999d), is(closeTo(4d, 0.1d)));

        // For this range the first 50% has no randomisation, and the second 50% has +/- 1 variation
        assertThat(generator.getDuration(100d), is(4d));
        assertThat(generator.getDuration(200d), is(5.5d));

        double variation = ((350d - 100d) / (500d - 100d)) * 2;
        assertThat(generator.getDuration(350d), is(closeTo(7.0d, variation)));
        assertThat(generator.getDuration(499.9999d), is(closeTo(10d, 1d)));

        // ranges.add(new Range(100, 500, 4, 10, new int[] { 0 }));

    }

    @Test public void testFlatOutMode() {

        TimeBasedGenerator generator = new TimeBasedGenerator();
        generator.setupRanges();

        LogEventBucket bucket = new LogEventBucket();
        generator.getEventMultiplexer().addDestination(bucket);

        // generator.getEventMultiplexer().addDestination(new Destination<LogEvent>() {
        // @Override public void send(LogEvent t) {
        // System.out.println(t);
        // }
        // });

        generator.generate(TimeUtils.parseTime("1/1/1970 00:00:00"), TimeUtils.parseTime("1/1/1970 01:00:00"));

        assertThat(bucket.size(), is(118800));
        assertThat(bucket.get(0).getOriginTime(), is(0L));
        assertThat((double) bucket.get(bucket.size() - 1).getOriginTime(), is(closeTo(TimeUtils.hours(1), 100)));
    }
}
