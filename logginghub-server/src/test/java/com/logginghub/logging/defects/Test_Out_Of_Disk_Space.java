package com.logginghub.logging.defects;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.hub.configuration.LegacySocketHubConfiguration;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.launchers.LegacyRunHub;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.modules.configuration.SocketTextReaderConfiguration;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;

@Ignore
public class Test_Out_Of_Disk_Space {

    /**
     * @throws Exception
     */
    @Test public void test_old_log_style() throws Exception {
        File folder = new File("target/temp/testHubConfiguration");
        FileUtils.deleteContents(folder);
        folder.mkdirs();

        File configurationFile = new File(folder, "/hub.xml");

        RollingFileLoggerConfiguration aggregatedFileLogConfiguration = new RollingFileLoggerConfiguration();
        aggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
        // F: is my 40 meg ram disk
        aggregatedFileLogConfiguration.setFilename("f:/log.txt");
        aggregatedFileLogConfiguration.setMaximumFileSize(1000000);
        aggregatedFileLogConfiguration.setNumberOfFiles(200);
        aggregatedFileLogConfiguration.setNumberOfCompressedFiles(200);
        aggregatedFileLogConfiguration.setOpenWithAppend(true);
        aggregatedFileLogConfiguration.setWriteAsynchronously(true);

        final int mainPort = NetUtils.findFreePort();
        int socketTextReaderPort = NetUtils.findFreePort();

//        SocketTextReaderConfiguration socketTestReaderConfiguration = new SocketTextReaderConfiguration();
//        socketTestReaderConfiguration.setPort(socketTextReaderPort);
//        socketTestReaderConfiguration.setMessageEnd("message end");
//        socketTestReaderConfiguration.setMessageStart("message start");
//        socketTestReaderConfiguration.setLevel(Level.WARNING.getName());

//        List<SocketTextReaderConfiguration> socketTextReaders = new ArrayList<SocketTextReaderConfiguration>();
//        socketTextReaders.add(socketTestReaderConfiguration);

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setAggregatedFileLogConfiguration(aggregatedFileLogConfiguration);
        configuration.setPort(mainPort);
//        configuration.setSocketTextReaders(socketTextReaders);

        configuration.writeToFile(configurationFile);

        final LegacyRunHub wrapper = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });

        WorkerThread wt = new WorkerThread("Connection retryer") {
            @Override protected void onRun() throws Throwable {
                SocketClient client = new SocketClient();
                client.addConnectionPoint(new InetSocketAddress("localhost", mainPort));
                try {
                    client.connect();
                    System.out.println("Connected");
                    client.disconnect();
                }
                catch (ConnectorException e) {
                    System.out.println("!!!!!!\r\n!!!!!!!\r\n Failed to connect");
                }
                
            }
        };
        
        wt.setIterationDelay(500);
        wt.startDaemon();

        String repeat = StringUtils.repeat("a", 1000);
        int messages = 1000000;
        for (int i = 0; i < messages; i++) {
            DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
            LogEventMessage message = new LogEventMessage(event1);
            event1.setMessage(repeat + i);
            wrapper.getHub().onNewMessage(message, Mockito.mock(SocketConnection.class));
        }
    }

    @Test public void test_new_log_style() throws Exception {
        File folder = new File("target/temp/testHubConfiguration");
        FileUtils.deleteContents(folder);
        folder.mkdirs();

        File configurationFile = new File(folder, "/hub.xml");

        TimestampVariableRollingFileLoggerConfiguration timestampAggregatedFileLogConfiguration = new TimestampVariableRollingFileLoggerConfiguration();
        timestampAggregatedFileLogConfiguration.setAsynchronousQueueWarningSize(5000);
        timestampAggregatedFileLogConfiguration.setExtension(".txt");
        timestampAggregatedFileLogConfiguration.setFilename("log");
        timestampAggregatedFileLogConfiguration.setFolder("f:/");
        timestampAggregatedFileLogConfiguration.setMaximumFileSize(10000000);
        timestampAggregatedFileLogConfiguration.setNumberOfCompressedFiles(2);
        timestampAggregatedFileLogConfiguration.setNumberOfFiles(2000);
        timestampAggregatedFileLogConfiguration.setOpenWithAppend(true);
        timestampAggregatedFileLogConfiguration.setWriteAsynchronously(true);

        final int mainPort = NetUtils.findFreePort();
        int socketTextReaderPort = NetUtils.findFreePort();

        SocketTextReaderConfiguration socketTestReaderConfiguration = new SocketTextReaderConfiguration();
        socketTestReaderConfiguration.setPort(socketTextReaderPort);
//        socketTestReaderConfiguration.setMessageEnd("message end");
//        socketTestReaderConfiguration.setMessageStart("message start");
//        socketTestReaderConfiguration.setLevel(Level.WARNING.getName());

        List<SocketTextReaderConfiguration> socketTextReaders = new ArrayList<SocketTextReaderConfiguration>();
        socketTextReaders.add(socketTestReaderConfiguration);

        LegacySocketHubConfiguration configuration = new LegacySocketHubConfiguration();
        configuration.setTimeStampAggregatedFileLogConfiguration(timestampAggregatedFileLogConfiguration);
        configuration.setPort(mainPort);
//        configuration.setSocketTextReaders(socketTextReaders);

        configuration.writeToFile(configurationFile);
        
        final LegacyRunHub wrapper = LegacyRunHub.mainInternal(new String[] { configurationFile.getAbsolutePath() });

        WorkerThread wt = new WorkerThread("Connection retryer") {
            @Override protected void onRun() throws Throwable {
                SocketClient client = new SocketClient();
                client.addConnectionPoint(new InetSocketAddress("localhost", mainPort));
                try {
                    client.connect();
                    System.out.println("Connected");
                    client.disconnect();
                }
                catch (ConnectorException e) {
                    System.out.println("!!!!!!\r\n!!!!!!!\r\n Failed to connect");
                }
                
            }
        };
        
        wt.setIterationDelay(500);
        wt.startDaemon();

        String repeat = StringUtils.repeat("a", 1000);
        int messages = 1000000;
        for (int i = 0; i < messages; i++) {
            DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
            LogEventMessage message = new LogEventMessage(event1);
            event1.setMessage(repeat + i);
            wrapper.getHub().onNewMessage(message, Mockito.mock(SocketConnection.class));
        }
    }
}
