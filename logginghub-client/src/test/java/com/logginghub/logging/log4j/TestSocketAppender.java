package com.logginghub.logging.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.log4j.SocketAppender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.logging.handlers.SimpleServer;

@RunWith(CustomRunner.class)
public class TestSocketAppender {

    private File properties;
    private SocketAppender appender;

    @Before public void setup() {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        File folder = FileUtils.createRandomTestFolderForClass(TestSocketAppender.class);
        properties = new File(folder, "log4j,properties");

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost");
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());     
        
        List<Appender> appenders = new ArrayList<Appender>();
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            appenders.add((Appender) allAppenders.nextElement());
        }

        appender = (SocketAppender) appenders.get(0);
    }
    
    @After public void stop() {
        LogManager.shutdown();
    }

    @Test public void test_default() {
        List<Appender> appenders = new ArrayList<Appender>();
        Enumeration allAppenders = Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            appenders.add((Appender) allAppenders.nextElement());
        }

        assertThat(appenders.size(), is(1));
        assertThat(appenders.get(0), is(instanceOf(SocketAppender.class)));

        SocketAppender appender = (SocketAppender) appenders.get(0);

        assertThat(appender.getFailureDelay(), is(50L));
        assertThat(appender.getMaxDispatchQueueSize(), is(1000));
        assertThat(appender.getSourceApplication(), is("Test application"));
        assertThat(appender.getTelemetry(), is(nullValue()));
        assertThat(appender.getThreshold(), is(nullValue()));
        assertThat(appender.isDontThrowExceptionsIfHubIsntUp(), is(false));
        assertThat(appender.isPublishMachineTelemetry(), is(false));
        assertThat(appender.isPublishProcessTelemetry(), is(false));
        assertThat(appender.isUseDispatchThread(), is(true));
    }

    @Test public void test_pid() throws LoggingMessageSenderException {

        SocketClient mockClient = Mockito.mock(SocketClient.class);
        appender.setSocketClient(mockClient);

        LoggingEvent event = new LoggingEvent("fqnOfCategoryClass", Category.getInstance("logger"), Priority.ERROR, "Message", null);
        appender.append(event);

        ArgumentCaptor<LoggingMessage> argument = ArgumentCaptor.forClass(LoggingMessage.class);
        Mockito.verify(mockClient, Mockito.timeout(1000).times(1)).send(argument.capture());

        LoggingMessage value = argument.getValue();

        assertThat(value, is(instanceOf(LogEventMessage.class)));
        LogEventMessage message = (LogEventMessage) value;

        LogEvent logEvent = message.getLogEvent();

        assertThat(logEvent.getPid(), is(not(-1)));
        assertThat(logEvent.getPid(), is(not(0)));
    }

    @Test public void test_capture_thread_details() throws InterruptedException {

        SimpleServer server = new SimpleServer();
        server.setListeningPort(NetUtils.findFreePort());
        server.start();
        server.waitUntilBound();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost:{}", server.getListeningPort());
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());
        
        final Logger logger = Logger.getLogger("test");

        WorkerThread.execute("TestThread", new Runnable() {
            public void run() {
                logger.info("Test message");
            }
        });
        
        server.getBucket().waitForMessages(1);
        LogEvent logEvent = server.getBucket().get(0);

        assertThat(logEvent.getThreadName(), is("TestThread"));
        assertThat(logEvent.getMessage(), is("Test message"));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
        assertThat(logEvent.getLoggerName(), is("test"));
        // Might not have location info on?
//        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.log4j.TestSocketAppender$")));
//        assertThat(logEvent.getSourceMethodName(), is("run"));
        assertThat(logEvent.getChannel(), is(nullValue()));
        
        server.shutdown();
    }
    
    @Test public void test_channel() throws InterruptedException {

        SimpleServer server = new SimpleServer();
        server.setListeningPort(NetUtils.findFreePort());
        server.start();
        server.waitUntilBound();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost:{}", server.getListeningPort());
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
        writer.appendLine("log4j.appender.loggingHub.channel=testChannel");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());
        
        final Logger logger = Logger.getLogger("test");

        WorkerThread.execute("TestThread", new Runnable() {
            public void run() {
                logger.info("Test message");
            }
        });
        
        server.getBucket().waitForMessages(1);
        LogEvent logEvent = server.getBucket().get(0);

        assertThat(logEvent.getThreadName(), is("TestThread"));
        assertThat(logEvent.getMessage(), is("Test message"));
        assertThat(logEvent.getSourceApplication(), is("Test application"));
        assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
        assertThat(logEvent.getLoggerName(), is("test"));
//        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.log4j.TestSocketAppender$")));
//        assertThat(logEvent.getSourceMethodName(), is("run"));
        assertThat(logEvent.getChannel(), is("testChannel"));
        
        server.shutdown();
    }

    @Test public void test_stack_traces_on() throws InterruptedException {
              
        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost:58770");
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
        
        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleEnabled=true");
        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleBroadcastInterval=15 seconds");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());
        
        Appender appender = Logger.getRootLogger().getAppender("loggingHub");
        assertThat(appender, is(instanceOf(SocketAppender.class)));
        SocketAppender socketAppender = (SocketAppender)appender;
        
        assertThat(socketAppender.isStackTraceModuleEnabled(), is(true));
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("15 seconds"));
    }

    @Test public void test_stack_traces_off() throws InterruptedException {
        
        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost:58770");
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
        
        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleEnabled=false");
        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleBroadcastInterval=9 seconds");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());
        
        Appender appender = Logger.getRootLogger().getAppender("loggingHub");
        assertThat(appender, is(instanceOf(SocketAppender.class)));
        SocketAppender socketAppender = (SocketAppender)appender;
        
        assertThat(socketAppender.isStackTraceModuleEnabled(), is(false));
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("9 seconds"));
    }
    
}
