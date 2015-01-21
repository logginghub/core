package com.logginghub.logging.internallogging;

import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.Logger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Bit of a mickey mouse non-test, but it checks to see if the Logger load properties correctly and that the
 * LoggingHubStream can be constructed ok.  Needed to verify this stuff for MS work quickly.
 */
// TODO : make this a proper test!
public class LoggingHubStreamTest {

    @Test public void test_properties_from_file() {

        File file = FileUtils.createRandomTestFileForClass(LoggingHubStreamTest.class);
        FileUtils.appendLine("cpu-logger=finest", file);
        FileUtils.appendLine("heap-logger=finest", file);
        FileUtils.appendLine("gc-logger=finest", file);

        System.setProperty("logginghub.debug", "true");
        System.setProperty("logginghub.levels.properties", file.getAbsolutePath());

        Logger.loadProperties();

        List<byte[]> blocks = new ArrayList<byte[]>();

        try {
            LoggingHubStream loggingHubStream = LoggingUtils.logToHub2("LoggingHubStreamTest",
                                                                       "localhost",
                                                                       VLPorts.getSocketHubDefaultPort());

            loggingHubStream.setJava7GCLogging(true);
            loggingHubStream.setCpuLogging(true);
            loggingHubStream.setHeapLogging(true);
            loggingHubStream.setPublishProcessTelemetry(true);

            //            for (int i = 0; i < 1000; i++) {
            //                Logger.getLoggerFor("test").info("This is a test : " + i);
            //                byte[] block = new byte[10 * 1024 * 1024];
            //                blocks.add(block);
            //                ThreadUtils.sleep(100);
            //            }
        } finally {
            System.clearProperty("logginghub.debug");
            System.clearProperty("logginghub.levels.properties");
        }
    }

    @Test public void test_properties_from_resouce() {

        System.setProperty("logginghub.debug", "true");
        System.setProperty("logginghub.levels.properties", "test/logging.properties");

        Logger.loadProperties();

        try {
            LoggingHubStream loggingHubStream = LoggingUtils.logToHub2("LoggingHubStreamTest",
                                                                       "localhost",
                                                                       VLPorts.getSocketHubDefaultPort());

            //            loggingHubStream.setJava7GCLogging(true);
            //            loggingHubStream.setCpuLogging(true);
            //            loggingHubStream.setPublishProcessTelemetry(true);
            //
            //            for (int i = 0; i < 1000; i++) {
            //                Logger.getLoggerFor("test").info("This is a test : " + i);
            //                ThreadUtils.sleep(100);
            //            }
        } finally {
            System.clearProperty("logginghub.debug");
            System.clearProperty("logginghub.levels.properties");
        }

    }

}