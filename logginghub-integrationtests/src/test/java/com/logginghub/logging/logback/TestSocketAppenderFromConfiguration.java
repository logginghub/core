package com.logginghub.logging.logback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.logback.SocketAppender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;

@RunWith(CustomRunner.class) public class TestSocketAppenderFromConfiguration {

    private File properties;

    private HubTestFixture fixture = new HubTestFixture();

    @Before public void setup() {
        File folder = FileUtils.createRandomTestFolderForClass(TestSocketAppenderFromConfiguration.class);
        properties = new File(folder, "logback.xml");
    }

    @After public void cleanup() throws IOException {
        fixture.stop();
    }

    // TODO : move this to hub integration test
//    @Test public void test_hub_shutdown_telemetry() throws InterruptedException, ConnectorException {
//        fixture.getSocketHubAConfiguration().setTelemetryPort(12312);
//        SocketHub hub = fixture.getSocketHubA();
//    }
    
    @Test public void test_config_settings() throws InterruptedException, ConnectorException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<LogEvent> bucket = fixture.createEventBucketFor(client);

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:{}</host>", hub.getPort());
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("  </appender>");

        writer.appendLine("  <root level='all'>");
        writer.appendLine("  <appender-ref ref='logginghub' />");
        writer.appendLine("  </root>");
        writer.appendLine("</configuration>");

        writer.close();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(properties.getAbsolutePath());
        }
        catch (JoranException je) {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        Logger logger = LoggerFactory.getLogger("test");

        Logger rootLogger = LoggerFactory.getLogger("ROOT");

        assertThat(rootLogger, is(instanceOf(ch.qos.logback.classic.Logger.class)));

        final ch.qos.logback.classic.Logger actualLogger = (ch.qos.logback.classic.Logger) logger;

        WorkerThread.execute("TestThread", new Runnable() {
            @Override public void run() {
                String param = "12323";
                actualLogger.info("Test message with a parameter '{}'", param);
            }
        });

        bucket.waitForMessages(1);

        LogEvent logEvent = bucket.get(0);

        assertThat(logEvent.getThreadName(), is("TestThread"));
        assertThat(logEvent.getMessage(), is("Test message with a parameter '12323'"));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
        assertThat(logEvent.getLoggerName(), is("test"));
        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.logback.TestSocketAppenderFromConfiguration$")));
        assertThat(logEvent.getSourceMethodName(), is("run"));
        assertThat(logEvent.getChannel(), is("testChannel"));

        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }

    // @Test public void test_cpu_monitoring() throws InterruptedException {
    //
    // SimpleServer server = new SimpleServer();
    // server.setListeningPort(NetUtils.findFreePort());
    // server.start();
    // server.waitUntilBound();
    //
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("<configuration>");
    // writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
    // writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
    // writer.appendLine("    <host>localhost:{}</host>", server.getListeningPort());
    // writer.appendLine("    <channel>testChannel</channel>");
    // writer.appendLine("    <cpuLogging>true</cpuLogging>");
    // writer.appendLine("  </appender>");
    //
    // writer.appendLine("  <root level='all'>");
    // writer.appendLine("  <appender-ref ref='logginghub' />");
    // writer.appendLine("  </root>");
    // writer.appendLine("</configuration>");
    //
    // writer.close();
    //
    // LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    //
    // try {
    // JoranConfigurator configurator = new JoranConfigurator();
    // configurator.setContext(context);
    // context.reset();
    // configurator.doConfigure(properties.getAbsolutePath());
    // }
    // catch (JoranException je) {
    // je.printStackTrace();
    // }
    // StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    //
    // server.getBucket().waitForMessages(1);
    // LogEvent logEvent = server.getBucket().get(0);
    //
    // assertThat(logEvent.getThreadName(), is("CpuLoggingTimer"));
    // assertThat(logEvent.getMessage(), is(startsWith("Summary Cpu stats")));
    // assertThat(logEvent.getSourceApplication(), is("Test application"));
    // assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.debug));
    // assertThat(logEvent.getLoggerName(), is("cpu-logger"));
    // assertThat(logEvent.getSourceMethodName(), is("log"));
    // assertThat(logEvent.getChannel(), is("testChannel"));
    //
    // ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    // server.shutdown();
    // }
    //
    // @Test public void test_heap_monitoring() throws InterruptedException {
    //
    // SimpleServer server = new SimpleServer();
    // server.setListeningPort(NetUtils.findFreePort());
    // server.start();
    // server.waitUntilBound();
    //
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("<configuration>");
    // writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
    // writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
    // writer.appendLine("    <host>localhost:{}</host>", server.getListeningPort());
    // writer.appendLine("    <channel>testChannel</channel>");
    // writer.appendLine("    <heapLogging>true</heapLogging>");
    // writer.appendLine("  </appender>");
    //
    // writer.appendLine("  <root level='all'>");
    // writer.appendLine("  <appender-ref ref='logginghub' />");
    // writer.appendLine("  </root>");
    // writer.appendLine("</configuration>");
    //
    // writer.close();
    //
    // LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    //
    // try {
    // JoranConfigurator configurator = new JoranConfigurator();
    // configurator.setContext(context);
    // context.reset();
    // configurator.doConfigure(properties.getAbsolutePath());
    // }
    // catch (JoranException je) {
    // je.printStackTrace();
    // }
    // StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    //
    // server.getBucket().waitForMessages(1);
    // LogEvent logEvent = server.getBucket().get(0);
    //
    // assertThat(logEvent.getThreadName(), is("HeapLoggingTimer"));
    // assertThat(logEvent.getMessage(), is(startsWith("Heap status : used memory")));
    // assertThat(logEvent.getSourceApplication(), is("Test application"));
    // assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.debug));
    // assertThat(logEvent.getLoggerName(), is("heap-logger"));
    // assertThat(logEvent.getSourceMethodName(), is("log"));
    // assertThat(logEvent.getChannel(), is("testChannel"));
    //
    // ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    // server.shutdown();
    // }
    //
    @Test public void test_stack_traces_on() throws InterruptedException, ConnectorException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<ChannelMessage> bucket = fixture.getChannelBucketFor(Channels.stackSnapshots, client);
        
        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "instanceType-234");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:{}</host>", hub.getPort());
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("    <stackTraceModuleEnabled>true</stackTraceModuleEnabled>");
        writer.appendLine("    <stackTraceModuleBroadcastInterval>10 ms</stackTraceModuleBroadcastInterval>");
        writer.appendLine("  </appender>");

        writer.appendLine("  <root level='all'>");
        writer.appendLine("  <appender-ref ref='logginghub' />");
        writer.appendLine("  </root>");
        writer.appendLine("</configuration>");

        writer.close();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(properties.getAbsolutePath());
        }
        catch (JoranException je) {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        Appender<ILoggingEvent> appender = context.getLogger("ROOT").getAppender("logginghub");

        assertThat(appender, is(instanceOf(SocketAppender.class)));
        SocketAppender socketAppender = (SocketAppender) appender;

        assertThat(socketAppender.isStackTraceModuleEnabled(), is(true));
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("10 ms"));

        bucket.waitForMessages(1);
        assertThat(bucket.get(0).getPayload(), is(instanceOf(StackSnapshot.class)));
        StackSnapshot snapshot = (StackSnapshot) bucket.get(0).getPayload();
        assertThat(snapshot.getEnvironment(), is("environment"));
        assertThat(snapshot.getHost(), is(NetUtils.getLocalHostname()));
        assertThat(snapshot.getInstanceNumber(), is(234));
        assertThat(snapshot.getInstanceType(), is("instanceType"));
        
        
        
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }
    //
    // @Test public void test_stack_traces_off() throws InterruptedException {
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("<configuration>");
    // writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
    // writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
    // writer.appendLine("    <host>localhost:58770</host>");
    // writer.appendLine("    <channel>testChannel</channel>");
    // writer.appendLine("    <stackTraceModuleEnabled>false</stackTraceModuleEnabled>");
    // writer.appendLine("    <stackTraceModuleBroadcastInterval>9 seconds</stackTraceModuleBroadcastInterval>");
    // writer.appendLine("  </appender>");
    //
    // writer.appendLine("  <root level='all'>");
    // writer.appendLine("  <appender-ref ref='logginghub' />");
    // writer.appendLine("  </root>");
    // writer.appendLine("</configuration>");
    //
    // writer.close();
    //
    // LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    //
    // try {
    // JoranConfigurator configurator = new JoranConfigurator();
    // configurator.setContext(context);
    // context.reset();
    // configurator.doConfigure(properties.getAbsolutePath());
    // }
    // catch (JoranException je) {
    // je.printStackTrace();
    // }
    // StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    //
    // Appender<ILoggingEvent> appender = context.getLogger("ROOT").getAppender("logginghub");
    //
    // assertThat(appender, is(instanceOf(SocketAppender.class)));
    // SocketAppender socketAppender = (SocketAppender) appender;
    //
    // assertThat(socketAppender.isStackTraceModuleEnabled(), is(false));
    // assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("9 seconds"));
    //
    // ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    // }

}
