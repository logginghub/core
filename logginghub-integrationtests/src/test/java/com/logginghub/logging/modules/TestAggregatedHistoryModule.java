package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
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
import com.logginghub.utils.StreamingDestination;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.logging.Logger;

public class TestAggregatedHistoryModule extends BaseHub {

    private SocketClient clientA;
    private HubFixture hubFixture;
    private SocketHub hub;

    @Before public void setup() throws ConnectorException {

        hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                        HubTestFixture.Features.PatternManager,
                                                        HubTestFixture.Features.Patterniser,
                                                        HubTestFixture.Features.Aggregator,
                                                        HubTestFixture.Features.AggregatedDiskHistory));
        hub = hubFixture.start();

        clientA = fixture.createClient("clientA", hub);
    }

    @Test public void test_historical_aggregated_data_request() throws ConnectorException, LoggingMessageSenderException, InterruptedException {

        final Bucket<PatternisedLogEvent> realTimePatternisedEventBucket = new Bucket<PatternisedLogEvent>();
        final Bucket<AggregatedLogEvent> realTimeAggregatedEventBucket = new Bucket<AggregatedLogEvent>();

        clientA.addSubscription(Channels.getPatternisedStream(0), new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                PatternisedLogEvent event = (PatternisedLogEvent) t.getPayload();
                realTimePatternisedEventBucket.add(event);
            }
        });

        clientA.addSubscription(Channels.getAggregatedStream(0), new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                AggregatedLogEvent event = (AggregatedLogEvent) t.getPayload();
                realTimeAggregatedEventBucket.add(event);
            }
        });
        
//        clientA.setDebug(true);

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Pattern patternTemplate = new Pattern("pattern1", "pattern {value}");

        assertThat(patternManagementAPI.createPattern(patternTemplate).isSuccessful(), is(true));

        Aggregation aggregationTemplate = new Aggregation();
        aggregationTemplate.setCaptureLabelIndex(0);
        aggregationTemplate.setGroupBy("{event.sourceHost}");
        aggregationTemplate.setInterval(1000);
        aggregationTemplate.setPatternID(patternTemplate.getPatternId());
        aggregationTemplate.setType(AggregationType.TotalSum);

        assertThat(patternManagementAPI.createAggregation(aggregationTemplate).isSuccessful(), is(true));
        
        LogEvent event1 = LogEventBuilder.start()
                                         .setLevel(Logger.warning)
                                         .setLocalCreationTimeMillis(1)
                                         .setSourceHost("host1")
                                         .setMessage("pattern 10")
                                         .toLogEvent();

        clientA.send(new LogEventMessage(event1));

        realTimePatternisedEventBucket.waitForMessages(1);

        assertThat(realTimePatternisedEventBucket.size(), is(1));
        assertThat(realTimePatternisedEventBucket.get(0).getPatternID(), is(0));
        assertThat(realTimePatternisedEventBucket.get(0).getSourceHost(), is("host1"));
        assertThat(realTimePatternisedEventBucket.get(0).getTime(), is(1L));
        assertThat(realTimePatternisedEventBucket.get(0).getLevel(), is(Logger.warning));
        assertThat(realTimePatternisedEventBucket.get(0).getVariable(0), is("10"));
        assertThat(realTimePatternisedEventBucket.get(0).getVariables(), is(new String[] { "10" }));
        
        realTimeAggregatedEventBucket.waitForMessages(1);

        assertThat(realTimeAggregatedEventBucket.size(), is(1));
        assertThat(realTimeAggregatedEventBucket.get(0).getAggregationID(), is(0));
        assertThat(realTimeAggregatedEventBucket.get(0).getSeriesKey(), is("host1"));
        assertThat(realTimeAggregatedEventBucket.get(0).getTime(), is(not(0L)));
        assertThat(realTimeAggregatedEventBucket.get(0).getTime(), is(not(1L)));
        assertThat(realTimeAggregatedEventBucket.get(0).getValue(), is(10d));

        // Now do the historical stuff
        ThreadUtils.sleep(500);
        
        final Bucket<AggregatedLogEvent> historicalAggregatedEventBucket = new Bucket<AggregatedLogEvent>();

        final CountDownLatch latch = new CountDownLatch(1);
        clientA.getHistoricalDataAPI().streamHistoricalAggregatedEvents(0, Long.MAX_VALUE, new StreamingDestination<AggregatedLogEvent>() {
            @Override public void send(AggregatedLogEvent t) {
                historicalAggregatedEventBucket.add(t);
            }

            @Override public void onStreamComplete() {
                latch.countDown();
            }
        });

        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));

        historicalAggregatedEventBucket.waitForMessages(1);
        assertThat(historicalAggregatedEventBucket.size(), is(1));
        assertThat(historicalAggregatedEventBucket.get(0).getAggregationID(), is(0));
        assertThat(historicalAggregatedEventBucket.get(0).getSeriesKey(), is("host1"));
        assertThat(historicalAggregatedEventBucket.get(0).getTime(), is(not(0L)));
        assertThat(historicalAggregatedEventBucket.get(0).getTime(), is(not(1L)));
        assertThat(historicalAggregatedEventBucket.get(0).getValue(), is(10d));

    }

}