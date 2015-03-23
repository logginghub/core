package com.logginghub.logging.hub;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.launchers.RunHub;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestConfigurationSQLExtractor {

    private static final File baseFolder = new File("src/main/resources/configs/hub");
    private Map<String, Validator> validators = new HashMap<String, Validator>();

    interface Validator {
        void validate(RunHub hub);
    }


    protected void validateFilters(RunHub hubWrapper) {
        System.out.println("Validating filters");
        SocketHub hub = hubWrapper.getFirst(SocketHub.class);
        Bucket<LogEvent> logEvents = new Bucket<LogEvent>();
        hub.addDestination(logEvents);
        hub.addDestination(new Destination<LogEvent>() {
            @Override
            public void send(LogEvent t) {
                System.out.println(t.getMessage());
            }
        });
        hub.addFilter(FilterConfiguration.startsWith("starts with"));

        logEvents.waitForMessages(10);

        boolean nothingThatShouldHaveBeenFiltered = true;

        for (int i = 0; i < logEvents.size(); i++) {

            if (logEvents.get(i).getMessage().startsWith("Trade reported")) {
                nothingThatShouldHaveBeenFiltered = false;
                break;
            }

            if (logEvents.get(i).getMessage().contains("stored")) {
                nothingThatShouldHaveBeenFiltered = false;
                break;
            }

            if (logEvents.get(i).getMessage().contains("enriched")) {
                nothingThatShouldHaveBeenFiltered = false;
                break;
            }
        }

        assertThat(nothingThatShouldHaveBeenFiltered, is(true));
    }


    @Test
    public void test_hub_with_channel_split_file_loggers() throws Exception {
        validate("hub.with.channel.split.file.loggers.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void testa() throws Exception {
        validate("hub.with.state.engine.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void testb() throws Exception {
        validate("hub.with.filters.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {
                validateFilters(hub);
            }
        });
    }

    @Test
    public void testc() throws Exception {
        validate("hub.with.sqlextractor.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_d() throws Exception {
        validate("hub.with.patterniser.and.aggregator.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_e() throws Exception {
        validate("hub.1.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }


    @Test
    public void test_f() throws Exception {
        validate("hub.2.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_g() throws Exception {
        validate("hub.with.rollingfile.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_h() throws Exception {
        validate("hub.with.sqlextractor.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_i() throws Exception {
        validate("hub.with.patternised.disk.history.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }


    @Test
    public void test_j() throws Exception {
        validate("hub.with.telemetry.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_k() throws Exception {
        validate("hub.with.periodic.stack.capture.and.history.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_l() throws Exception {
        validate("hub.with.disk.history.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    @Test
    public void test_hub_with_telemetry() throws Exception {
        validate("hub.with.telemetry.xml", new Validator() {
            @Override
            public void validate(RunHub hub) {

            }
        });
    }

    private void validate(String string, Validator validator) throws Exception {
        File file = new File(baseFolder, string);
        validate(file, validator);
    }

    private void validate(File file, Validator validator) throws Exception {
        assertThat("Validator for '" + file.getName() + "' wasn't found",
                   validator,
                   is(not(nullValue())));
        RunHub hub = RunHub.mainInternal(file.getAbsolutePath());
        validator.validate(hub);
        hub.close();
    }

}
