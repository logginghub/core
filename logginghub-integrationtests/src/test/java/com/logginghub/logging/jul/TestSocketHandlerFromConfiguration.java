package com.logginghub.logging.jul;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.handlers.SocketHandler;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.ThreadUtils;

@RunWith(CustomRunner.class) public class TestSocketHandlerFromConfiguration {
    private File properties;

    private HubTestFixture fixture = new HubTestFixture();

    @Before public void setup() throws InterruptedException {
        properties = FileUtils.getRandomFile("target/test/testsockethandler", "logging", ".properties");

        LogManager logManager = LogManager.getLogManager();
        logManager.reset();
    }

    @After public void teardown() throws IOException {
        LogManager logManager = LogManager.getLogManager();
        Handler[] handlers = logManager.getLogger("").getHandlers();
        for (Handler handler : handlers) {
            handler.close();
        }

        LogManager.getLogManager().reset();

        // jshaw - hack to try and get the kryo threads to die before we finish and the thread
        // checker fails us :/
        ThreadUtils.sleep(500);

        fixture.stop();
    }

//    @Test public void test_environment_variable_replacments() throws SecurityException, FileNotFoundException, IOException {
//        File randomFile = FileUtils.getRandomFile("target/test/testsockethandler", "logging", ".properties");
//        StringBuilder sb = new StringBuilder();
//
//        String newline = String.format("%n");
//
//        System.setProperty("appname", "Test app");
//        System.setProperty("channel", "Test Channel");
//
//        sb.append("handlers=java.util.logging.ConsoleHandler, com.logginghub.logging.handlers.SocketHandler").append(newline);
//        sb.append(".level = INFO").append(newline);
//        sb.append("com.logginghub.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.level = WARNING").append(newline);
//        sb.append("com.hazelcast.level = WARNING").append(newline);
//        sb.append("java.util.logging.ConsoleHandler.level = ALL").append(newline);
//        sb.append("java.util.logging.ConsoleHandler.formatter = com.logginghub.utils.logging.SingleLineFormatter").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.connectionPoints=localhost").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.applicationName=${appname}").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.channel=${channel}").append(newline);
//
//        FileUtils.write(sb.toString(), randomFile);
//
//        LogManager logManager = java.util.logging.LogManager.getLogManager();
//        logManager.readConfiguration(new FileInputStream(randomFile));
//
//        Handler[] handlers = logManager.getLogger("").getHandlers();
//        assertThat(handlers.length, is(2));
//
//        SocketHandler socketHandler = (SocketHandler) handlers[1];
//        assertThat(socketHandler.getSourceApplication(), is("Test app"));
//        assertThat(socketHandler.getAppenderHelper().getChannel(), is("Test Channel"));
//    }
//
//    @Test public void testSocketHandlerAsync() {
//
//        SocketHandler handler = new SocketHandler();
//        handler.setForceFlush(true);
//        handler.addConnectionPoint(server.getConnectionPoint());
//
//        LogRecord record = LogRecordFactory.getLogRecord1();
//        handler.publish(record);
//
//        handler.waitUntilAllRecordsHaveBeenPublished();
//
//        server.getBucket().waitForMessages(1, 5, TimeUnit.SECONDS);
//        assertEquals(1, server.getBucket().getEvents().size());
//
//        handler.close();
//    }
//
//    @Test public void testSocketHandlerAsyncLots() {
//        SocketHandler handler = new SocketHandler();
//        handler.addConnectionPoint(server.getConnectionPoint());
//
//        LogRecord record = LogRecordFactory.getLogRecord1();
//
//        int count = 500;
//        for (int i = 0; i < count; i++) {
//            handler.publish(record);
//        }
//
//        assertTrue(server.getBucket().getEvents().size() < count);
//
//        handler.waitUntilAllRecordsHaveBeenPublished();
//
//        server.getBucket().waitForMessages(count, 5, TimeUnit.SECONDS);
//        assertEquals(count, server.getBucket().getEvents().size());
//
//        handler.close();
//    }
//
//    @Test public void test_thread_name_captured() {
//
//        LogManager logManager = LogManager.getLogManager();
//        logManager.reset();
//        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("test");
//
//        System.setProperty("appname", "Test application");
//
//        SocketHandler handler = new SocketHandler();
//
//        handler.setUseDispatchThread(true);
//        handler.addConnectionPoint(new InetSocketAddress(server.getListeningPort()));
//        handler.setSourceApplication("${appname}");
//        handler.setGatheringCallerDetails(true);
//        logger.addHandler(handler);
//
//        WorkerThread.execute("TestThread", new Runnable() {
//            public void run() {
//                logger.info("Test message");
//            }
//        });
//
//        server.getBucket().waitForMessages(1);
//        LogEvent logEvent = server.getBucket().get(0);
//
//        assertThat(logEvent.getThreadName(), is("TestThread"));
//        assertThat(logEvent.getMessage(), is("Test message"));
//        assertThat(logEvent.getSourceApplication(), is("Test application"));
//        assertThat(logEvent.getLevel(), is(Logger.info));
//        assertThat(logEvent.getLoggerName(), is("test"));
//        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.handlers.TestSocketHandler$")));
//        assertThat(logEvent.getSourceMethodName(), is("run"));
//
//        handler.close();
//    }
//
//    @Test public void test_gathering_caller_details_off() throws SecurityException, FileNotFoundException, IOException {
//        File randomFile = FileUtils.getRandomFile("target/test/testsockethandler", "logging", ".properties");
//        StringBuilder sb = new StringBuilder();
//
//        String newline = String.format("%n");
//        System.setProperty("appname", "Test application");
//
//        sb.append("handlers=com.logginghub.logging.handlers.SocketHandler").append(newline);
//        sb.append(".level = INFO").append(newline);
//        sb.append("com.logginghub.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.level = WARNING").append(newline);
//        sb.append("com.hazelcast.level = WARNING").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.connectionPoints=localhost:").append(server.getListeningPort()).append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.applicationName=${appname}").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.gatherCallerDetails=false").append(newline);
//
//        FileUtils.write(sb.toString(), randomFile);
//
//        LogManager logManager = java.util.logging.LogManager.getLogManager();
//        logManager.readConfiguration(new FileInputStream(randomFile));
//
//        Handler[] handlers = logManager.getLogger("").getHandlers();
//        assertThat(handlers.length, is(1));
//
//        SocketHandler socketHandler = (SocketHandler) handlers[0];
//        assertThat(socketHandler.getSourceApplication(), is("Test application"));
//
//        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("test");
//
//        WorkerThread.execute("TestThread", new Runnable() {
//            public void run() {
//                logger.info("Test message");
//            }
//        });
//
//        server.getBucket().waitForMessages(1);
//        LogEvent logEvent = server.getBucket().get(0);
//
//        assertThat(logEvent.getThreadName(), is("TestThread"));
//        assertThat(logEvent.getMessage(), is("Test message"));
//        assertThat(logEvent.getSourceApplication(), is("Test application"));
//        assertThat(logEvent.getLevel(), is(Logger.info));
//        assertThat(logEvent.getLoggerName(), is("test"));
//        assertThat(logEvent.getSourceClassName(), is(nullValue()));
//        assertThat(logEvent.getSourceMethodName(), is(nullValue()));
//
//    }
//
//    @Test public void test_gathering_caller_details_on() throws SecurityException, FileNotFoundException, IOException {
//
//        StringBuilder sb = new StringBuilder();
//
//        String newline = String.format("%n");
//        System.setProperty("appname", "Test application");
//
//        sb.append("handlers=com.logginghub.logging.handlers.SocketHandler").append(newline);
//        sb.append(".level = INFO").append(newline);
//        sb.append("com.logginghub.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.level = WARNING").append(newline);
//        sb.append("com.hazelcast.level = WARNING").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.level = ALL").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.connectionPoints=localhost:").append(server.getListeningPort()).append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.applicationName=${appname}").append(newline);
//        sb.append("com.logginghub.logging.handlers.SocketHandler.gatherCallerDetails=true").append(newline);
//
//        FileUtils.write(sb.toString(), properties);
//
//        LogManager logManager = java.util.logging.LogManager.getLogManager();
//        logManager.readConfiguration(new FileInputStream(properties));
//
//        Handler[] handlers = logManager.getLogger("").getHandlers();
//        assertThat(handlers.length, is(1));
//
//        SocketHandler socketHandler = (SocketHandler) handlers[0];
//        assertThat(socketHandler.getSourceApplication(), is("Test application"));
//
//        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("test");
//
//        WorkerThread.execute("TestThread", new Runnable() {
//            public void run() {
//                logger.info("Test message");
//            }
//        });
//
//        server.getBucket().waitForMessages(1);
//        LogEvent logEvent = server.getBucket().get(0);
//
//        assertThat(logEvent.getThreadName(), is("TestThread"));
//        assertThat(logEvent.getMessage(), is("Test message"));
//        assertThat(logEvent.getSourceApplication(), is("Test application"));
//        assertThat(logEvent.getLevel(), is(Logger.info));
//        assertThat(logEvent.getLoggerName(), is("test"));
//        assertThat(logEvent.getSourceClassName(), is(startsWith("com.logginghub.logging.handlers.TestSocketHandler$")));
//        assertThat(logEvent.getSourceMethodName(), is("run"));
//    }
//
//    @Test public void testSocketHandlerSync() {
//        SocketHandler handler = new SocketHandler();
//        handler.addConnectionPoint(server.getConnectionPoint());
//
//        LogRecord record = LogRecordFactory.getLogRecord1();
//
//        int count = 500;
//        for (int i = 0; i < count; i++) {
//            handler.publish(record);
//        }
//
//        server.getBucket().waitForMessages(count, 5, TimeUnit.SECONDS);
//        assertEquals(count, server.getBucket().getEvents().size());
//
//        handler.close();
//    }

    @Ignore // jshaw - can't work out why this isn't working from the command line :/
    @Test public void test_stack_traces_on() throws InterruptedException, SecurityException, FileNotFoundException, IOException, ConnectorException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<ChannelMessage> bucket = fixture.getChannelBucketFor(Channels.stackSnapshots, client);
        
        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");

        writer.appendLine("handlers=com.logginghub.logging.handlers.SocketHandler");
        writer.appendLine(".level = INFO");
        writer.appendLine("com.logginghub.level = ALL");
        writer.appendLine("com.logginghub.logging.level = WARNING");
        writer.appendLine("com.hazelcast.level = WARNING");
        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.level = ALL");
        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.connectionPoints=localhost:{}", hub.getPort());
        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.applicationName=${appname}");
        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.gatherCallerDetails=true");

        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.stackTraceModuleEnabled=true");
        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.stackTraceModuleBroadcastInterval=10 ms");
        writer.close();

        LogManager logManager = java.util.logging.LogManager.getLogManager();
        logManager.readConfiguration(new FileInputStream(properties));

        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");

        Handler[] handlers = logger.getHandlers();
        assertThat(handlers.length, is(1));
        assertThat(handlers[0], is(instanceOf(SocketHandler.class)));
        SocketHandler socketHandler = (SocketHandler) handlers[0];

        assertThat(socketHandler.isStackTraceModuleEnabled(), is(true));
        assertThat(socketHandler.getStackTraceModuleBroadcastInterval(), is("10 ms"));
        
        bucket.waitForMessages(1);
        assertThat(bucket.get(0).getPayload(), is(instanceOf(StackSnapshot.class)));
        
        socketHandler.close();
        
    }

//    @Test public void test_stack_traces_off() throws InterruptedException, SecurityException, FileNotFoundException, IOException {
//        FileUtilsWriter writer = FileUtils.createWriter(properties);
//
//        System.setProperty("app", "Test application");
//        writer.appendLine("handlers=com.logginghub.logging.handlers.SocketHandler");
//        writer.appendLine(".level = INFO");
//        writer.appendLine("com.logginghub.level = ALL");
//        writer.appendLine("com.logginghub.logging.level = WARNING");
//        writer.appendLine("com.hazelcast.level = WARNING");
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.level = ALL");
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.connectionPoints=localhost:58770");
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.applicationName=${appname}");
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.gatherCallerDetails=true");
//
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.stackTraceModuleEnabled=false");
//        writer.appendLine("com.logginghub.logging.handlers.SocketHandler.stackTraceModuleBroadcastInterval=9 seconds");
//        writer.close();
//
//        LogManager logManager = java.util.logging.LogManager.getLogManager();
//        logManager.readConfiguration(new FileInputStream(properties));
//
//        final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
//
//        Handler[] handlers = logger.getHandlers();
//        assertThat(handlers.length, is(1));
//        assertThat(handlers[0], is(instanceOf(SocketHandler.class)));
//        SocketHandler socketHandler = (SocketHandler) handlers[0];
//
//        assertThat(socketHandler.isStackTraceModuleEnabled(), is(false));
//        assertThat(socketHandler.getStackTraceModuleBroadcastInterval(), is("9 seconds"));
//
//    }

}
