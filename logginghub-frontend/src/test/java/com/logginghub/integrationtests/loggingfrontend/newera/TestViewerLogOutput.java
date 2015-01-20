package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestViewerLogOutput {

    private SwingFrontEndDSL dsl;
    private File logOutput;
    private TimestampVariableRollingFileLoggerConfiguration logConfiguration;
    
    private static final Logger logger = Logger.getLoggerFor(TestViewerLogOutput.class);

    @Before public void create() throws IOException {
        logOutput = FileUtils.createRandomTestFolderForClass(this.getClass());
        logger.info("Output to {}", logOutput.getAbsolutePath());

        logConfiguration = new TimestampVariableRollingFileLoggerConfiguration();
        logConfiguration.setFolder(logOutput.getAbsolutePath());
        logConfiguration.setFilename("test");
        logConfiguration.setForceFlush(true);
    }

    @After public void stopHub() throws IOException {
        dsl.shutdown();
    }

    @Test public void test_no_log_by_default() {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.assertMenuOption("Log view", "Sources", "Write output log", true, false);

        int events = 5;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events);
        assertThat(FileUtils.listFiles(logOutput).size(), is(0));

    }

    @Test public void test_default_configuration_single_environment() {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default").setWriteOutputLog(true))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.assertMenuOption("Log view", "Sources", "Write output log", true, true);

        logOutput = new File("logs/default");
        FileUtils.deleteContents(logOutput);

        int events = 5;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));

    }

    @Test public void test_menu_option_hidden_with_no_configuration() {
        TimestampVariableRollingFileLoggerConfiguration logConfiguration = null;

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .setOutputLogConfiguration(logConfiguration))
                                                                                        .toConfiguration();
        dsl = SwingFrontEndDSL.createDSL(configuration);
    }

    @Test public void test_multiple_environments_toggle_on_off(){

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env1").setWriteOutputLog(true))
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("env2").setWriteOutputLog(false))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);
        
        FileUtils.deleteContents(new File("logs/"));
        File logOutputEnv1 = new File("logs/env1");
        File logOutputEnv2 = new File("logs/env2");
        
        dsl.assertMenuOption("env1", "Sources", "Write output log", true, true);
        dsl.assertMenuOption("env2", "Sources", "Write output log", true, false);

        // Publish two events
        dsl.publishEvent("env1", LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message env 1").toLogEvent());
        dsl.publishEvent("env2", LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message env 2").toLogEvent());
        
        dsl.waitForBatch("env1");
        dsl.waitForBatch("env2");
        
        // Make sure they arrived in the table
        dsl.assertLogEventTableSize("env1", 1);
        dsl.assertLogEventTableSize("env2", 1);
        
        // Assert the file system changes
        assertThat(FileUtils.listFiles(logOutputEnv1).size(), is(1));
        assertThat(FileUtils.listFiles(logOutputEnv2).size(), is(0));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutputEnv1).get(0)).length, is(1));        
        
        // Turn both on
        dsl.selectTab("env1");
        dsl.setWriteOutputLog(true);
        
        dsl.selectTab("env2");
        dsl.setWriteOutputLog(true);
        
        dsl.assertMenuOption("env1", "Sources", "Write output log", true, true);
        dsl.assertMenuOption("env2", "Sources", "Write output log", true, true);
        
        // Publish two more events
        dsl.publishEvent("env1", LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message env 1").toLogEvent());
        dsl.publishEvent("env2", LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message env 2").toLogEvent());
        
        dsl.waitForBatch("env1");
        dsl.waitForBatch("env2");
        
        // Make sure they arrived in the table
        dsl.assertLogEventTableSize("env1", 2);
        dsl.assertLogEventTableSize("env2", 2);
        
        // Assert the file system changes
        assertThat(FileUtils.listFiles(logOutputEnv1).size(), is(1));
        assertThat(FileUtils.listFiles(logOutputEnv2).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutputEnv1).get(0)).length, is(2));        
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutputEnv2).get(0)).length, is(1));
        
        // Make sure changes stick
        dsl.selectTab("env1");
        dsl.setWriteOutputLog(false);
        
        dsl.assertMenuOption("env1", "Sources", "Write output log", true, false);
        dsl.assertMenuOption("env2", "Sources", "Write output log", true, true);
        
        dsl.selectTab("env2");
        dsl.setWriteOutputLog(false);
        
        dsl.assertMenuOption("env1", "Sources", "Write output log", true, false);
        dsl.assertMenuOption("env2", "Sources", "Write output log", true, false);
    }

    @Test public void test_turned_on_in_configuration() {
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .setOutputLogConfiguration(logConfiguration)
                                                                                                                                        .setWriteOutputLog(true))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.assertWriteOutputLog("Log view", true);

        int events = 5;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));

        dsl.setWriteOutputLog(false);
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events * 2);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));

        dsl.setWriteOutputLog(true);

        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events * 3);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(10));
    }

    @Test public void test_turned_off_in_configuration() throws InterruptedException {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .setOutputLogConfiguration(logConfiguration)
                                                                                                                                        .setWriteOutputLog(false))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);
        dsl.assertWriteOutputLog("Log view", false);

        int events = 5;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events);
        assertThat(FileUtils.listFiles(logOutput).size(), is(0));

        dsl.setWriteOutputLog(true);
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events * 2);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));

        dsl.setWriteOutputLog(false);

        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events * 3);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));
    }

    
    @Test public void test_switch_output() {
        
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .setOutputLogConfiguration(logConfiguration)
                                                                                                                                        .setWriteOutputLog(true))
                                                                                        .toConfiguration();

        dsl = SwingFrontEndDSL.createDSL(configuration);

        dsl.assertWriteOutputLog("Log view", true);

        int events = 5;
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events);
        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));

        dsl.switchOutputLog("alternative.log");
        
        for (int i = 0; i < events; i++) {
            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
            dsl.publishEvent(event1);
        }

        dsl.waitForBatch();
        dsl.assertLogEventTableSize(events * 2);
        assertThat(FileUtils.listFiles(logOutput).size(), is(2));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(5));
        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(1)).length, is(5));

//        dsl.debugSleep(300000);
        
//        dsl.setWriteOutputLog(true);
//
//        for (int i = 0; i < events; i++) {
//            LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message : " + i).toLogEvent();
//            dsl.publishEvent(event1);
//        }
//
//        dsl.waitForBatch();
//        dsl.assertLogEventTableSize(events * 3);
//        assertThat(FileUtils.listFiles(logOutput).size(), is(1));
//        assertThat(FileUtils.readAsStringArray(FileUtils.listFiles(logOutput).get(0)).length, is(10));
    }
}
