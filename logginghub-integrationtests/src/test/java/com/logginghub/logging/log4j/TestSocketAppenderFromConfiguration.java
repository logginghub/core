package com.logginghub.logging.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.log4j.SocketAppender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.sof.SerialisableObject;

@RunWith(CustomRunner.class) public class TestSocketAppenderFromConfiguration {

    private File properties;
    private SocketAppender appender;

    private HubTestFixture fixture = new HubTestFixture();

    @Before public void setup() {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        File folder = FileUtils.createRandomTestFolderForClass(TestSocketAppenderFromConfiguration.class);
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

    @After public void stop() throws IOException {
        LogManager.shutdown();
        fixture.stop();
    }

    // @Test public void test_capture_thread_details() throws InterruptedException {
    //
    // SimpleServer server = new SimpleServer();
    // server.setListeningPort(NetUtils.findFreePort());
    // server.start();
    // server.waitUntilBound();
    //
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
    // writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
    // writer.appendLine("log4j.appender.loggingHub.host=localhost:{}", server.getListeningPort());
    // writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
    // writer.close();
    //
    // PropertyConfigurator.configure(properties.getAbsolutePath());
    //
    // final Logger logger = Logger.getLogger("test");
    //
    // WorkerThread.execute("TestThread", new Runnable() {
    // public void run() {
    // logger.info("Test message");
    // }
    // });
    //
    // server.getBucket().waitForMessages(1);
    // LogEvent logEvent = server.getBucket().get(0);
    //
    // assertThat(logEvent.getThreadName(), is("TestThread"));
    // assertThat(logEvent.getMessage(), is("Test message"));
    // assertThat(logEvent.getSourceApplication(), is("Test application"));
    // assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
    // assertThat(logEvent.getLoggerName(), is("test"));
    // assertThat(logEvent.getSourceClassName(),
    // is(startsWith("com.logginghub.logging.log4j.TestSocketAppender$")));
    // assertThat(logEvent.getSourceMethodName(), is("run"));
    // assertThat(logEvent.getChannel(), is(nullValue()));
    //
    // server.shutdown();
    // }
    //
    // @Test public void test_channel() throws InterruptedException {
    //
    // SimpleServer server = new SimpleServer();
    // server.setListeningPort(NetUtils.findFreePort());
    // server.start();
    // server.waitUntilBound();
    //
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
    // writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
    // writer.appendLine("log4j.appender.loggingHub.host=localhost:{}", server.getListeningPort());
    // writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
    // writer.appendLine("log4j.appender.loggingHub.channel=testChannel");
    // writer.close();
    //
    // PropertyConfigurator.configure(properties.getAbsolutePath());
    //
    // final Logger logger = Logger.getLogger("test");
    //
    // WorkerThread.execute("TestThread", new Runnable() {
    // public void run() {
    // logger.info("Test message");
    // }
    // });
    //
    // server.getBucket().waitForMessages(1);
    // LogEvent logEvent = server.getBucket().get(0);
    //
    // assertThat(logEvent.getThreadName(), is("TestThread"));
    // assertThat(logEvent.getMessage(), is("Test message"));
    // assertThat(logEvent.getSourceApplication(), is("Test application"));
    // assertThat(logEvent.getLevel(), is(com.logginghub.utils.logging.Logger.info));
    // assertThat(logEvent.getLoggerName(), is("test"));
    // assertThat(logEvent.getSourceClassName(),
    // is(startsWith("com.logginghub.logging.log4j.TestSocketAppender$")));
    // assertThat(logEvent.getSourceMethodName(), is("run"));
    // assertThat(logEvent.getChannel(), is("testChannel"));
    //
    // server.shutdown();
    // }

    @Test public void test_stack_traces_on() throws InterruptedException, ConnectorException {

        SocketHub hub = fixture.getSocketHubA();

        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        Bucket<ChannelMessage> bucket = fixture.getChannelBucketFor(Channels.stackSnapshots, client);

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
        writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.loggingHub.host=localhost:{}", hub.getPort());
        writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");

        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleEnabled=true");
        writer.appendLine("log4j.appender.loggingHub.stackTraceModuleBroadcastInterval=10 ms");
        writer.close();

        PropertyConfigurator.configure(properties.getAbsolutePath());

        Appender appender = Logger.getRootLogger().getAppender("loggingHub");
        assertThat(appender, is(instanceOf(SocketAppender.class)));
        SocketAppender socketAppender = (SocketAppender) appender;

        assertThat(socketAppender.isStackTraceModuleEnabled(), is(true));
        assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("10 ms"));

        bucket.waitForMessages(1);
        assertThat(bucket.get(0).getPayload(), is(instanceOf(StackSnapshot.class)));
    }

    // @Test public void test_stack_traces_off() throws InterruptedException {
    //
    // FileUtilsWriter writer = FileUtils.createWriter(properties);
    //
    // System.setProperty("app", "Test application");
    // writer.appendLine("log4j.rootLogger=DEBUG, loggingHub");
    // writer.appendLine("log4j.appender.loggingHub=com.logginghub.logging.log4j.SocketAppender");
    // writer.appendLine("log4j.appender.loggingHub.host=localhost:58770");
    // writer.appendLine("log4j.appender.loggingHub.sourceApplication=${app}");
    //
    // writer.appendLine("log4j.appender.loggingHub.stackTraceModuleEnabled=false");
    // writer.appendLine("log4j.appender.loggingHub.stackTraceModuleBroadcastInterval=9 seconds");
    // writer.close();
    //
    // PropertyConfigurator.configure(properties.getAbsolutePath());
    //
    // Appender appender = Logger.getRootLogger().getAppender("loggingHub");
    // assertThat(appender, is(instanceOf(SocketAppender.class)));
    // SocketAppender socketAppender = (SocketAppender)appender;
    //
    // assertThat(socketAppender.isStackTraceModuleEnabled(), is(false));
    // assertThat(socketAppender.getStackTraceModuleBroadcastInterval(), is("9 seconds"));
    // }
    //

    @Test public void testProcessTelemetry() throws IOException, InterruptedException, ConnectorException {

        SocketHub hub = fixture.getSocketHubA();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=TRACE, console, socket");
        writer.appendLine("log4j.appender.console=org.apache.log4j.ConsoleAppender");
        writer.appendLine("log4j.appender.console.layout=org.apache.log4j.PatternLayout");
        writer.appendLine("log4j.appender.console.layout.ConversionPattern=%d %p [%c{1}] - %m%n");
        writer.appendLine("log4j.appender.socket=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.socket.host=localhost:{}", hub.getPort());
        // writer.appendLine("log4j.appender.socket.telemetry=localhost:{}",
        // hub.getTelemetryHub().getPort());
        writer.appendLine("log4j.appender.socket.sourceApplication=${app}");
        writer.appendLine("log4j.appender.socket.publishProcessTelemetry=true");
        writer.close();

        PropertyConfigurator.configure(properties.toURI().toURL());

        final Bucket<ChannelMessage> messageBucket = new Bucket<ChannelMessage>();
        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        client.subscribe(Channels.telemetryUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                messageBucket.add(t);
            }
        });

        Logger.getLogger("test").info("informational message");

        messageBucket.waitForMessages(1);

        ChannelMessage message = messageBucket.get(0);
        SerialisableObject payload = message.getPayload();
        assertThat(payload, is(instanceOf(DataStructure.class)));
        DataStructure dataStructure = (DataStructure) payload;
        assertThat(dataStructure.containsValue(Values.SIGAR_OS_Cpu_User_Time), is(false));
        assertThat(dataStructure.containsValue(Values.JVM_Process_Memory_Maximum), is(true));

        hub.stop();

    }

    @Test public void testMachineTelemetry() throws IOException, InterruptedException, ConnectorException {
        SocketHub hub = fixture.getSocketHubA();

        FileUtilsWriter writer = FileUtils.createWriter(properties);

        System.setProperty("app", "Test application");
        writer.appendLine("log4j.rootLogger=TRACE, console, socket");
        writer.appendLine("log4j.appender.console=org.apache.log4j.ConsoleAppender");
        writer.appendLine("log4j.appender.console.layout=org.apache.log4j.PatternLayout");
        writer.appendLine("log4j.appender.console.layout.ConversionPattern=%d %p [%c{1}] - %m%n");
        writer.appendLine("log4j.appender.socket=com.logginghub.logging.log4j.SocketAppender");
        writer.appendLine("log4j.appender.socket.host=localhost:{}", hub.getPort());
        // writer.appendLine("log4j.appender.socket.telemetry=localhost:{}",
        // hub.getTelemetryHub().getPort());
        writer.appendLine("log4j.appender.socket.sourceApplication=${app}");
        writer.appendLine("log4j.appender.socket.publishMachineTelemetry=true");
        writer.close();

        PropertyConfigurator.configure(properties.toURI().toURL());

        final Bucket<ChannelMessage> messageBucket = new Bucket<ChannelMessage>();
        SocketClient client = fixture.createClientAutoSubscribe("client", hub);
        client.subscribe(Channels.telemetryUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                messageBucket.add(t);
            }
        });

        Logger.getLogger("test").info("informational message");

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {

                boolean ok;

                if (messageBucket.size() > 0) {
                    ChannelMessage message = messageBucket.popFirst();
                    SerialisableObject payload = message.getPayload();

                    assertThat(payload, is(instanceOf(DataStructure.class)));
                    DataStructure data = (DataStructure) payload;

                    ok = true;
                    ok &= data.containsValue(Values.SIGAR_OS_Cpu_User_Time);
                    ok &= !data.containsValue(Values.JVM_Process_Memory_Maximum);
                }
                else {
                    ok = false;
                }

                return ok;
            }
        });

        hub.stop();

    }

}
