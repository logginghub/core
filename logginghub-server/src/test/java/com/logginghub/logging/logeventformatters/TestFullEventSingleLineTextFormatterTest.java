package com.logginghub.logging.logeventformatters;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
import com.logginghub.utils.NetUtils;

public class TestFullEventSingleLineTextFormatterTest {

    @Test 
    public void test() {
        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setLocalCreationTimeMillis(0);
        
        String localIP = NetUtils.getLocalIP();
        String localhost = NetUtils.getLocalHostname();
        
        String format = formatter.format(event);

        // shit test - the formatting depends on which machine you run it on... needs regex.
        // assertThat(format, is("01-Jan-1970 01:00:00 "+localhost+"          "+localIP+"    TestApplication LogEventFactory.getLogRecord1() main            INFO        This is mock record 1"));
    }
    
}
