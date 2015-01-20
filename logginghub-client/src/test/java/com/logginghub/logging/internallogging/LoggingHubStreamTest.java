package com.logginghub.logging.internallogging;

import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoggingHubStreamTest {

    @Test public void test_something() {


        LoggingHubStream loggingHubStream = LoggingUtils.logToHub2("LoggingHubStreamTest",
                                                                   "localhost",
                                                                   VLPorts.getSocketHubDefaultPort());

        loggingHubStream.setJava7GCLogging(true);
        loggingHubStream.setCpuLogging(true);
        loggingHubStream.setPublishProcessTelemetry(true);

        for(int i = 0; i < 1000; i++) {
            Logger.getLoggerFor("test").info("This is a test : " + i);
            ThreadUtils.sleep(100);
        }


    }

}