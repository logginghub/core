package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.HistoricalPatternisedDataRequest;
import com.logginghub.logging.messages.HistoricalPatternisedDataResponse;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.configuration.PatternisedDiskHistoryConfiguration;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stream;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestPatternisedDiskHistoryModule {

    @Test public void test() {

        File folder = FileUtils.createRandomTestFileForClass(getClass());

        PatternisedDiskHistoryConfiguration configuration = new PatternisedDiskHistoryConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        PatternisedDiskHistoryModule module = new PatternisedDiskHistoryModule();

        ChannelMessagingService mockChannelMessaging = Mockito.mock(ChannelMessagingService.class);
        SocketHubInterface mockSocketHubInterface = Mockito.mock(SocketHubInterface.class);

        Stream<PatternisedLogEvent> patternisedEventStream = new Stream<PatternisedLogEvent>();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();

        discovery.bind(SocketHubInterface.class, mockSocketHubInterface);
        discovery.bind(ChannelMessagingService.class, mockChannelMessaging);
        discovery.bind(Source.class, PatternisedLogEvent.class, patternisedEventStream);

        module.configure(configuration, discovery);

        module.start();

        PatternisedLogEvent event1 = new PatternisedLogEvent();
        event1.setChannel("channel");
        event1.setLevel(Logger.info);
        event1.setLoggerName("loggerName");
        event1.setPatternID(5);
        event1.setPid(1234);
        event1.setSequenceNumber(1234567L);
        event1.setSourceAddress("sourceAddress");
        event1.setSourceApplication("sourceApplication");
        event1.setSourceClassName("sourceClassName");
        event1.setSourceHost("sourceHost");
        event1.setSourceMethodName("sourceMethodName");
        event1.setThreadName("threadName");
        event1.setTime(12);
        event1.setVariables(new String[] { "variable0", "variable1", "variable2" });

        patternisedEventStream.send(event1);

        HistoricalPatternisedDataRequest request = new HistoricalPatternisedDataRequest(0, 100);
        HistoricalPatternisedDataResponse response = module.handleDataRequest(request);
        assertThat(response.getEvents().length, is(1));
        assertThat(response.getEvents()[0].getChannel(), is("channel"));
        assertThat(response.getEvents()[0].getLevel(), is(Logger.info));
        assertThat(response.getEvents()[0].getLoggerName(), is("loggerName"));
        assertThat(response.getEvents()[0].getPatternID(), is(5));
        assertThat(response.getEvents()[0].getPid(), is(1234));
        assertThat(response.getEvents()[0].getSequenceNumber(), is(1234567L));
        assertThat(response.getEvents()[0].getSourceAddress(), is("sourceAddress"));
        assertThat(response.getEvents()[0].getSourceApplication(), is("sourceApplication"));
        assertThat(response.getEvents()[0].getSourceClassName(), is("sourceClassName"));
        assertThat(response.getEvents()[0].getSourceHost(), is("sourceHost"));
        assertThat(response.getEvents()[0].getSourceMethodName(), is("sourceMethodName"));
        assertThat(response.getEvents()[0].getThreadName(), is("threadName"));
        assertThat(response.getEvents()[0].getTime(), is(12L));
        assertThat(response.getEvents()[0].getVariables(), is(new String[] { "variable0", "variable1", "variable2" }));

        module.stop();

    }

}
