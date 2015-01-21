package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StreamingDestination;
import com.logginghub.utils.logging.Logger;

public class TestPatternHistoryModule extends BaseHub {

    private SocketClient clientA;
    private HubFixture hubFixture;
    private SocketHub hub;

    @Before public void setup() throws ConnectorException {

        hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                        HubTestFixture.Features.PatternManager,
                                                        HubTestFixture.Features.Patterniser,
                                                        HubTestFixture.Features.PatternDiskHistory));
        hub = hubFixture.start();

        clientA = fixture.createClient("clientA", hub);
    }

    @Test public void test_historical_pattern_request() throws ConnectorException, LoggingMessageSenderException, InterruptedException {

        final Bucket<PatternisedLogEvent> realTimePatternisedEventBucket = new Bucket<PatternisedLogEvent>();

        clientA.addSubscription(Channels.getPatternisedStream(0), new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                PatternisedLogEvent event = (PatternisedLogEvent) t.getPayload();
                realTimePatternisedEventBucket.add(event);
            }
        });
        
//        clientA.setDebug(true);

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Pattern patternTemplate = new Pattern("pattern1", "pattern {value}");

        assertThat(patternManagementAPI.createPattern(patternTemplate).isSuccessful(), is(true));

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

        final Bucket<PatternisedLogEvent> historicalPatternisedEventBucket = new Bucket<PatternisedLogEvent>();

        final CountDownLatch latch = new CountDownLatch(1);

        // Need to sleep here to detail with the various thread pools that will take the event to the disk
        Thread.sleep(500);
        
        clientA.getHistoricalDataAPI().streamHistoricalPatternisedEvents(0L, 10L, new StreamingDestination<PatternisedLogEvent>() {
            @Override public void send(PatternisedLogEvent t) {
                historicalPatternisedEventBucket.add(t);
            }

            @Override public void onStreamComplete() {
                latch.countDown();
            }
        });
        
        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
        
        historicalPatternisedEventBucket.waitForMessages(1);
        assertThat(historicalPatternisedEventBucket.size(), is(1));
        assertThat(historicalPatternisedEventBucket.get(0).getPatternID(), is(0));
        assertThat(historicalPatternisedEventBucket.get(0).getSourceHost(), is("host1"));
        assertThat(historicalPatternisedEventBucket.get(0).getTime(), is(1L));
        assertThat(historicalPatternisedEventBucket.get(0).getLevel(), is(Logger.warning));
        assertThat(historicalPatternisedEventBucket.get(0).getVariable(0), is("10"));
        assertThat(historicalPatternisedEventBucket.get(0).getVariables(), is(new String[] { "10" }));

    }

}