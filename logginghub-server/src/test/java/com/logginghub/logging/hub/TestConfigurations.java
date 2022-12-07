package com.logginghub.logging.hub;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.launchers.RunHub;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class TestConfigurations {

    private static final File baseFolder = new File("src/main/resources/configs/hub");
    private Map<String, Validator> validators = new HashMap<String, Validator>();

    interface Validator {
        void validate(RunHub hub);
    }

    public TestConfigurations() {
        validators.put("hub.with.channel.split.file.loggers.xml", new Validator() {
            @Override public void validate(RunHub hub) {
                // TODO : put something in here...
            }
        });

        validators.put("hub.with.state.engine.xml", new Validator() {
            @Override public void validate(RunHub hub) {
                // TODO : put something in here...
            }
        });

        validators.put("hub.with.filters.xml", new Validator() {
            @Override public void validate(RunHub hub) {
                validateFilters(hub);
            }
        });

        validators.put("hub.with.patterniser.and.aggregator.xml", new Validator() {
            @Override public void validate(RunHub hub) {
                // TODO : put something in here...
            }
        });

        validators.put("hub.1.xml", new Validator() {
            @Override public void validate(RunHub hub) {}
        });

        validators.put("hub.2.xml", new Validator() {
            @Override public void validate(RunHub hub) {}
        });

        validators.put("hub.with.rollingfile.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });
        
        validators.put("hub.with.sqlextractor.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });
        
        validators.put("hub.with.patternised.disk.history.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });
        
        validators.put("hub.with.telemetry.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });

        validators.put("hub.with.periodic.stack.capture.and.history.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });

        validators.put("hub.with.disk.history.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });
    }

    protected void validateFilters(RunHub hubWrapper) {
        System.out.println("Validating filters");
        SocketHub hub = hubWrapper.getFirst(SocketHub.class);
        Bucket<LogEvent> logEvents = new Bucket<LogEvent>();
        hub.addDestination(logEvents);
        hub.addDestination(new Destination<LogEvent>() {
            @Override public void send(LogEvent t) {
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

    @Test public void test() throws Exception {

        List<File> listFiles = FileUtils.listFiles(baseFolder);

        assertThat(listFiles.size(), is(not(0)));

        if(!SigarHelper.hasSigarSupport()) {
            Iterator<File> iterator = listFiles.iterator();
            while(iterator.hasNext()) {
                File file = iterator.next();
                if(file.getName().equals("hub.with.telemetry.xml")) {
                    iterator.remove();
                }
            }
        }

        for (File file : listFiles) {
            try {
                test(file);
            }
            catch (Throwable t) {
                throw new Exception(StringUtils.format("Problem found in '{}'", file.getAbsolutePath()), t);
            }
        }

    }

    @Test public void test_sql_extractor() throws Exception {
        validate("hub.with.sqlextractor.xml", new Validator() {
            @Override public void validate(RunHub hub) {

            }
        });
    }

    private void validate(String string, Validator validator) throws Exception {
        File file = new File(baseFolder, string);
        validate(file, validator);
    }

    private void validate(File file, Validator validator) throws Exception {
        assertThat("Validator for '" + file.getName() + "' wasn't found", validator, is(not(nullValue())));
        RunHub hub = RunHub.mainInternal(file.getAbsolutePath());
        validator.validate(hub);
        hub.close();
    }

    private void test(File file) throws Exception {
        Validator validator = validators.get(file.getName());
        assertThat("Validator for '" + file.getName() + "' wasn't found", validator, is(not(nullValue())));
        validate(file, validator);
    }

}
