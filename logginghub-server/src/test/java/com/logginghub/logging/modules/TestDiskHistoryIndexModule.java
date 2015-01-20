package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.modules.DiskHistoryIndexModule;
import com.logginghub.logging.modules.configuration.DiskHistoryIndexConfiguration;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ProxyServiceDiscovery;

// TODO : post wedding ignore, needs fixing
@Ignore
public class TestDiskHistoryIndexModule {

    private DiskHistoryIndexConfiguration configuration = new DiskHistoryIndexConfiguration();
    private DiskHistoryIndexModule indexModule = new DiskHistoryIndexModule();
    private File folder;
    private FixedTimeProvider fixedTimeProvider= new FixedTimeProvider(0);

    @Before public void setup() {
        folder = FileUtils.createRandomTestFolderForClass(TestDiskHistoryIndexModule.class);
        configuration.setFolder(folder.getAbsolutePath());
        indexModule.setTimeProvider(fixedTimeProvider);
    }
    
    @Test public void test_add_one_event() throws Exception {
        
        indexModule.configure(configuration, new ProxyServiceDiscovery());
        
        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        
        // Add item
        indexModule.send(event1);
        
        // Force the publish
        indexModule.publishIndex();
        indexModule.flush();
        
        // Get the index back
        HistoricalIndexRequest message = new HistoricalIndexRequest(0, 10);
        HistoricalIndexResponse indexResponse = indexModule.handleIndexRequest(message);
        assertThat(indexResponse.getElements().length, is(1));
        assertThat(indexResponse.getElements()[0].getInfoCount(), is(1));
        assertThat(indexResponse.getElements()[0].getInterval(), is(1000L));
        assertThat(indexResponse.getElements()[0].getOtherCount(), is(0));
        assertThat(indexResponse.getElements()[0].getSevereCount(), is(0));
        assertThat(indexResponse.getElements()[0].getTime(), is(0L));
        assertThat(indexResponse.getElements()[0].getTotalCount(), is(1));
        assertThat(indexResponse.getElements()[0].getWarningCount(), is(0));
    }
    
    // TODO : why is all this stuff commented?
    
//    @Test public void test_index_created() throws Exception {}
//    @Test public void test_file_wraps() throws Exception {}
//    @Test public void test_two_files() throws Exception {}
//    @Test public void test_index_notifications() throws Exception {}    
//    
//    
    @Test public void test_same_time() throws Exception {

        indexModule.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(0, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(0, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(0, Logger.debug, "event5");

        indexModule.send(event1);
        indexModule.send(event2);
        indexModule.send(event3);
        indexModule.send(event4);
        indexModule.send(event5);
        
        // Force the publish
        indexModule.publishIndex();
        indexModule.flush();

        HistoricalIndexResponse indexResponse = indexModule.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(1));
        assertThat(elements[0].getInfoCount(), is(2));
        assertThat(elements[0].getInterval(), is(1000L));
        assertThat(elements[0].getOtherCount(), is(1));
        assertThat(elements[0].getSevereCount(), is(1));
        assertThat(elements[0].getWarningCount(), is(1));
        assertThat(elements[0].getTime(), is(0L));

    }

    @Test public void test_index_time_range() throws Exception {

        indexModule.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        indexModule.send(event1);
        indexModule.send(event2);
        fixedTimeProvider.setTime(1000);
        indexModule.publishIndex();
        
        indexModule.send(event3);
        indexModule.send(event4);
        fixedTimeProvider.setTime(2000);
        indexModule.publishIndex();
        
        indexModule.send(event5);
        fixedTimeProvider.setTime(3000);
        indexModule.publishIndex();
        indexModule.flush();

        HistoricalIndexResponse indexResponse = indexModule.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(3));

        assertThat(elements[0].getTime(), is(0L));
        assertThat(elements[0].getInterval(), is(1000L));
        assertThat(elements[0].getOtherCount(), is(0));
        assertThat(elements[0].getInfoCount(), is(1));
        assertThat(elements[0].getSevereCount(), is(0));
        assertThat(elements[0].getWarningCount(), is(1));

        assertThat(elements[1].getTime(), is(1000L));
        assertThat(elements[1].getInterval(), is(1000L));
        assertThat(elements[1].getOtherCount(), is(0));
        assertThat(elements[1].getInfoCount(), is(1));
        assertThat(elements[1].getSevereCount(), is(1));
        assertThat(elements[1].getWarningCount(), is(0));

        assertThat(elements[2].getTime(), is(2000L));
        assertThat(elements[2].getInterval(), is(1000L));
        assertThat(elements[2].getOtherCount(), is(1));
        assertThat(elements[2].getInfoCount(), is(0));
        assertThat(elements[2].getSevereCount(), is(0));
        assertThat(elements[2].getWarningCount(), is(0));

    }
    
    @Test public void test_index_without_flush() throws Exception {

        indexModule.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        indexModule.send(event1);
        indexModule.send(event2);
        fixedTimeProvider.setTime(1000);
        indexModule.publishIndex();
        
        indexModule.send(event3);
        indexModule.send(event4);
        fixedTimeProvider.setTime(2000);
        indexModule.publishIndex();
        
        indexModule.send(event5);
        fixedTimeProvider.setTime(3000);
        indexModule.publishIndex();

        HistoricalIndexResponse indexResponse = indexModule.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(3));

        assertThat(elements[0].getTime(), is(0L));
        assertThat(elements[0].getInterval(), is(1000L));
        assertThat(elements[0].getOtherCount(), is(0));
        assertThat(elements[0].getInfoCount(), is(1));
        assertThat(elements[0].getSevereCount(), is(0));
        assertThat(elements[0].getWarningCount(), is(1));

        assertThat(elements[1].getTime(), is(1000L));
        assertThat(elements[1].getInterval(), is(1000L));
        assertThat(elements[1].getOtherCount(), is(0));
        assertThat(elements[1].getInfoCount(), is(1));
        assertThat(elements[1].getSevereCount(), is(1));
        assertThat(elements[1].getWarningCount(), is(0));

        assertThat(elements[2].getTime(), is(2000L));
        assertThat(elements[2].getInterval(), is(1000L));
        assertThat(elements[2].getOtherCount(), is(1));
        assertThat(elements[2].getInfoCount(), is(0));
        assertThat(elements[2].getSevereCount(), is(0));
        assertThat(elements[2].getWarningCount(), is(0));

    }

    @Test public void test_search_sub_time_range() throws Exception {

        indexModule.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        indexModule.send(event1);
        indexModule.send(event2);
        fixedTimeProvider.setTime(1000);
        indexModule.publishIndex();
        
        indexModule.send(event3);
        indexModule.send(event4);
        fixedTimeProvider.setTime(2000);
        indexModule.publishIndex();
        
        indexModule.send(event5);
        fixedTimeProvider.setTime(3000);
        indexModule.publishIndex();
        indexModule.flush();

        HistoricalIndexResponse indexResponse = indexModule.handleIndexRequest(new HistoricalIndexRequest(1000, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(2));

        assertThat(elements[0].getTime(), is(1000L));
        assertThat(elements[0].getInterval(), is(1000L));
        assertThat(elements[0].getOtherCount(), is(0));
        assertThat(elements[0].getInfoCount(), is(1));
        assertThat(elements[0].getSevereCount(), is(1));
        assertThat(elements[0].getWarningCount(), is(0));

        assertThat(elements[1].getTime(), is(2000L));
        assertThat(elements[1].getInterval(), is(1000L));
        assertThat(elements[1].getOtherCount(), is(1));
        assertThat(elements[1].getInfoCount(), is(0));
        assertThat(elements[1].getSevereCount(), is(0));
        assertThat(elements[1].getWarningCount(), is(0));

    }

}
