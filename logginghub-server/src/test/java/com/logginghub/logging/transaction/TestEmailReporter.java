package com.logginghub.logging.transaction;

import org.junit.Test;

import com.logginghub.logging.transaction.EmailConnector;
import com.logginghub.logging.transaction.EmailReporter;


public class TestEmailReporter {

    @Test public void test_something() throws Exception {
       
        EmailReporter emailReporter = new EmailReporter();
        
        EmailConnector emailSender = new EmailConnector();
        
    }

}
