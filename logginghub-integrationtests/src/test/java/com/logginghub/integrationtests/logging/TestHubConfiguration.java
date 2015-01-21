package com.logginghub.integrationtests.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.hub.configuration.LegacySocketHubConfiguration;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.launchers.LegacyRunHub;
import com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.utils.ArrayUtils;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;

public class TestHubConfiguration {

    private File folder;
    private File configurationFile;

    @Test public void test_log4j_pattern_timestamp_appender() throws Exception {
        TimestampVariableRollingFileLoggerConfiguration timestampAggregatedFileLogConfiguration = new TimestampVariableRollingFileLoggerConfiguration();
        timestampAggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
        timestampAggregatedFileLogConfiguration.setExtension(".txt");
        timestampAggregatedFileLogConfiguration.setFilename("log");
        timestampAggregatedFileLogConfiguration.setFolder("target/temp/testHubConfiguration/timestampAggregatedLogs");
        timestampAggregatedFileLogConfiguration.setMaximumFileSize(100000);
        timestampAggregatedFileLogConfiguration.setNumberOfCompressedFiles(2);
        timestampAggregatedFileLogConfiguration.setNumberOfFiles(2);
        timestampAggregatedFileLogConfiguration.setOpenWithAppend(true);
        timestampAggregatedFileLogConfiguration.setWriteAsynchronously(true);
        timestampAggregatedFileLogConfiguration.setFormatter(Log4jPatternLogEventFormatter.class.getName());
        timestampAggregatedFileLogConfiguration.setPattern("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n");
        timestampAggregatedFileLogConfiguration.setAutoNewline(false);

        int mainPort = NetUtils.findFreePort();

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(mainPort);
        configuration.setTimeStampAggregatedFileLogConfiguration(timestampAggregatedFileLogConfiguration);

        configuration.writeToFile(configurationFile);

        final LegacyRunHub wrapper = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage message = new LogEventMessage(event1);
        wrapper.getHub().onNewMessage(message, Mockito.mock(SocketConnection.class));

        // Give it a second for the async stuff to pass through
        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return wrapper.getTimestampAggregatedFileLoggerPool().getQueue().size() == 0;
            }
        });

        wrapper.getHub().stop();

        File timestampLogs = new File("target/temp/testHubConfiguration/timestampAggregatedLogs");
        Map<String, File> listFiles = FileUtils.listFilesAsMap(timestampLogs);
        assertThat(listFiles.size(), is(2));
        String contents = FileUtils.readAsString(listFiles.get("log.txt"));
        String expected = StringUtils.format("[{}] TestApplication {} {} INFO   getLogRecord1  - This is mock record 1{}",
                                             Thread.currentThread().getName(),
                                             NetUtils.getLocalHostname(),
                                             NetUtils.getLocalIP(),
                                             StringUtils.newline);
        assertThat(contents, is(endsWith(expected)));
    }

    @Test public void test_log4j_pattern_rolling_appender() throws Exception {
        RollingFileLoggerConfiguration aggregatedFileLogConfiguration = new RollingFileLoggerConfiguration();
        aggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
        aggregatedFileLogConfiguration.setFilename("target/temp/testHubConfiguration/normalAggregatedLogs/log.txt");
        aggregatedFileLogConfiguration.setMaximumFileSize(100000);
        aggregatedFileLogConfiguration.setNumberOfFiles(2);
        aggregatedFileLogConfiguration.setNumberOfCompressedFiles(2);
        aggregatedFileLogConfiguration.setOpenWithAppend(true);
        aggregatedFileLogConfiguration.setWriteAsynchronously(true);
        aggregatedFileLogConfiguration.setFormatter(Log4jPatternLogEventFormatter.class.getName());
        aggregatedFileLogConfiguration.setPattern("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n");
        aggregatedFileLogConfiguration.setAutoNewline(false);

        int mainPort = NetUtils.findFreePort();

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(mainPort);
        configuration.setAggregatedFileLogConfiguration(aggregatedFileLogConfiguration);

        configuration.writeToFile(configurationFile);

        final LegacyRunHub wrapper = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        LogEventMessage message = new LogEventMessage(event1);
        wrapper.getHub().onNewMessage(message, Mockito.mock(SocketConnection.class));

        // Give it a second for the async stuff to pass through
        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return wrapper.getAggregatedFileLoggerPool().getQueue().size() == 0;
            }
        });

        wrapper.getHub().stop();

        File timestampLogs = new File("target/temp/testHubConfiguration/normalAggregatedLogs");
        File[] listFiles = timestampLogs.listFiles();
        assertThat(listFiles.length, is(1));
        String contents = FileUtils.readAsString(listFiles[0]);
        String expected = StringUtils.format("[{}] TestApplication {} {} INFO   getLogRecord1  - This is mock record 1{}",
                                             Thread.currentThread().getName(),
                                             NetUtils.getLocalHostname(),
                                             NetUtils.getLocalIP(),
                                             StringUtils.newline);
        assertThat(contents, is(endsWith(expected)));
    }

    @Test public void testHubConfiguration() throws Exception {

        if (OSUtils.isWindows()) {

            TimestampVariableRollingFileLoggerConfiguration timestampAggregatedFileLogConfiguration = new TimestampVariableRollingFileLoggerConfiguration();
            timestampAggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
            timestampAggregatedFileLogConfiguration.setExtension(".txt");
            timestampAggregatedFileLogConfiguration.setFilename("log");
            timestampAggregatedFileLogConfiguration.setFolder("target/temp/testHubConfiguration/timestampAggregatedLogs");
            timestampAggregatedFileLogConfiguration.setMaximumFileSize(100000);
            timestampAggregatedFileLogConfiguration.setNumberOfCompressedFiles(2);
            timestampAggregatedFileLogConfiguration.setNumberOfFiles(2);
            timestampAggregatedFileLogConfiguration.setOpenWithAppend(true);
            timestampAggregatedFileLogConfiguration.setWriteAsynchronously(true);

            RollingFileLoggerConfiguration aggregatedFileLogConfiguration = new RollingFileLoggerConfiguration();
            aggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
            aggregatedFileLogConfiguration.setFilename("log");
            aggregatedFileLogConfiguration.setExtension(".txt");
            aggregatedFileLogConfiguration.setFolder("target/temp/testHubConfiguration/normalAggregatedLogs/");
            aggregatedFileLogConfiguration.setMaximumFileSize(100000);
            aggregatedFileLogConfiguration.setNumberOfFiles(2);
            aggregatedFileLogConfiguration.setNumberOfCompressedFiles(2);
            aggregatedFileLogConfiguration.setOpenWithAppend(true);
            aggregatedFileLogConfiguration.setWriteAsynchronously(true);

            int mainPort = NetUtils.findFreePort();
            int socketTextReaderPort = NetUtils.findFreePort();

            // SocketTextReaderConfiguration socketTestReaderConfiguration = new
            // SocketTextReaderConfiguration();
            // socketTestReaderConfiguration.setPort(socketTextReaderPort);
            // socketTestReaderConfiguration.setMessageEnd("message end");
            // socketTestReaderConfiguration.setMessageStart("message start");
            // socketTestReaderConfiguration.setLevel(Level.WARNING.getName());

            // List<SocketTextReaderConfiguration> socketTextReaders = new
            // ArrayList<SocketTextReaderConfiguration>();
            // socketTextReaders.add(socketTestReaderConfiguration);

            LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
            configuration.setAggregatedFileLogConfiguration(aggregatedFileLogConfiguration);
            configuration.setPort(mainPort);
            // configuration.setSocketTextReaders(socketTextReaders);
            configuration.setTimeStampAggregatedFileLogConfiguration(timestampAggregatedFileLogConfiguration);

            configuration.writeToFile(configurationFile);

            final LegacyRunHub wrapper = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });

            // Logger.setLevel(TimestampFixedRollingFileLogger.class, Logger.trace);

            // Both appenders are set for 2 normal + 2 compressed files, 100,000 in
            // size
            int messagesToRollover = ((2 + 2) * 100000) / 164;
            for (int i = 0; i < messagesToRollover; i++) {
                DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
                LogEventMessage message = new LogEventMessage(event1);

                event1.setMessage("Test message " + i);
                // System.out.println(event1.getMessage());
                wrapper.getHub().onNewMessage(message, Mockito.mock(SocketConnection.class));

                if (i % 500 == 0) {
                    // TODO : there is bug with the zipping and filenumbering in the new fixed name
                    // logger if everything happens in the same second :/
                    Thread.sleep(500);
                }
            }

            // Give it a second for the async stuff to pass through
            ThreadUtils.untilTrue(100, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    return wrapper.getAggregatedFileLoggerPool().getQueue().size() == 0 &&
                           wrapper.getTimestampAggregatedFileLoggerPool().getQueue().size() == 0;
                }
            });

            wrapper.getHub().stop();

            // Make sure everything go through ok
            checkStandardLog(wrapper);
            checkTimestampLog(wrapper);
        }
    }

    private void checkStandardLog(LegacyRunHub wrapper) {
        File timestampLogs = new File("target/temp/testHubConfiguration/normalAggregatedLogs/");
        File[] listFiles = timestampLogs.listFiles();
        int counter = 0;
        ArrayUtils.reverse(listFiles);
        for (File file : listFiles) {
            // System.out.println(file.getName());

            String contents;
            if (file.getName().endsWith(".zip")) {
                contents = FileUtils.readFromZipAsString(file, "log.txt.1");
            }
            else {
                contents = FileUtils.readAsString(file);
            }

            String[] lines = StringUtils.splitIntoLines(contents);
            for (String string : lines) {
                assertThat(string, endsWith(Integer.toString(counter)));
                counter++;
            }
        }
    }

    private void checkTimestampLog(LegacyRunHub wrapper) {
        File timestampLogs = new File("target/temp/testHubConfiguration/timestampAggregatedLogs");

        File[] listFiles = timestampLogs.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                return file.getName().startsWith("log.") &&
                       (file.getName().endsWith(".txt") || file.getName().endsWith(".txt.zip")) &&
                       !file.getName().equals("log.txt");
            }
        });

        wrapper.getTimestampAggregatedFileLogger().sortArray(listFiles);

        List<File> files = new ArrayList<File>();
        for (File file : listFiles) {
            files.add(file);
        }

        // Add the non-stamped file into the list
        files.add(new File(timestampLogs, "log.txt"));

        int counter = 0;
        for (File file : files) {
            System.out.println(file.getName());

            String name = file.getName();
            name = StringUtils.before(name, ".zip");

            String contents;
            if (file.getName().endsWith(".zip")) {
                contents = FileUtils.readFromZipAsString(file, name);
            }
            else {
                contents = FileUtils.readAsString(file);
            }

            String[] lines = StringUtils.splitIntoLines(contents);
            for (String string : lines) {
                assertThat(string, endsWith(Integer.toString(counter)));
                counter++;
            }
        }
    }

    @Before public void setup() {
        folder = new File("target/temp/testHubConfiguration");
        FileUtils.deleteContents(folder);
        folder.mkdirs();

        configurationFile = new File(folder, "/hub.xml");
    }
}
