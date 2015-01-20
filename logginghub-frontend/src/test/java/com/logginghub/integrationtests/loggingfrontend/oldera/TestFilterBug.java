package com.logginghub.integrationtests.loggingfrontend.oldera;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;

public class TestFilterBug extends BaseSwing {

    @Override protected LoggingFrontendConfiguration getConfiguration() {

        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default")
                                                                                                                                        .highlighter("blue", "0x0000ff", false)
                                                                                                                                        .highlighter("green", "0x00ff00", false))
                                                                                        .toConfiguration();

        return configuration;

    }
    
    @Test public void test() throws InterruptedException, InvocationTargetException {
        dsl.changeLevelQuickFilter(Level.INFO);
        
        LogEvent event1Info = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").setLocalCreationTimeMillis(10).toLogEvent();
        LogEvent event2Warning = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").setLocalCreationTimeMillis(20).toLogEvent();
        LogEvent event3Info = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").setLocalCreationTimeMillis(30).toLogEvent();
        
        dsl.publishEvent(event1Info);
        dsl.publishEvent(event2Warning);
        dsl.publishEvent(event3Info);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1Info);
        dsl.assertLogEventInTable(1, event2Warning);
        dsl.assertLogEventInTable(2, event3Info);
        
        dsl.changeLevelQuickFilter(Level.WARNING);
        dsl.waitForQuickFilter();        
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, event2Warning);
        
        dsl.changeLevelQuickFilter(Level.INFO);
        dsl.waitForQuickFilter();
        
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1Info);
        dsl.assertLogEventInTable(1, event2Warning);
        dsl.assertLogEventInTable(2, event3Info);


    }

    
}
