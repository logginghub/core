package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.configuration.AggregationConfiguration;
import com.logginghub.logging.modules.configuration.AggregatorConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.Source;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestAggregatorModule {

    private LogEvent logEvent1;
    private PatternisedLogEvent patternisedEvent1;

    private LogEvent logEvent2;
    private PatternisedLogEvent patternisedEvent2;

    private LogEvent logEvent3;
    private PatternisedLogEvent patternisedEvent3;

    private LogEvent logEvent4;
    private PatternisedLogEvent patternisedEvent4;

    private SimplePatternManager patternManagerService;
    private AggregatorModule aggregator;
    private ConfigurableServiceDiscovery serviceDiscovery;
    private AggregatorConfiguration configuration;

    @Before public void setup() {
        patternManagerService = new SimplePatternManager();
        patternManagerService.addPattern(new Pattern(0, "pattern0", "pattern {variable}" ));

        logEvent1 = LogEventBuilder.start().setMessage("I'm a log event 1.2 with some variables 'James'").setLocalCreationTimeMillis(10).toLogEvent();
        logEvent2 = LogEventBuilder.start()
                                   .setMessage("I'm a log event 2.3 with some variables 'Sarah'")
                                   .setLocalCreationTimeMillis(900)
                                   .toLogEvent();
        logEvent3 = LogEventBuilder.start()
                                   .setMessage("I'm a log event 3.4 with some variables 'James'")
                                   .setLocalCreationTimeMillis(1500)
                                   .toLogEvent();
        logEvent4 = LogEventBuilder.start()
                                   .setMessage("I'm a log event 4.5 with some variables 'Chris'")
                                   .setLocalCreationTimeMillis(2300)
                                   .toLogEvent();

        patternisedEvent1 = new PatternisedLogEvent(Logger.info, logEvent1.getOriginTime(), 0, 0, "TestApplication");
        patternisedEvent1.setVariables(new String[] { "1.2", "James" });

        patternisedEvent2 = new PatternisedLogEvent(Logger.info, logEvent2.getOriginTime(), 0, 0, "TestApplication");
        patternisedEvent2.setVariables(new String[] { "2.3", "Sarah" });

        patternisedEvent3 = new PatternisedLogEvent(Logger.info, logEvent3.getOriginTime(), 0, 0, "TestApplication");
        patternisedEvent3.setVariables(new String[] { "3.4", "James" });

        patternisedEvent4 = new PatternisedLogEvent(Logger.info, logEvent4.getOriginTime(), 0, 0, "TestApplication");
        patternisedEvent4.setVariables(new String[] { "4.5", "Chris" });

        configuration = new AggregatorConfiguration();
        serviceDiscovery = new ConfigurableServiceDiscovery();
        aggregator = new AggregatorModule();
        
        serviceDiscovery.bind(Source.class, PatternisedLogEvent.class, Mockito.mock(Source.class));
        serviceDiscovery.bind(ChannelMessagingService.class, Mockito.mock(ChannelMessagingService.class));
        serviceDiscovery.bind(PatternManagerService.class, patternManagerService);
    }

    @Test public void testSend() throws Exception {

        
        configuration.getAggregations().add(new AggregationConfiguration(0, 0, 0, "1 second", "Mean", ""));

        FixedTimeProvider time = new FixedTimeProvider(0);

        aggregator.configure(configuration, serviceDiscovery);
        
        aggregator.setTimeProvider(time);
        aggregator.setPatternManager(patternManagerService);

        Bucket<AggregatedLogEvent> results = new Bucket<AggregatedLogEvent>();

        aggregator.addDestination(results);

        // This will open a new interval for the 0-1000ms
        time.setTime(patternisedEvent1.getTime());
        aggregator.send(patternisedEvent1);
        assertThat(results.size(), is(0));

        // This will update that interval
        time.setTime(patternisedEvent2.getTime());
        aggregator.send(patternisedEvent2);
        assertThat(results.size(), is(0));

        // This one will close the first seconds data
        time.setTime(patternisedEvent3.getTime());
        aggregator.send(patternisedEvent3);
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getAggregationID(), is(0));
        assertThat(results.get(0).getTime(), is(0L));
        assertThat(results.get(0).getValue(), is(1.75));

    }

    @Test public void test_event_parts() throws Exception {

        configuration.getAggregations().add(new AggregationConfiguration(0, 0, 0, "1 second", "Mean", "{event.sourceApplication}"));

        FixedTimeProvider time = new FixedTimeProvider(0);

        aggregator.configure(configuration, serviceDiscovery);
        aggregator.setTimeProvider(time);

        Bucket<AggregatedLogEvent> results = new Bucket<AggregatedLogEvent>();

        aggregator.addDestination(results);

        // This will open a new interval for the 0-1000ms
        time.setTime(patternisedEvent1.getTime());
        aggregator.send(patternisedEvent1);
        assertThat(results.size(), is(0));

        // This will update that interval
        time.setTime(patternisedEvent2.getTime());
        aggregator.send(patternisedEvent2);
        assertThat(results.size(), is(0));

        // This one will close the first seconds data
        time.setTime(patternisedEvent3.getTime());
        aggregator.send(patternisedEvent3);
        assertThat(results.size(), is(1));

        assertThat(results.get(0).getAggregationID(), is(0));
        assertThat(results.get(0).getTime(), is(0L));
        assertThat(results.get(0).getValue(), is(1.75));
    }

    @Test public void test_time_expire() throws Exception {
       
        configuration.getAggregations().add(new AggregationConfiguration(0, 0, 0, "1 second", "Mean", "{event.sourceApplication}"));

        FixedTimeProvider time = new FixedTimeProvider(0);

        aggregator.configure(configuration, serviceDiscovery);
        aggregator.setTimeProvider(time);
        aggregator.setPatternManager(patternManagerService);
        
        Bucket<AggregatedLogEvent> results = new Bucket<AggregatedLogEvent>();

        aggregator.addDestination(results);
        aggregator.start();

        // This will open a new interval for the 0-1000ms
        time.setTime(patternisedEvent1.getTime());
        aggregator.send(patternisedEvent1);
        assertThat(results.size(), is(0));

        // Kick the time forwards and wait for the timer to kick in
        time.setTime(TimeUtils.parseInterval("2 seconds"));

        results.waitForMessages(1);
        assertThat(results.get(0).getAggregationID(), is(0));
        assertThat(results.get(0).getTime(), is(0L));
        assertThat(results.get(0).getValue(), is(1.2));
    }

}
