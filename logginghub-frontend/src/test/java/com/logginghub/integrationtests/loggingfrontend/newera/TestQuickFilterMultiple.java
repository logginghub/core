package com.logginghub.integrationtests.loggingfrontend.newera;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder;
import com.logginghub.utils.logging.Logger;
import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;

public class TestQuickFilterMultiple {

    private SwingFrontEndDSL dsl;
    private long quickFilterUpdateTime = 500;

    @Before public void before() { 
        LoggingFrontendConfiguration configuration = LoggingFrontendConfigurationBuilder.newConfiguration()
                        .environment(LoggingFrontendConfigurationBuilder.newEnvironment("default"))
                        .toConfiguration();
        
        dsl = BaseSwing.createDSL(configuration);
    }
    
    @Test public void test_add_filter() {

        LogEvent event1 = dsl.publishEvent("default", Logger.info, "apple orange");
        LogEvent event2 = dsl.publishEvent("default", Logger.info, "apple banana");
        LogEvent event3 = dsl.publishEvent("default", Logger.info, "pear cherry");
        LogEvent event4 = dsl.publishEvent("default", Logger.info, "orange pear");
        LogEvent event5 = dsl.publishEvent("default", Logger.info, "cherry apple");
        
        dsl.waitForBatch();
        
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
        
        dsl.addQuickFilter("default");
        
        dsl.setQuickFilter("default", "apple", 0);
        dsl.sleep(quickFilterUpdateTime);

        dsl.assertLogEventTable(event1, event2, event5);
        
        dsl.setQuickFilter("default", "orange", 1);
        dsl.sleep(quickFilterUpdateTime);
        
        dsl.assertLogEventTable(event1);
        
        dsl.enableQuickFilter("default", 0, false);
        dsl.sleep(quickFilterUpdateTime);
        dsl.assertLogEventTable(event1, event4);
        
        dsl.enableQuickFilter("default", 1, false);
        dsl.sleep(quickFilterUpdateTime);
        dsl.assertLogEventTable(event1, event2, event3, event4, event5);
        
        dsl.removeQuickFilter("default", 1);
        dsl.setQuickFilter("default", "pear", 0);
        dsl.sleep(quickFilterUpdateTime);
        dsl.assertLogEventTable(event3, event4);
    }
    

    @After public void after() throws IOException {
        dsl.shutdown();
    }

}
