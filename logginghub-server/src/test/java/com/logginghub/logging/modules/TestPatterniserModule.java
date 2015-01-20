package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatterniserModule;
import com.logginghub.logging.modules.configuration.PatternConfiguration;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.utils.Bucket;

public class TestPatterniserModule {

    @Test public void test_send() throws Exception {

        PatterniserConfiguration configuration = new PatterniserConfiguration();
        configuration.setUseQueue(false);
        configuration.getPatterns().add(new PatternConfiguration(0, "patternA", "I'm a log event {time} with some variables '[user]'"));
        PatterniserModule patterniser = LoggingContainer.fromConfiguration(configuration, PatterniserModule.class);

        Bucket<PatternisedLogEvent> results = new Bucket<PatternisedLogEvent>();
        patterniser.addDestination(results);

        LogEvent logEvent = LogEventBuilder.start().setMessage("I'm a log event 12.3 with some variables 'foo'").setLocalCreationTimeMillis(10).toLogEvent();
        patterniser.send(logEvent);
        
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getTime(), is(10L));
        assertThat(results.get(0).getPatternID(), is(0));
        assertThat(results.get(0).getVariables()[0], is("12.3"));
        assertThat(results.get(0).getVariables()[1], is("foo"));
    }
    
    @Test public void test_send_with_queue() throws Exception {

        PatterniserConfiguration configuration = new PatterniserConfiguration();
        
        configuration.getPatterns().add(new PatternConfiguration(0, "patternA", "I'm a log event {time} with some variables '[user]'"));
        PatterniserModule patterniser = LoggingContainer.fromConfiguration(configuration, PatterniserModule.class);
        patterniser.start();

        Bucket<PatternisedLogEvent> results = new Bucket<PatternisedLogEvent>();
        patterniser.addDestination(results);

        LogEvent logEvent = LogEventBuilder.start().setMessage("I'm a log event 12.3 with some variables 'foo'").setLocalCreationTimeMillis(10).toLogEvent();
        patterniser.send(logEvent);
        
        patterniser.waitForQueueToDrain();
        
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getTime(), is(10L));
        assertThat(results.get(0).getPatternID(), is(0));
        assertThat(results.get(0).getVariables().length, is(2));
        assertThat(results.get(0).getVariables()[0], is("12.3"));
        assertThat(results.get(0).getVariables()[1], is("foo"));
    }
    
    
    @Test public void test_send_all_destinations() throws Exception {

        PatterniserConfiguration configuration = new PatterniserConfiguration();
        configuration.setUseQueue(false);
        configuration.setMatchAgainstAllPatterns(true);
        configuration.getPatterns().add(new PatternConfiguration(0, "patternA", "I'm a log event {time} with some variables '[user]'"));
        configuration.getPatterns().add(new PatternConfiguration(1, "patternB", "I'm a log event {something else called time} with some variables '[user]'"));
        PatterniserModule patterniser = LoggingContainer.fromConfiguration(configuration, PatterniserModule.class);

        Bucket<PatternisedLogEvent> results = new Bucket<PatternisedLogEvent>();
        patterniser.addDestination(results);

        LogEvent logEvent = LogEventBuilder.start().setMessage("I'm a log event 12.3 with some variables 'foo'").setLocalCreationTimeMillis(10).toLogEvent();
        patterniser.send(logEvent);
        
        assertThat(results.size(), is(2));
        
        assertThat(results.get(0).getTime(), is(10L));
        assertThat(results.get(0).getPatternID(), is(0));
        assertThat(results.get(0).getVariables().length, is(2));
        assertThat(results.get(0).getVariables()[0], is("12.3"));
        assertThat(results.get(0).getVariables()[1], is("foo"));
        
        assertThat(results.get(1).getTime(), is(10L));
        assertThat(results.get(1).getPatternID(), is(1));
        assertThat(results.get(1).getVariables().length, is(2));
        assertThat(results.get(1).getVariables()[0], is("12.3"));
        assertThat(results.get(1).getVariables()[1], is("foo"));

    }
    
    @Test public void test_send_first_destination() throws Exception {

        PatterniserConfiguration configuration = new PatterniserConfiguration();
        configuration.setUseQueue(false);
        configuration.setMatchAgainstAllPatterns(false);
        configuration.getPatterns().add(new PatternConfiguration(0, "patternA", "I'm a log event {time} with some variables '[user]'"));
        configuration.getPatterns().add(new PatternConfiguration(1, "patternB", "I'm a log event {something else called time} with some variables '[user]'"));
        PatterniserModule patterniser = LoggingContainer.fromConfiguration(configuration, PatterniserModule.class);

        Bucket<PatternisedLogEvent> results = new Bucket<PatternisedLogEvent>();
        patterniser.addDestination(results);

        LogEvent logEvent = LogEventBuilder.start().setMessage("I'm a log event 12.3 with some variables 'foo'").setLocalCreationTimeMillis(10).toLogEvent();
        patterniser.send(logEvent);
        
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getTime(), is(10L));
        assertThat(results.get(0).getPatternID(), is(0));
        assertThat(results.get(0).getVariables().length, is(2));
        assertThat(results.get(0).getVariables()[0], is("12.3"));
        assertThat(results.get(0).getVariables()[1], is("foo"));

    }


}
