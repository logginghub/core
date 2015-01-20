package com.logginghub.logging.logback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;

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

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.logback.SocketAppender;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.logging.handlers.SimpleServer;

@RunWith(CustomRunner.class) public class TestSocketAppender {

    private File properties;

    @Before public void setup() {
        File folder = FileUtils.createRandomTestFolderForClass(TestSocketAppender.class);
        properties = new File(folder, "logback.xml");
    }

    @Test public void test_logback_socket_config_load() throws LoggingMessageSenderException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure("src/test/resources/com/logginghub/logging/logback/socket_appender_config.xml");
        }
        catch (JoranException je) {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        Logger logger = LoggerFactory.getLogger("com.logginghub.logging.logback.LoggingTest");

        Logger rootLogger = LoggerFactory.getLogger("ROOT");

        assertThat(rootLogger, is(instanceOf(ch.qos.logback.classic.Logger.class)));

        ch.qos.logback.classic.Logger actualLogger = (ch.qos.logback.classic.Logger) rootLogger;

        Appender<ILoggingEvent> appender = actualLogger.getAppender("socket");
        assertThat(appender, is(not(nullValue())));
        assertThat(appender, is(instanceOf(com.logginghub.logging.logback.SocketAppender.class)));

        final SocketAppender socketAppender = (com.logginghub.logging.logback.SocketAppender) appender;
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();

        // TODO : assert the various configuration properties

    }

    @Test public void test_config_settings() throws InterruptedException {

        SimpleServer server = new SimpleServer();
        server.setListeningPort(NetUtils.findFreePort());
        server.start();
        server.waitUntilBound();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:{}</host>", server.getListeningPort());
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

        WorkerThread wt = new WorkerThread("TestThread") {
            @Override protected void onRun() throws Throwable {
                String param = "12323";
                actualLogger.info("Test message with a parameter '{}'", param);
            }
        };
        wt.start();

        server.getBucket().waitForMessages(1);
        LogEvent logEvent = server.getBucket().get(0);

        assertThat(logEvent.getThreadName(), is("TestThread"));
        assertThat(logEvent.getMessage(), is("Test message with a parameter '12323'"));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
        assertThat(logEvent.getLoggerName(), is("test"));
        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.logback.TestSocketAppender$")));
        assertThat(logEvent.getSourceMethodName(), is("onRun"));
        assertThat(logEvent.getChannel(), is("testChannel"));

        wt.stop();
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
        server.shutdown();
    }

    @Test public void test_cpu_monitoring() throws InterruptedException {

        SimpleServer server = new SimpleServer();
        server.setListeningPort(NetUtils.findFreePort());
        server.start();
        server.waitUntilBound();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:{}</host>", server.getListeningPort());
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("    <cpuLogging>true</cpuLogging>");
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

        server.getBucket().waitForMessages(1);
        LogEvent logEvent = server.getBucket().get(0);

        assertThat(logEvent.getThreadName(), is("CpuLoggingTimer"));
        assertThat(logEvent.getMessage(), is(startsWith("Summary Cpu stats")));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.debug));
        assertThat(logEvent.getLoggerName(), is("cpu-logger"));
        assertThat(logEvent.getSourceMethodName(), is("log"));
        assertThat(logEvent.getChannel(), is("testChannel"));

        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
        server.shutdown();
    }

    @Test public void test_heap_monitoring() throws InterruptedException {

        SimpleServer server = new SimpleServer();
        server.setListeningPort(NetUtils.findFreePort());
        server.start();
        server.waitUntilBound();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:{}</host>", server.getListeningPort());
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("    <heapLogging>true</heapLogging>");
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

        server.getBucket().waitForMessages(1);
        LogEvent logEvent = server.getBucket().get(0);

        assertThat(logEvent.getThreadName(), is("HeapLoggingTimer"));
        assertThat(logEvent.getMessage(), is(startsWith("Heap status : used memory")));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.debug));
        assertThat(logEvent.getLoggerName(), is("heap-logger"));
        assertThat(logEvent.getSourceMethodName(), is("log"));
        assertThat(logEvent.getChannel(), is("testChannel"));

        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
        server.shutdown();
    }

    @Test public void test_stack_traces_on() throws InterruptedException {

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:58770</host>");
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("    <stackTraceModuleEnabled>true</stackTraceModuleEnabled>");
        writer.appendLine("    <stackTraceModuleBroadcastInterval>15 seconds</stackTraceModuleBroadcastInterval>");
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
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("15 seconds"));

        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }

    @Test public void test_stack_traces_off() throws InterruptedException {
        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("<configuration>");
        writer.appendLine("  <appender name='logginghub' class='com.logginghub.logging.logback.SocketAppender'>");
        writer.appendLine("    <sourceApplication>${app}</sourceApplication>");
        writer.appendLine("    <host>localhost:58770</host>");
        writer.appendLine("    <channel>testChannel</channel>");
        writer.appendLine("    <stackTraceModuleEnabled>false</stackTraceModuleEnabled>");
        writer.appendLine("    <stackTraceModuleBroadcastInterval>9 seconds</stackTraceModuleBroadcastInterval>");
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

        assertThat(socketAppender.isStackTraceModuleEnabled(), is(false));
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("9 seconds"));

        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
    }

}
