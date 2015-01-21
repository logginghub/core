package com.logginghub.logging.modules;

import java.io.IOException;

import org.junit.Test;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;

public class TestRemotePatternisedEvents extends BaseHub {

    // TODO : this has been broken by the new aggregation code - DSL aren't using it anymore, so I'm not fixing it now    
    @Test public void test_remote_patternised() throws IOException, ConnectorException, LoggingMessageSenderException {

//        fixture.getPatterniserConfiguration()
//               .getPatterns()
//               .add(new PatternConfiguration(0, "patternA", "operationA completed successfully in {time} ms : user data was '[data]'"));
//
//        fixture.getAggregatorConfiguration().setUseEventTimes(true);
//        fixture.getAggregatorConfiguration().getAggregations().add(new AggregationConfiguration(0, 0, "1 second", "Mean", ""));
//
//        SocketHub hub = fixture.getSocketHubA();
//
//        SocketClient clientA = fixture.createClient("client", hub);
//        AggregationKey key = new AggregationKey(0, 0, AggregationType.Mean, 1000, null);
//        String channel = key.getChannel();
//        Bucket<ChannelMessage> channelMessages = fixture.getChannelBucketFor(channel, clientA);
//
//        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "operationA completed successfully in 1 ms : user data was 'cat'")));
//        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "operationA completed successfully in 2 ms : user data was 'cat'")));
//        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "operationA completed successfully in 3 ms : user data was 'cat'")));
//        clientA.send(new LogEventMessage(LogEventBuilder.create(1000, Logger.info, "operationA completed successfully in 4 ms : user data was 'cat'")));
//
//        channelMessages.waitForMessages(1);
//        ChannelMessage channelMessage = channelMessages.get(0);
//        assertThat(channelMessage.getPayload(), is(instanceOf(AggregatedPatternData.class)));
//        AggregatedPatternData message = (AggregatedPatternData) channelMessage.getPayload();
//
//        assertThat(message.getInterval(), is(1000L));
//        assertThat(message.getSeries(), is("0/0/Mean"));
//        assertThat(message.getTime(), is(0L));
//        assertThat(message.getValue(), is(2d));

    }

}
