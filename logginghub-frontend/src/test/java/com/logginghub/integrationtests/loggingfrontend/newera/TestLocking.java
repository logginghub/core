package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Tracer;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestLocking {

    private SwingFrontEndDSL dsl;
    
    @Before public void create() throws IOException {
        Tracer.enable();
        Tracer.autoIndent(true);
        
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                                                                                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                                                                                        .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);
    }

    @After public void stopHub() throws IOException {
        dsl.getFrameFixture().cleanUp();
    }
    
    @Test public void test_row_locking_doesnt_remove() throws InterruptedException {

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();
       
        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);

        dsl.lockRow(1);        
        
        dsl.clearEvents();
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, event2);               
        
        // Unlock it and clear again
        dsl.lockRow(0);
        dsl.clearEvents();
        dsl.assertLogEventTableSize(0);
    }
    
    @Test public void test_auto_locking() throws InterruptedException {
        
        dsl.setAutoLockWarning(true);
        
        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();
       
        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        dsl.clearEvents();
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, event2);
    }
    
    @Test public void test_row_locking_with_remove() throws InterruptedException {

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();
       
        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);

        // Lock the second one
        dsl.lockRow(1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Unlock the second one
        dsl.lockRow(1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Clear the second one
        dsl.pressDelete();
        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event3);
    }
    
    @Test public void test_row_re_locking() throws InterruptedException {

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();
       
        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);

        // Lock the second one
        dsl.lockRow(1);        
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Unlock the second one
        dsl.lockRow(1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Re-lock the second one
        dsl.lockRow(1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);        
        
        // Clear things, make sure its still there
        dsl.clearEvents();
        dsl.assertLogEventTableSize(1);
        dsl.assertLogEventInTable(0, event2);
    }
    
    @Test public void test_locking_multiple() throws InterruptedException {

        LogEvent event1 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 1 : Info").toLogEvent();
        LogEvent event2 = LogEventBuilder.start().setLevel(Level.WARNING.intValue()).setMessage("Message 2 : Warning").toLogEvent();
        LogEvent event3 = LogEventBuilder.start().setLevel(Level.INFO.intValue()).setMessage("Message 3 : Info").toLogEvent();
       
        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);
        
        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);

        // Select the second two rows
        dsl.clickTableRow(1, 1);        
        dsl.controlClickTableRow(2, 1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Lock on the second, it should lock the other one too
        dsl.rightClickTableRow(1, 1);
        dsl.assertLogEventTableSize(3);
        dsl.assertLogEventInTable(0, event1);
        dsl.assertLogEventInTable(1, event2);
        dsl.assertLogEventInTable(2, event3);
        
        // Clear things, make sure its still there
        dsl.clearEvents();
        dsl.assertLogEventTableSize(2);
        dsl.assertLogEventInTable(0, event2);
        dsl.assertLogEventInTable(1, event3);
    }

}

