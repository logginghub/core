package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatternManagerModule;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FixedTimeProvider;

public class TestAggregatorCore {

    private AggregatorCore aggregatorCore;

    @Before public void setup() {
        PatternManagerModule patternManager = new PatternManagerModule();
        Pattern pattern = new Pattern();
        pattern.setPattern("");
        pattern.setPatternId(2);
        patternManager.addPattern(pattern);
        aggregatorCore = new AggregatorCore(patternManager);
    }

    @Test public void test_simple_force_interval_end() {

        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        aggregatorCore.setTimeProvider(timeProvider);

        Aggregation aggregation1 = new Aggregation();
        aggregation1.setAggregationID(5);
        aggregation1.setPatternID(2);
        aggregation1.setCaptureLabelIndex(1);
        aggregation1.setInterval(1000);
        aggregation1.setGroupBy(null);
        aggregation1.setType(AggregationType.Count);

        aggregatorCore.addAggregation(aggregation1);

        Bucket<AggregatedLogEvent> bucket = new Bucket<AggregatedLogEvent>();
        aggregatorCore.addDestination(bucket);

        PatternisedLogEvent patternised1 = LogEventFactory.createPatternisedEvent();
        patternised1.setPatternID(2);
        patternised1.setTime(1000);
        patternised1.setVariables(new String[] { "a", "b", "c" });

        timeProvider.setTime(1000);
        aggregatorCore.send(patternised1);

        timeProvider.setTime(4000);
        aggregatorCore.checkOpenIntervals();

        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getAggregationID(), is(5));
        assertThat(bucket.get(0).getSeriesKey(), is("/1/Count"));
        assertThat(bucket.get(0).getTime(), is(1000L));
        assertThat(bucket.get(0).getValue(), is(1d));
    }

    @Test public void test_simple_normal_interval_end() {

        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        aggregatorCore.setTimeProvider(timeProvider);

        Aggregation aggregation1 = new Aggregation();
        aggregation1.setAggregationID(5);
        aggregation1.setPatternID(2);
        aggregation1.setCaptureLabelIndex(1);
        aggregation1.setInterval(1000);
        aggregation1.setGroupBy(null);
        aggregation1.setType(AggregationType.Count);

        aggregatorCore.addAggregation(aggregation1);

        Bucket<AggregatedLogEvent> bucket = new Bucket<AggregatedLogEvent>();
        aggregatorCore.addDestination(bucket);

        PatternisedLogEvent patternised1 = LogEventFactory.createPatternisedEvent();
        patternised1.setPatternID(2);
        patternised1.setTime(500);
        patternised1.setVariables(new String[] { "a", "b", "c" });

        PatternisedLogEvent patternised2 = LogEventFactory.createPatternisedEvent();
        patternised2.setPatternID(2);
        patternised2.setTime(1500);
        patternised2.setVariables(new String[] { "a", "b", "c" });

        timeProvider.setTime(500);
        aggregatorCore.send(patternised1);

        timeProvider.setTime(1500);
        aggregatorCore.send(patternised2);

        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getAggregationID(), is(5));
        assertThat(bucket.get(0).getSeriesKey(), is("/1/Count"));
        assertThat(bucket.get(0).getTime(), is(0L));
        assertThat(bucket.get(0).getValue(), is(1d));
    }

    @Test public void test_grouped_force_interval_end() {

        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        aggregatorCore.setTimeProvider(timeProvider);

        Aggregation aggregation1 = new Aggregation();
        aggregation1.setAggregationID(5);
        aggregation1.setPatternID(2);
        aggregation1.setCaptureLabelIndex(1);
        aggregation1.setInterval(1000);
        aggregation1.setGroupBy("This is the series key {0} {1} {2} {event.sourceHost} {event.level}");
        aggregation1.setType(AggregationType.Count);

        aggregatorCore.addAggregation(aggregation1);

        Bucket<AggregatedLogEvent> bucket = new Bucket<AggregatedLogEvent>();
        aggregatorCore.addDestination(bucket);

        PatternisedLogEvent patternised1 = LogEventFactory.createPatternisedEvent();
        patternised1.setPatternID(2);
        patternised1.setTime(1000);
        patternised1.setVariables(new String[] { "a", "b", "c" });

        timeProvider.setTime(1000);
        aggregatorCore.send(patternised1);

        timeProvider.setTime(4000);
        aggregatorCore.checkOpenIntervals();

        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getAggregationID(), is(5));
        assertThat(bucket.get(0).getSeriesKey(), is("This is the series key a b c sourceHost info"));
        assertThat(bucket.get(0).getTime(), is(1000L));
        assertThat(bucket.get(0).getValue(), is(1d));
    }

    @Test public void test_grouped_multiple_forced() {

        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        aggregatorCore.setTimeProvider(timeProvider);

        Aggregation aggregation1 = new Aggregation();
        aggregation1.setAggregationID(5);
        aggregation1.setPatternID(2);
        aggregation1.setCaptureLabelIndex(1);
        aggregation1.setInterval(1000);
        aggregation1.setGroupBy("{0} {1}");
        aggregation1.setType(AggregationType.Count);

        aggregatorCore.addAggregation(aggregation1);

        Bucket<AggregatedLogEvent> bucket = new Bucket<AggregatedLogEvent>();
        aggregatorCore.addDestination(bucket);

        PatternisedLogEvent patternised1 = LogEventFactory.createPatternisedEvent();
        patternised1.setPatternID(2);
        patternised1.setTime(1000);
        patternised1.setVariables(new String[] { "a", "b", "c" });

        timeProvider.setTime(1000);
        aggregatorCore.send(patternised1);

        patternised1.setVariables(new String[] { "a", "d", "e" });
        aggregatorCore.send(patternised1);

        patternised1.setVariables(new String[] { "a", "b", "f" });
        aggregatorCore.send(patternised1);

        patternised1.setVariables(new String[] { "b", "a", "z" });
        aggregatorCore.send(patternised1);

        timeProvider.setTime(4000);
        aggregatorCore.checkOpenIntervals();

        assertThat(bucket.size(), is(3));

        assertThat(bucket.get(0).getAggregationID(), is(5));
        assertThat(bucket.get(1).getAggregationID(), is(5));
        assertThat(bucket.get(2).getAggregationID(), is(5));

        assertThat(bucket.get(0).getTime(), is(1000L));
        assertThat(bucket.get(1).getTime(), is(1000L));
        assertThat(bucket.get(2).getTime(), is(1000L));
        
        while (!bucket.isEmpty()) {
            AggregatedLogEvent event = bucket.remove(0);
            if (event.getSeriesKey().equals("a b")) {
                assertThat(event.getValue(), is(2d));                
            }
            else if (event.getSeriesKey().equals("b a")) {
                assertThat(event.getValue(), is(1d));
            }
            else if (event.getSeriesKey().equals("a d")) {
                assertThat(event.getValue(), is(1d));
            }
            else {
                fail("Unexpected");
            }
        }

        
    }

}
