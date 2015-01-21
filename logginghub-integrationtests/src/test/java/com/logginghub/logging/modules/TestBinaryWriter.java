package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.modules.BinaryImporterModule;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;

@RunWith(CustomRunner.class) 
public class TestBinaryWriter extends BaseHub {

    @Test public void test_binary_writer()throws IOException, ConnectorException, LoggingMessageSenderException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.BinaryWriter));
        
        File folder = FileUtils.createRandomTestFolderForClass(getClass());
        hubFixture.getBinaryWriterConfiguration().setFolder(folder.getAbsolutePath());
        
        SocketHub hub = hubFixture.start();
        
        SocketClient clientA = fixture.createClient("clientA", hub);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hub);
        Bucket<LogEvent> eventBucket = fixture.createEventBucketFor(clientB);
        
        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "Test message")));

        eventBucket.waitForMessages(1);
        
        // Close down the writer
        hubFixture.stop();
        
        assertThat(folder.listFiles().length, is(1));
        
        Bucket<LogEvent> restoredEvents = new Bucket<LogEvent>();
        BinaryImporterModule.importFileBlocking(folder.listFiles()[0], restoredEvents);
        
        assertThat(restoredEvents.size(), is(1));
        assertThat(restoredEvents.get(0).getMessage(), is("Test message"));
        
    }
}