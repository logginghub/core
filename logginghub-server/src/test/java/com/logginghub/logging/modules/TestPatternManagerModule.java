package com.logginghub.logging.modules;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.modules.configuration.PatternManagerConfiguration;
import com.logginghub.logging.servers.ServerMessageHandler;
import com.logginghub.logging.servers.ServerSubscriptionsService;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Result;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestPatternManagerModule {

    private File dataFile;
    private PatternManagerModule module;
    private ConfigurableServiceDiscovery disco;
    private PatternManagerConfiguration configuration;

    @Before public void setup() {
        dataFile = FileUtils.createRandomTestFileForClass(getClass());

        configuration = new PatternManagerConfiguration();
        configuration.setDataFile(dataFile.getAbsolutePath());
        disco = new ConfigurableServiceDiscovery();

        disco.bind(ServerSubscriptionsService.class, new ServerSubscriptionsService() {
            @Override public void unsubscribe(String channel, ServerMessageHandler handler) {}

            @Override public void send(ChannelMessage message) throws LoggingMessageSenderException {

            }

            @Override public void subscribe(String channel, ServerMessageHandler handler) {}
        });

        module = new PatternManagerModule();
        module.configure(configuration, disco);
        module.start();
    }

    @After public void teardown() {
        module.stop();
    }

    @Test public void test_create_two() throws Exception {

        Result<Pattern> resultA = module.createPattern("a", "patternA");
        Result<Pattern> resultB = module.createPattern("b", "patternB");

        assertThat(resultA.isSuccessful(), is(true));
        assertThat(resultA.getValue().getPatternId(), is(0));
        assertThat(resultA.getValue().getName(), is("a"));
        assertThat(resultA.getValue().getPattern(), is("patternA"));

        assertThat(resultB.isSuccessful(), is(true));
        assertThat(resultB.getValue().getPatternId(), is(1));
        assertThat(resultB.getValue().getName(), is("b"));
        assertThat(resultB.getValue().getPattern(), is("patternB"));

    }

    @Test public void test_same_name() throws Exception {
        Result<Pattern> resultA = module.createPattern("a", "patternA");
        Result<Pattern> resultB = module.createPattern("a", "patternB");

        assertThat(resultA.isSuccessful(), is(true));
        assertThat(resultA.getValue().getPatternId(), is(0));
        assertThat(resultA.getValue().getName(), is("a"));
        assertThat(resultA.getValue().getPattern(), is("patternA"));

        assertThat(resultB.isSuccessful(), is(false));
        assertThat(resultB.getExternalReason(), is("A pattern with name 'a' already exists"));

        assertThat(module.getPatterns().getValue().size(), is(1));
    }

    @Test public void test_same_pattern() throws Exception {
        Result<Pattern> resultA = module.createPattern("a", "patternA");
        Result<Pattern> resultB = module.createPattern("b", "patternA");

        assertThat(resultA.isSuccessful(), is(true));
        assertThat(resultA.getValue().getPatternId(), is(0));
        assertThat(resultA.getValue().getName(), is("a"));
        assertThat(resultA.getValue().getPattern(), is("patternA"));

        assertThat(resultB.isSuccessful(), is(false));
        assertThat(resultB.getExternalReason(), is("A pattern with regex 'patternA' already exists"));

        assertThat(module.getPatterns().getValue().size(), is(1));
    }

    @Test public void test_reload() throws Exception {
        assertThat(dataFile.exists(), is(false));

        Result<Pattern> patternResult = module.createPattern("a", "patternA");
        assertThat(patternResult.isSuccessful(), is(true));
        assertThat(patternResult.getValue().getPatternId(), is(0));
        assertThat(patternResult.getValue().getName(), is("a"));
        assertThat(patternResult.getValue().getPattern(), is("patternA"));

        assertThat(dataFile.exists(), is(true));

        module.stop();

        // Reload the module and make sure its still there
        module = new PatternManagerModule();
        module.configure(configuration, disco);
        module.start();

        assertThat(dataFile.exists(), is(true));

        Result<ObservableList<Pattern>> result = module.getPatterns();
        assertThat(result.isSuccessful(), is(true));
        assertThat(result.getValue().size(), is(1));
        assertThat(result.getValue().get(0).getPatternId(), is(0));
        assertThat(result.getValue().get(0).getName(), is("a"));
        assertThat(result.getValue().get(0).getPattern(), is("patternA"));

        Result<Pattern> resultB = module.createPattern("b", "patternB");
        assertThat(resultB.isSuccessful(), is(true));
        assertThat(resultB.getValue().getPatternId(), is(1));
        assertThat(resultB.getValue().getName(), is("b"));
        assertThat(resultB.getValue().getPattern(), is("patternB"));

        module.stop();

    }

    @Test public void test_create_aggregation() {

        Result<Aggregation> result1 = module.createAggregation(0, 0, TimeUtils.parseInterval("1 second"), AggregationType.Count, null);

        assertThat(result1.isSuccessful(), is(true));

        assertThat(result1.getValue().getAggregationID(), is(0));
        assertThat(result1.getValue().getPatternID(), is(0));
        assertThat(result1.getValue().getCaptureLabelIndex(), is(0));
        assertThat(result1.getValue().getInterval(), is(1000L));
        assertThat(result1.getValue().getType(), is(AggregationType.Count));

        Result<Aggregation> result2 = module.createAggregation(4, 8, TimeUtils.parseInterval("1 minute"), AggregationType.Mean, null);

        assertThat(result2.isSuccessful(), is(true));

        assertThat(result2.getValue().getAggregationID(), is(1));
        assertThat(result2.getValue().getPatternID(), is(4));
        assertThat(result2.getValue().getCaptureLabelIndex(), is(8));
        assertThat(result2.getValue().getInterval(), is(60000L));
        assertThat(result2.getValue().getType(), is(AggregationType.Mean));

        System.out.println(FileUtils.read(dataFile));

    }

}
