package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.EnumSet;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;

public class TestStreamingAggregations extends BaseHub {

    @Test public void test_create_pattern() throws ConnectorException, LoggingMessageSenderException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                                   HubTestFixture.Features.PatternManager,
                                                                   HubTestFixture.Features.Patterniser,
                                                                   HubTestFixture.Features.Aggregator));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);

        final Bucket<PatternisedLogEvent> patternisedEventBucket = new Bucket<PatternisedLogEvent>();
        final Bucket<AggregatedLogEvent> aggregatedEventBucket = new Bucket<AggregatedLogEvent>();

        clientA.addSubscription(Channels.getPatternisedStream(0), new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                PatternisedLogEvent event = (PatternisedLogEvent) t.getPayload();
                patternisedEventBucket.add(event);
            }
        });

        clientA.addSubscription(Channels.getAggregatedStream(0), new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                AggregatedLogEvent event = (AggregatedLogEvent) t.getPayload();
                aggregatedEventBucket.add(event);
            }
        });

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Pattern patternTemplate = new Pattern("pattern1", "pattern {value}");

        assertThat(patternManagementAPI.createPattern(patternTemplate).isSuccessful(), is(true));

        Aggregation aggregationTemplate = new Aggregation();
        aggregationTemplate.setCaptureLabelIndex(0);
        aggregationTemplate.setGroupBy("{event.sourceHost}");
        aggregationTemplate.setInterval(1000);
        aggregationTemplate.setPatternID(patternTemplate.getPatternID());
        aggregationTemplate.setType(AggregationType.TotalSum);

        assertThat(patternManagementAPI.createAggregation(aggregationTemplate).isSuccessful(), is(true));

        LogEvent event1 = LogEventBuilder.start()
                                         .setLevel(Logger.warning)
                                         .setLocalCreationTimeMillis(1)
                                         .setSourceHost("host1")
                                         .setMessage("pattern 10")
                                         .toLogEvent();

        clientA.send(new LogEventMessage(event1));

        patternisedEventBucket.waitForMessages(1);

        assertThat(patternisedEventBucket.size(), is(1));
        assertThat(patternisedEventBucket.get(0).getPatternID(), is(0));
        assertThat(patternisedEventBucket.get(0).getSourceHost(), is("host1"));
        assertThat(patternisedEventBucket.get(0).getTime(), is(1L));
        assertThat(patternisedEventBucket.get(0).getLevel(), is(Logger.warning));
        assertThat(patternisedEventBucket.get(0).getVariable(0), is("10"));
        assertThat(patternisedEventBucket.get(0).getVariables(), is(new String[] { "10" }));

        aggregatedEventBucket.waitForMessages(1);

        assertThat(aggregatedEventBucket.size(), is(1));
        assertThat(aggregatedEventBucket.get(0).getAggregationID(), is(0));
        assertThat(aggregatedEventBucket.get(0).getSeriesKey(), is("host1"));
        // The time should be "now"
        assertThat(aggregatedEventBucket.get(0).getTime(), is(not(0L)));
        assertThat(aggregatedEventBucket.get(0).getTime(), is(not(1L)));
        assertThat(aggregatedEventBucket.get(0).getValue(), is(10d));

        // Send a second event

        LogEvent event2 = LogEventBuilder.start()
                                         .setLevel(Logger.warning)
                                         .setLocalCreationTimeMillis(1)
                                         .setSourceHost("host2")
                                         .setMessage("pattern 15")
                                         .toLogEvent();

        clientA.send(new LogEventMessage(event2));

        patternisedEventBucket.waitForMessages(2);

        assertThat(patternisedEventBucket.size(), is(2));
        assertThat(patternisedEventBucket.get(1).getPatternID(), is(0));
        assertThat(patternisedEventBucket.get(1).getSourceHost(), is("host2"));
        assertThat(patternisedEventBucket.get(1).getTime(), is(1L));
        assertThat(patternisedEventBucket.get(1).getLevel(), is(Logger.warning));
        assertThat(patternisedEventBucket.get(1).getVariable(0), is("15"));
        assertThat(patternisedEventBucket.get(1).getVariables(), is(new String[] { "15" }));

        aggregatedEventBucket.waitForMessages(2);

        assertThat(aggregatedEventBucket.size(), is(2));
        
        assertThat(aggregatedEventBucket.get(0).getAggregationID(), is(0));
        assertThat(aggregatedEventBucket.get(0).getSeriesKey(), is("host1"));
        assertThat(aggregatedEventBucket.get(0).getValue(), is(10d));
        
        assertThat(aggregatedEventBucket.get(1).getAggregationID(), is(0));
        assertThat(aggregatedEventBucket.get(1).getSeriesKey(), is("host2"));
        assertThat(aggregatedEventBucket.get(1).getValue(), is(15d));

    }
}