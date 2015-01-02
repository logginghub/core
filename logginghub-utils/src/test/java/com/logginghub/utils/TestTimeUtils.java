package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.utils.TimeUtils;

public class TestTimeUtils {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void testToMilliseconds() throws Exception {

        assertThat(TimeUtils.toMilliseconds(1, "sec"), is(1000L));
        assertThat(TimeUtils.toMilliseconds(1, "s"), is(1000L));
        assertThat(TimeUtils.toMilliseconds(1, "second"), is(1000L));

        assertThat(TimeUtils.toMilliseconds(10, "sunsets"), is(10000L));

        assertThat(TimeUtils.toMilliseconds(1, "ms"), is(1L));
        assertThat(TimeUtils.toMilliseconds(1, "millis"), is(1L));
        assertThat(TimeUtils.toMilliseconds(1, "millisecond"), is(1L));
        assertThat(TimeUtils.toMilliseconds(1, "milliseconds"), is(1L));

        assertThat(TimeUtils.toMilliseconds(1, "microseconds"), is(0L));
        assertThat(TimeUtils.toMilliseconds(1, "mu"), is(0L));

        assertThat(TimeUtils.toMilliseconds(1, "nanoseconds"), is(0L));
        assertThat(TimeUtils.toMilliseconds(1, "ns"), is(0L));

        assertThat(TimeUtils.toMilliseconds(1, "m"), is(60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "min"), is(60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "mins"), is(60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "minute"), is(60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "minutes"), is(60 * 1000L));

        assertThat(TimeUtils.toMilliseconds(1, "h"), is(60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "hr"), is(60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "hour"), is(60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "hours"), is(60 * 60 * 1000L));

        assertThat(TimeUtils.toMilliseconds(1, "d"), is(24 * 60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "day"), is(24 * 60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(1, "days"), is(24 * 60 * 60 * 1000L));
        assertThat(TimeUtils.toMilliseconds(2, "days"), is(2 * 24 * 60 * 60 * 1000L));
    }

    @Test public void testParseInterval() {

        assertThat(TimeUtils.parseInterval("1 ms"), is(1L));
        assertThat(TimeUtils.parseInterval("1ms"), is(1L));
        assertThat(TimeUtils.parseInterval("1s"), is(1000L));
        assertThat(TimeUtils.parseInterval("1"), is(1L));

        assertThat(TimeUtils.parseInterval("10000"), is(10000L));
        assertThat(TimeUtils.parseInterval("1 min 30 seconds"), is(90 * 1000L));
        assertThat(TimeUtils.parseInterval("2 days, 10 minutes and 1 second"), is(2 * TimeUtils.days + 10 * TimeUtils.minutes + 1000));
    }

    @Test public void test_parse_interval_failure_1() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("We couldn't understand the interval value 'foo'");
        TimeUtils.parseInterval("foo");
    }

    @Test public void test_parse_interval_failure_2() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("We couldn't understand the interval value '1 min 30'");
        TimeUtils.parseInterval("1 min 30");
    }

    @Test public void testFormatIntervalMilliseconds() throws Exception {
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.hours(2) + TimeUtils.minutes(5) + TimeUtils.seconds(1)), is("125 minutes 1 second"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.minutes(5) + TimeUtils.seconds(1)), is("5 minutes 1 second"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.minutes(1) + TimeUtils.seconds(1)), is("1 minute 1 second"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.minutes(2) + TimeUtils.seconds(2)), is("2 minutes 2 seconds"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.seconds(1)), is("1 second"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.seconds(2)), is("2 seconds"));
        assertThat(TimeUtils.formatIntervalMilliseconds(TimeUtils.seconds(60)), is("1 minute"));
        assertThat(TimeUtils.formatIntervalMilliseconds(1234), is("1.23 seconds"));
        assertThat(TimeUtils.formatIntervalMilliseconds(123), is("123 ms"));
    }

    @Test public void testFormatIntervalMillisecondsCompact() throws Exception {
        assertThat(TimeUtils.formatIntervalMillisecondsCompact(TimeUtils.seconds(1)), is("1second"));
        assertThat(TimeUtils.formatIntervalMillisecondsCompact(TimeUtils.minutes(10)), is("10minutes"));
    }

    @Test public void testParseTimePart() throws Exception {
        assertThat(TimeUtils.parseTimePart("1"), is(TimeUtils.hours(1)));
        assertThat(TimeUtils.parseTimePart("1:1"), is(TimeUtils.hours(1) + TimeUtils.minutes(1)));
        assertThat(TimeUtils.parseTimePart("1:2:3"), is(TimeUtils.hours(1) + TimeUtils.minutes(2) + TimeUtils.seconds(3)));        
        assertThat(TimeUtils.parseTimePart("1:2:3.4"), is(TimeUtils.hours(1) + TimeUtils.minutes(2) + TimeUtils.seconds(3) + 4));
        
        assertThat(TimeUtils.parseTimePart("10:02:34.567"), is(TimeUtils.hours(10) + TimeUtils.minutes(2) + TimeUtils.seconds(34) + 567));
    }
}
