package com.logginghub.logging.launchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileFilter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.messaging.SocketConnectionInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.hub.configuration.FileLogConfiguration;
import com.logginghub.logging.hub.configuration.LegacySocketHubConfiguration;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.SocketHubConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.launchers.LegacyRunHub;
import com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.utils.AggregatedFileLogger;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class TestRunHub {

    private LegacyRunHub hub = new LegacyRunHub();

    private File folder = FileUtils.createRandomTestFolderForClass(TestRunHub.class);
    private File logsFolder = new File(folder, "logs");

    private LegacyRunHub mainInternal;

    @Before public void setup() {
        FileUtils.deleteContents(folder);
    }

    @After public void teardown() {
        FileUtils.closeQuietly(mainInternal);
    }

    @Test public void test_config_file_search() throws Exception {

        System.out.println(new File(".").getAbsolutePath());

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/search/inner/config.xml", "true" });
        assertThat(mainInternal.getAggregatedFileLogger().getFileName(), is("outer.txt"));
        mainInternal.close();

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/search/inner/config.xml" });
        assertThat(mainInternal.getAggregatedFileLogger().getFileName(), is("inner.txt"));
        mainInternal.close();

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/search/inner/unique.xml", "true" });
        assertThat(mainInternal.getAggregatedFileLogger().getFileName(), is("unique.txt"));
        mainInternal.close();

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/search/inner/missing.xml", "true" });
        assertThat(mainInternal, is(nullValue()));

    }

    @Ignore @Test public void test_environment_variable_replacement() throws Exception {

        folder = new File(System.getenv("TEMP") + "/LegacyRunHubTest/" + StringUtils.randomString(5));
        folder.mkdirs();
        System.out.println("Temp folder is " + folder.getAbsolutePath());
        FileUtils.deleteContents(folder);

        String cluster = "DSL_CLUSTERNAME";

        System.setProperty(cluster, "clusterName");

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/environmentReplacement.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));
        mainInternal.close();

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(2));
        assertThat(listFiles[0].getName().startsWith("clusterName"), is(true));

    }

    @Ignore @Test public void test_force_flush_timestamp_false() throws Exception {

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/forceFlush_false_timestamp.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(2));
        assertThat(listFiles[0].length(), is(0L));

        mainInternal.close();

        listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(164L));

    }

    @Ignore @Test public void test_force_flush_timestamp_true() throws Exception {

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/forceFlush_true_timestamp.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(2));
        assertThat(listFiles[0].length(), is(164L));

        mainInternal.close();

        listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(164L));
    }

    @Ignore @Test public void test_force_flush_timestamp_true_log4j() throws Exception {

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/forceFlush_true_timestamp_log4j.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(2));

        // The IP address size might change....
        assertThat(listFiles[0].length(), is(118L));

        mainInternal.close();

        listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(118L));
    }

    @Ignore @Test public void test_force_flush_normal_false() throws Exception {

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/forceFlush_false_normal.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(0L));

        mainInternal.close();

        listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(164L));

    }

    @Ignore @Test public void test_force_flush_normal_true() throws Exception {

        mainInternal = LegacyRunHub.mainInternal(new String[] { "src/test/resources/testhubconfigurations/forceFlush_true_normal.xml" });
        mainInternal.waitUntilBound();

        mainInternal.getHub().onNewMessage(new LogEventMessage(LogEventFactory.createFullLogEvent1()), Mockito.mock(SocketConnection.class));

        File[] listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(164L));

        mainInternal.close();

        listFiles = folder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(164L));

    }

    @Test public void test_force_flush_normal_true_log4j() throws Exception {

        StringUtilsBuilder sb = new StringUtilsBuilder();
        sb.appendLine("<hubConfiguration port='64547' telemetryPort='64548'>");
        sb.appendLine("<aggregatedFileLogConfiguration filename='log' folder='{}/logs/'", folder.getAbsolutePath());
        sb.appendLine("                 writeAsynchronously='false'       ");
        sb.appendLine("                 forceFlush='true'");
        sb.appendLine("                 formatter='com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter'");
        sb.appendLine("                 pattern='%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n'/>");
        sb.appendLine("</hubConfiguration>");

        System.out.println(folder.getAbsolutePath());
        File file = new File(folder, "config.xml");
        sb.toFile(file);

        mainInternal = LegacyRunHub.mainInternal(new String[] { file.getAbsolutePath() });
        mainInternal.waitUntilBound();

        Log4jPatternLogEventFormatter formatter = new Log4jPatternLogEventFormatter();
        formatter.setPattern("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n");
        DefaultLogEvent createFullLogEvent1 = LogEventFactory.createFullLogEvent1();
        mainInternal.getHub().onNewMessage(new LogEventMessage(createFullLogEvent1), Mockito.mock(SocketConnection.class));

        long size = formatter.format(createFullLogEvent1).getBytes().length;
        long sizeWithNewline = size + StringUtils.newline.getBytes().length;

        File[] listFiles = logsFolder.listFiles();
        assertThat(listFiles.length, is(1));

        assertThat(listFiles[0].length(), is(sizeWithNewline));

        mainInternal.close();

        listFiles = logsFolder.listFiles();
        assertThat(listFiles.length, is(1));
        assertThat(listFiles[0].length(), is(sizeWithNewline));

    }

    @Ignore // the distribution ships with the new container based configuration
    @Test public void test_default_config_parse() {
        LegacySocketHubConfiguration configuration = LegacySocketHubConfiguration.fromFile(new File("dist/hub/conf/hub.xml"));

        assertThat(configuration.getPort(), is(58770));
//        assertThat(configuration.getSocketTextReaders().size(), is(0));

        assertThat(configuration.getAggregatedFileLogConfiguration().getFilename(), is("../logs/hub.log"));
        assertThat(configuration.getAggregatedFileLogConfiguration().getNumberOfFiles(), is(5));

        assertThat(configuration.getTimeStampAggregatedFileLogConfiguration(), is(nullValue()));

        assertThat(configuration.getExportBridges().size(), is(0));
        assertThat(configuration.getImportBridges().size(), is(0));
    }

    @Test public void test_default_config() throws Exception {

        int socketHubPort = NetUtils.findFreePort();
        int telemtryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemtryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", "xml");
        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        assertThat(mainInternal, is(nullValue()));
    }

    @Test public void test_old_style_file_appender_sync() throws Exception {

        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", "xml");
        File logFile = FileUtils.getRandomFile("target/test", "hub", ".log");

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(socketHubPort);
        configuration.setAggregatedFileLogConfiguration(new RollingFileLoggerConfiguration());
        configuration.getAggregatedFileLogConfiguration().setWriteAsynchronously(false);
        configuration.getAggregatedFileLogConfiguration().setFilename(StringUtils.before(logFile.getName(), ".log"));
        configuration.getAggregatedFileLogConfiguration().setFolder(logFile.getParent());
        configuration.setTimeStampAggregatedFileLogConfiguration(null);

        configuration.writeToFile(configurationFile);

        assertThat(NetUtils.isPortOpen(socketHubPort), is(false));
        assertThat(NetUtils.isPortOpen(telemetryHubPort), is(false));

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        assertThat(mainInternal.getAggregatedFileLogger(), is(not(nullValue())));
        assertThat(mainInternal.getTimestampAggregatedFileLogger(), is(nullValue()));

        assertThat(mainInternal.getAggregatedFileLoggerPool(), is(not(nullValue())));
        assertThat(mainInternal.getTimestampAggregatedFileLoggerPool(), is(nullValue()));

        LogEventBucket eventBucketA = new LogEventBucket();
        LogEventBucket eventBucketB = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);
        SocketClient clientB = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketB);

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent event2 = LogEventFactory.createFullLogEvent2();
        DefaultLogEvent event3 = LogEventFactory.createFullLogEvent3();

        clientA.send(new LogEventMessage(event1));
        clientA.send(new LogEventMessage(event2));
        clientA.send(new LogEventMessage(event3));

        eventBucketB.waitForMessages(3, 30, TimeUnit.SECONDS);
        assertThat(eventBucketB.get(0).getMessage(), is(event1.getMessage()));
        assertThat(eventBucketB.get(1).getMessage(), is(event2.getMessage()));
        assertThat(eventBucketB.get(2).getMessage(), is(event3.getMessage()));

        clientA.close();
        clientB.close();
        mainInternal.getHub().shutdown();

        String[] logFileContents = FileUtils.readAsStringArray(logFile);

        assertThat(logFileContents[0], endsWith(event1.getMessage()));
        assertThat(logFileContents[1], endsWith(event2.getMessage()));
        assertThat(logFileContents[2], endsWith(event3.getMessage()));

    }

    @Test public void test_new_style_file_appender_sync() throws Exception {
        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", ".xml");
        final File logFile = FileUtils.getRandomFile("target/test", "hub", "");

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(socketHubPort);
        configuration.setTimeStampAggregatedFileLogConfiguration(new TimestampVariableRollingFileLoggerConfiguration());
        configuration.getTimeStampAggregatedFileLogConfiguration().setWriteAsynchronously(false);
        configuration.getTimeStampAggregatedFileLogConfiguration().setFolder(logFile.getParent());
        configuration.getTimeStampAggregatedFileLogConfiguration().setFilename(logFile.getName());
        configuration.setAggregatedFileLogConfiguration(null);

        configuration.writeToFile(configurationFile);

        assertThat(NetUtils.isPortOpen(socketHubPort), is(false));
        assertThat(NetUtils.isPortOpen(telemetryHubPort), is(false));

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        assertThat(mainInternal.getAggregatedFileLogger(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLogger(), is(not(nullValue())));

        assertThat(mainInternal.getAggregatedFileLoggerPool(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLoggerPool(), is(not(nullValue())));

        LogEventBucket eventBucketA = new LogEventBucket();
        LogEventBucket eventBucketB = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);
        SocketClient clientB = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketB);

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent event2 = LogEventFactory.createFullLogEvent2();
        DefaultLogEvent event3 = LogEventFactory.createFullLogEvent3();

        clientA.send(new LogEventMessage(event1));
        clientA.send(new LogEventMessage(event2));
        clientA.send(new LogEventMessage(event3));

        eventBucketB.waitForMessages(3, 30, TimeUnit.SECONDS);
        assertThat(eventBucketB.get(0).getMessage(), is(event1.getMessage()));
        assertThat(eventBucketB.get(1).getMessage(), is(event2.getMessage()));
        assertThat(eventBucketB.get(2).getMessage(), is(event3.getMessage()));

        clientA.close();
        clientB.close();
        mainInternal.getHub().shutdown();

        List<File> files = FileUtils.findFilesRecursively(logFile.getParentFile(), new FileFilter() {
            @Override public boolean accept(File file) {
                return file.getName().startsWith(logFile.getName());
            }
        });

        Map<String, File> map = FileUtils.convertFilesToMap(logFile.getParentFile(), files);
        
        assertThat(files.size(), is(2));

        final File actualLogFile = map.get(logFile.getName() + ".log");
        String[] logFileContents = FileUtils.readAsStringArray(actualLogFile);

        assertThat(logFileContents[0], endsWith(event1.getMessage()));
        assertThat(logFileContents[1], endsWith(event2.getMessage()));
        assertThat(logFileContents[2], endsWith(event3.getMessage()));
    }

    @Test public void test_old_style_file_appender_async() throws Exception {
        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", "xml");
        final File logFile = FileUtils.getRandomFile("target/test", "hub", ".log");

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(socketHubPort);
        configuration.setAggregatedFileLogConfiguration(new RollingFileLoggerConfiguration());
        configuration.getAggregatedFileLogConfiguration().setWriteAsynchronously(true);
        configuration.getAggregatedFileLogConfiguration().setFilename(StringUtils.before(logFile.getName(), ".log"));
        configuration.getAggregatedFileLogConfiguration().setFolder(logFile.getParent());
        configuration.setTimeStampAggregatedFileLogConfiguration(null);

        configuration.writeToFile(configurationFile);

        assertThat(NetUtils.isPortOpen(socketHubPort), is(false));
        assertThat(NetUtils.isPortOpen(telemetryHubPort), is(false));

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        assertThat(mainInternal.getAggregatedFileLogger(), is(not(nullValue())));
        assertThat(mainInternal.getTimestampAggregatedFileLogger(), is(nullValue()));

        assertThat(mainInternal.getAggregatedFileLoggerPool(), is(not(nullValue())));
        assertThat(mainInternal.getTimestampAggregatedFileLoggerPool(), is(nullValue()));

        LogEventBucket eventBucketA = new LogEventBucket();
        LogEventBucket eventBucketB = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);
        SocketClient clientB = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketB);

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent event2 = LogEventFactory.createFullLogEvent2();
        DefaultLogEvent event3 = LogEventFactory.createFullLogEvent3();

        clientA.send(new LogEventMessage(event1));
        clientA.send(new LogEventMessage(event2));
        clientA.send(new LogEventMessage(event3));

        eventBucketB.waitForMessages(3, 30, TimeUnit.SECONDS);
        assertThat(eventBucketB.get(0).getMessage(), is(event1.getMessage()));
        assertThat(eventBucketB.get(1).getMessage(), is(event2.getMessage()));
        assertThat(eventBucketB.get(2).getMessage(), is(event3.getMessage()));

        clientA.close();
        clientB.close();

        mainInternal.flush();
        mainInternal.getHub().shutdown();

        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                String[] logFileContents = FileUtils.readAsStringArray(logFile);
                return logFileContents.length == 3;
            }
        });

        String[] logFileContents = FileUtils.readAsStringArray(logFile);

        assertThat(logFileContents[0], endsWith(event1.getMessage()));
        assertThat(logFileContents[1], endsWith(event2.getMessage()));
        assertThat(logFileContents[2], endsWith(event3.getMessage()));
    }

    @Test public void test_new_style_file_appender_async() throws Exception {
        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", ".xml");
        final File logFile = FileUtils.getRandomFile("target/test", "hub", "");

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setPort(socketHubPort);
        configuration.setTimeStampAggregatedFileLogConfiguration(new TimestampVariableRollingFileLoggerConfiguration());
        configuration.getTimeStampAggregatedFileLogConfiguration().setWriteAsynchronously(true);
        configuration.getTimeStampAggregatedFileLogConfiguration().setFolder(logFile.getParent());
        configuration.getTimeStampAggregatedFileLogConfiguration().setFilename(logFile.getName());
        configuration.setAggregatedFileLogConfiguration(null);

        configuration.writeToFile(configurationFile);

        assertThat(NetUtils.isPortOpen(socketHubPort), is(false));
        assertThat(NetUtils.isPortOpen(telemetryHubPort), is(false));

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        assertThat(mainInternal.getAggregatedFileLogger(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLogger(), is(not(nullValue())));

        assertThat(mainInternal.getAggregatedFileLoggerPool(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLoggerPool(), is(not(nullValue())));

        LogEventBucket eventBucketA = new LogEventBucket();
        LogEventBucket eventBucketB = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);
        SocketClient clientB = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketB);

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent event2 = LogEventFactory.createFullLogEvent2();
        DefaultLogEvent event3 = LogEventFactory.createFullLogEvent3();

        clientA.send(new LogEventMessage(event1));
        clientA.send(new LogEventMessage(event2));
        clientA.send(new LogEventMessage(event3));

        eventBucketB.waitForMessages(3, 30, TimeUnit.SECONDS);
        assertThat(eventBucketB.get(0).getMessage(), is(event1.getMessage()));
        assertThat(eventBucketB.get(1).getMessage(), is(event2.getMessage()));
        assertThat(eventBucketB.get(2).getMessage(), is(event3.getMessage()));

        clientA.close();
        clientB.close();

        mainInternal.flush();
        mainInternal.getHub().shutdown();

        List<File> files = FileUtils.findFilesRecursively(logFile.getParentFile(), new FileFilter() {
            @Override public boolean accept(File file) {
                return file.getName().startsWith(logFile.getName());
            }
        });

        Map<String, File> map = FileUtils.convertFilesToMap(logFile.getParentFile(), files);
        
        assertThat(files.size(), is(2));

        final File actualLogFile = map.get(logFile.getName() + ".log");
        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                String[] logFileContents = FileUtils.readAsStringArray(actualLogFile);
                return logFileContents.length == 3;
            }
        });

        String[] logFileContents = FileUtils.readAsStringArray(actualLogFile);

        assertThat(logFileContents[0], endsWith(event1.getMessage()));
        assertThat(logFileContents[1], endsWith(event2.getMessage()));
        assertThat(logFileContents[2], endsWith(event3.getMessage()));
    }

    @Test public void test_sub_aggregator_async() throws Exception {
        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", ".xml");

        SocketHubConfiguration configuration = new SocketHubConfiguration();
        configuration.setPort(socketHubPort);

        configuration.writeToFile(configurationFile);

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        AggregatedFileLogger logger = Mockito.mock(AggregatedFileLogger.class);

        ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        FileLogConfiguration config = new FileLogConfiguration() {

            @Override public boolean getWriteAsynchronously() {
                return true;

            }

            @Override public boolean getAutoNewline() {
                return true;

            }

            @Override public String getPattern() {
                return "";

            }

            @Override public String getFormatter() {
                return "";

            }

            @Override public int getAsynchronousQueueWarningSize() {
                return 3;

            }

            @Override public int getAsynchronousQueueDiscardSize() {
                return 4;

            }

            @Override public boolean getForceFlush() {
                return false;
            }
        };
        mainInternal.setupLocalLogAggregator(config, logger, pool);

        assertThat(mainInternal.getAggregatedFileLogger(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLogger(), is(nullValue()));

        assertThat(mainInternal.getAggregatedFileLoggerPool(), is(nullValue()));
        assertThat(mainInternal.getTimestampAggregatedFileLoggerPool(), is(nullValue()));

        LogEventBucket eventBucketA = new LogEventBucket();
        LogEventBucket eventBucketB = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);
        SocketClient clientB = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketB);

        DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent event2 = LogEventFactory.createFullLogEvent2();
        DefaultLogEvent event3 = LogEventFactory.createFullLogEvent3();

        clientA.send(new LogEventMessage(event1));
        clientA.send(new LogEventMessage(event2));
        clientA.send(new LogEventMessage(event3));

        eventBucketB.waitForMessages(3, 30, TimeUnit.SECONDS);
        assertThat(eventBucketB.get(0).getMessage(), is(event1.getMessage()));
        assertThat(eventBucketB.get(1).getMessage(), is(event2.getMessage()));
        assertThat(eventBucketB.get(2).getMessage(), is(event3.getMessage()));

        clientA.close();
        clientB.close();

        mainInternal.flush();
        mainInternal.getHub().shutdown();

        // Not sure what to do, need to keep going until its been called three
        // times?!
        ThreadUtils.sleep(200);

        Mockito.verify(logger, Mockito.times(3)).send(Mockito.any(LogEventMessage.class));

    }

    @Test public void test_max_client_send_queue_size() throws Exception {

        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", ".xml");

        SocketHubConfiguration configuration = new SocketHubConfiguration();
        configuration.setMaximumClientSendQueueSize(1);
        configuration.setPort(socketHubPort);

        configuration.writeToFile(configurationFile);

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        LogEventBucket eventBucketA = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);

        List<SocketConnectionInterface> connectionsList = mainInternal.getHub().getConnectionsList();
        assertThat(connectionsList.size(), is(1));
        SocketConnectionInterface socketConnection = connectionsList.get(0);
        assertThat(socketConnection.getWriteQueue().size(), is(0));
        assertThat(socketConnection.getWriteQueue().remainingCapacity(), is(1));

        clientA.close();

        mainInternal.flush();
        mainInternal.getHub().shutdown();

    }

    @Test public void test_max_client_send_queue_default() throws Exception {

        int socketHubPort = NetUtils.findFreePort();
        int telemetryHubPort = NetUtils.findFreePort();
        System.setProperty(VLPorts.socketHubProperty, Integer.toString(socketHubPort));
        System.setProperty(VLPorts.telemetryProperty, Integer.toString(telemetryHubPort));

        File configurationFile = FileUtils.getRandomFile("target/test", "hub", ".xml");

        SocketHubConfiguration configuration = new SocketHubConfiguration();
        configuration.setPort(socketHubPort);

        configuration.writeToFile(configurationFile);

        LegacyRunHub mainInternal = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });
        mainInternal.waitUntilBound();

        LogEventBucket eventBucketA = new LogEventBucket();

        SocketClient clientA = SocketClient.connect(new InetSocketAddress("localhost", socketHubPort), true, eventBucketA);

        List<SocketConnectionInterface> connectionsList = mainInternal.getHub().getConnectionsList();
        assertThat(connectionsList.size(), is(1));
        SocketConnectionInterface socketConnection = connectionsList.get(0);
        assertThat(socketConnection.getWriteQueue().size(), is(0));
        assertThat(socketConnection.getWriteQueue().remainingCapacity(), is(SocketConnection.writeBufferDefaultSize));

        clientA.close();

        mainInternal.flush();
        mainInternal.getHub().shutdown();

    }

}
