package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Result;

public class TestPatternModuleClientAPI extends BaseHub {

    @Test public void test_create_pattern() throws ConnectorException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                                   HubTestFixture.Features.PatternManager));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Result<List<Pattern>> patterns = patternManagementAPI.getPatterns();
        assertThat(patterns.isSuccessful(), is(true));
        assertThat(patterns.getValue().size(), is(0));

        Pattern template = new Pattern("pattern1", "Pattern");
        Result<Pattern> createPatternResult = patternManagementAPI.createPattern(template);

        assertThat(createPatternResult.isSuccessful(), is(true));
        assertThat(createPatternResult.getValue().getPatternID(), is(0));
        assertThat(createPatternResult.getValue().getName(), is("pattern1"));
        assertThat(createPatternResult.getValue().getPattern(), is("Pattern"));

        patterns = patternManagementAPI.getPatterns();
        assertThat(patterns.isSuccessful(), is(true));
        assertThat(patterns.getValue().size(), is(1));
        assertThat(patterns.getValue().get(0).getPatternID(), is(0));
        assertThat(patterns.getValue().get(0).getName(), is("pattern1"));
        assertThat(patterns.getValue().get(0).getPattern(), is("Pattern"));

    }

    @Test public void test_create_pattern_same_name() throws ConnectorException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                                   HubTestFixture.Features.PatternManager));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Result<Pattern> createPatternResult1 = patternManagementAPI.createPattern(new Pattern("pattern1", "Pattern"));
        Result<Pattern> createPatternResult2 = patternManagementAPI.createPattern(new Pattern("pattern1", "Pattern"));
                
        assertThat(createPatternResult1.isSuccessful(), is(true));
        assertThat(createPatternResult2.isSuccessful(), is(false));
        assertThat(createPatternResult2.isUnsuccessful(), is(false));
        assertThat(createPatternResult2.isFailure(), is(true));
        
        assertThat(createPatternResult2.getExternalReason(), is("A pattern with name 'pattern1' already exists"));
        assertThat(createPatternResult2.getInternalReason(), is("A pattern with name 'pattern1' already exists"));
    }
    
    @Test public void test_create_aggregation() throws ConnectorException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.ChannelSubscriptions,
                                                                   HubTestFixture.Features.PatternManager));
        SocketHub hub = hubFixture.start();

        SocketClient clientA = fixture.createClient("clientA", hub);

        PatternManagementAPI patternManagementAPI = clientA.getPatternManagementAPI();

        Result<List<Aggregation>> aggregations = patternManagementAPI.getAggregations();
        assertThat(aggregations.isSuccessful(), is(true));
        assertThat(aggregations.getValue().size(), is(0));

        Aggregation template = new Aggregation();
        template.setCaptureLabelIndex(4);
        template.setGroupBy("group by");
        template.setInterval(1000);
        template.setPatternID(123);
        template.setType(AggregationType.LastValue);
        
        Result<Aggregation> createAggregationResult = patternManagementAPI.createAggregation(template);

        assertThat(createAggregationResult.getState(), is(Result.State.Successful));
        assertThat(createAggregationResult.getValue().getAggregationID(), is(0));
        assertThat(createAggregationResult.getValue().getCaptureLabelIndex(), is(4));
        assertThat(createAggregationResult.getValue().getGroupBy(), is("group by"));
        assertThat(createAggregationResult.getValue().getInterval(), is(1000L));
        assertThat(createAggregationResult.getValue().getPatternID(), is(123));
        assertThat(createAggregationResult.getValue().getType(), is(AggregationType.LastValue));

        aggregations = patternManagementAPI.getAggregations();
        assertThat(aggregations.isSuccessful(), is(true));
        assertThat(aggregations.getValue().size(), is(1));
        assertThat(aggregations.getValue().get(0).getAggregationID(), is(0));
        assertThat(aggregations.getValue().get(0).getCaptureLabelIndex(), is(4));
        assertThat(aggregations.getValue().get(0).getGroupBy(), is("group by"));
        assertThat(aggregations.getValue().get(0).getInterval(), is(1000L));
        assertThat(aggregations.getValue().get(0).getPatternID(), is(123));
        assertThat(aggregations.getValue().get(0).getType(), is(AggregationType.LastValue));


    }
}
