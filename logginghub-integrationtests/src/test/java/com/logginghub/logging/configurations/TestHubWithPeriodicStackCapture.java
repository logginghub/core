package com.logginghub.logging.configurations;

import com.logginghub.logging.AppenderHelper;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.handlers.SocketHandler;
import com.logginghub.logging.internallogging.LoggingHubStream;
import com.logginghub.logging.launchers.RunHub;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.modules.ChannelSubscriptionsModule;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Timeout;
import org.testng.annotations.Test;

import java.io.File;
import java.net.InetSocketAddress;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.startsWith;

/**
 * Created by james on 28/01/15.
 */
public class TestHubWithPeriodicStackCapture {

    @Test(invocationCount = 1, threadPoolSize = 1)
    public void test() throws LoggingMessageSenderException {

        File file = FileUtils.createRandomTestFileForClass(TestHubWithPeriodicStackCapture.class);

        File outputLogFolder = FileUtils.createRandomTestFolderForClass(TestHubWithPeriodicStackCapture.class);

        FileUtils.appendLine(file, "<container>");
        FileUtils.appendLine(file, "<socketHub port = '-1' />");
        FileUtils.appendLine(file, "<channelSubscriptions />");
        FileUtils.appendLine(file, "<stackCapture snapshotInterval='0' snapshotRequestInterval='1 second' outputToLog='true' channel = 'stack' environment='local' respondToRequests='false'/>");
        FileUtils.appendLine(file, "<timestampVariableRollingFileLogger folder='{}' channels='stack' filename='stack' />", outputLogFolder.getAbsolutePath());
        FileUtils.appendLine(file, "</container>");

        RunHub hub = RunHub.fromConfiguration(file.getAbsolutePath());

        hub.getFirst(SocketHub.class).waitUntilBound();
        int port = hub.getFirst(SocketHub.class).getPort();

        final Bucket<StackSnapshot> snapshots = new Bucket<StackSnapshot>();
        ChannelSubscriptionsModule channelSubscriptionsModule = hub.getFirst(ChannelSubscriptionsModule.class);
        channelSubscriptionsModule.subscribe(Channels.stackSnapshots, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage channelMessage) {
                StackSnapshot stackSnapshot = (StackSnapshot) channelMessage.getPayload();
                snapshots.add(stackSnapshot);
            }
        });

        // Setup a consumer
        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress("localhost", port));
        LogEventBucket eventBucket = new LogEventBucket();
        client.addLogEventListener(eventBucket);
        client.subscribe();

        // Setup a producer
        LoggingHubStream stream = new LoggingHubStream();
        stream.setSourceApplication("Test application");
        stream.setEnvironment("Test environment");
        stream.setInstanceNumber(666);
        stream.getAppenderHelper().setStackTraceModuleEnabled(true);
        stream.setHost("localhost:" + port);
        stream.start();

        // Make sure we get a snapshot response
        snapshots.waitForMessages(1);

        StackSnapshot stackSnapshot = snapshots.get(0);
        assertThat(stackSnapshot.getEnvironment(), is("Test environment"));
        assertThat(stackSnapshot.getHost(), is(NetUtils.getLocalHostname()));
        assertThat(stackSnapshot.getInstanceNumber(), is(666));

        // Make sure we got a log event for the stack trace
        eventBucket.waitForMessages(1);
        assertThat(eventBucket.get(0).getMessage().startsWith("Stack trace snapshot"), is(true));

        stream.stop();
        hub.stop();

        // Check the log was written
        assertThat(outputLogFolder.listFiles().length, is(1));
        assertThat(FileUtils.read(outputLogFolder.listFiles()[0]).contains("Stack trace snapshot"), is(true));

    }




}
