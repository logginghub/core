package com.logginghub.logging.modules;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.logginghub.integrationtests.logging.HubTestFixture;
import com.logginghub.integrationtests.logging.HubTestFixture.HubFixture;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.modules.configuration.BadEventsReportConfiguration;
import com.logginghub.logging.modules.configuration.BinaryProcessorConfiguration;
import com.logginghub.logging.modules.configuration.EventCountingProcessorConfiguration;
import com.logginghub.logging.modules.configuration.RegexExtractingProcessorConfiguration;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.logging.Logger;

// TODO : fix the AWT windows that are causing AWT to keep running afterwards
//@RunWith(CustomRunner.class) 
public class TestBinaryProcessor extends BaseHub {

    @Test public void test_binary_processor()throws IOException, ConnectorException, LoggingMessageSenderException {

        HubFixture hubFixture = fixture.createSocketHub(EnumSet.of(HubTestFixture.Features.BinaryWriter, HubTestFixture.Features.BinaryProcessor));
        
        File folder = FileUtils.createRandomTestFolderForClass(getClass());
        final File outputFolder = FileUtils.createRandomTestFolderForClass(getClass());
        
        hubFixture.getBinaryWriterConfiguration().setFolder(folder.getAbsolutePath());
        BinaryProcessorConfiguration binaryProcessorConfiguration = hubFixture.getBinaryProcessorConfiguration();
        
        binaryProcessorConfiguration.setInputPath(folder.getAbsolutePath());
        binaryProcessorConfiguration.setOutputPath(outputFolder.getAbsolutePath());
        binaryProcessorConfiguration.getBadEventReportsConfiguration().add(new BadEventsReportConfiguration());
        binaryProcessorConfiguration.getEventCounterProcessorsConfiguration().add(new EventCountingProcessorConfiguration());
        binaryProcessorConfiguration.getRegexProcessorsConfiguration().add(new RegexExtractingProcessorConfiguration("operationA", "OperationA completed successfully in {time} ms : user data was '[data]'", 1000, false, false));
        
        SocketHub hub = hubFixture.start();
        
        SocketClient clientA = fixture.createClient("clientA", hub);
        SocketClient clientB = fixture.createClientAutoSubscribe("clientB", hub);
        Bucket<LogEvent> eventBucket = fixture.createEventBucketFor(clientB);
        
        clientA.send(new LogEventMessage(LogEventBuilder.create(0, Logger.info, "OperationA completed successfully in 12.32 ms : user data was 'cat'")));

        eventBucket.waitForMessages(1);
        
        ThreadUtils.sleep(500);
        hubFixture.getBinaryWriterModule().stop();

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return outputFolder.listFiles().length == 1;
            }
        });
        
        

        
//        // Close down the writer
//        hubFixture.stop();
//        
//        assertThat(folder.listFiles().length, is(1));
//        
//        Bucket<LogEvent> restoredEvents = new Bucket<LogEvent>();
//        BinaryImporterModule.importFileBlocking(folder.listFiles()[0], restoredEvents);
//        
//        assertThat(restoredEvents.size(), is(1));
//        assertThat(restoredEvents.get(0).getMessage(), is("Test message"));
        
    }
}