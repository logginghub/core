package com.logginghub.logging.modules;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.modules.configuration.DiskHistoryConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.Out;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestDiskHistoryModule {

    private DiskHistoryConfiguration configuration = new DiskHistoryConfiguration();
    private DiskHistoryModule history = new DiskHistoryModule();
    private File folder;

    @Before public void setup() {
        folder = FileUtils.createRandomTestFolderForClass(TestDiskHistoryModule.class);
        configuration.setFolder(folder.getAbsolutePath());
    }

    @Test public void test_add_one_event() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");

        // Add item
        history.send(event1);
        history.flush();

        // Get it back
        HistoricalDataResponse response = history.handleDataRequest(new HistoricalDataRequest(0, 10));
        assertThat(response.getEvents().length, is(1));
        assertThat(response.getEvents()[0].getMessage(), is("event1"));

    }

    @Test public void test_same_time() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(0, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(0, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(0, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);

        history.flush();

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));
    }

    @Test public void test_time_range() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);
        history.flush();

        HistoricalDataRequest message = new HistoricalDataRequest(0, 10000);
        HistoricalDataResponse dataResponse = history.handleDataRequest(message);
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));

        final Bucket<HistoricalDataResponse> responses = new Bucket<HistoricalDataResponse>();
        QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                responses.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(message, sender);

        responses.waitForMessages(1);

        dataResponse = responses.get(0);
        events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));
    }

    @Test public void test_time_range_no_flush() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);

        HistoricalDataRequest message = new HistoricalDataRequest(0, 10000);
        HistoricalDataResponse dataResponse = history.handleDataRequest(message);
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));

        final Bucket<HistoricalDataResponse> responses = new Bucket<HistoricalDataResponse>();
        QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                responses.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(message, sender);

        responses.waitForMessages(1);

        dataResponse = responses.get(0);
        events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));
    }

    @Test public void test_time_based_flush() throws Exception {

        configuration.setMaximumFlushInterval("500 ms");
        history.configure(configuration, new ProxyServiceDiscovery());

        // jshaw - disable the latest data check to make sure this thing has been flushed on the timer
        history.setCheckLatest(false);

        history.start();

        final DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        history.send(event1);

        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                HistoricalDataRequest message = new HistoricalDataRequest(0, 10000);
                HistoricalDataResponse dataResponse = history.handleDataRequest(message);
                DefaultLogEvent[] events = dataResponse.getEvents();
                return events.length == 1 && events[0].equals(event1);
            }
        });

        history.stop();
    }

    @Test public void test_search_sub_time_range() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);
        history.flush();

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(1000, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(3));
        assertThat(events[0], is(event3));
        assertThat(events[1], is(event4));
        assertThat(events[2], is(event5));
    }

    @Test public void test_search_sub_time_range_no_flush() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(1000, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(3));
        assertThat(events[0], is(event3));
        assertThat(events[1], is(event4));
        assertThat(events[2], is(event5));
    }

    @Test public void test_stream_forwards() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.send(event1);
        history.send(event2);
        history.send(event3);
        history.send(event4);
        history.send(event5);

        HistoricalDataRequest historicalDataRequest = new HistoricalDataRequest(0, 10000);

        final Bucket<LoggingMessage> messages = new Bucket<LoggingMessage>();
        QueueAwareLoggingMessageSender source = new QueueAwareLoggingMessageSender() {
            @Override public boolean isSendQueueEmpty() {
                return true;
            }

            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                messages.add(message);
            }
        };

        history.handleDataRequestStreaming(historicalDataRequest, source);

        HistoricalDataResponse response = (HistoricalDataResponse) messages.get(0);

        DefaultLogEvent[] events = response.getEvents();

        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));

    }

    @Test public void test_stream_backwards() throws Exception {

        // Set a small batch size so we can see updates coming through easily
        configuration.setStreamingBatchSize(2);

        // Force a small block size to highlight the bug in the disk bit
        configuration.setBlockSize("1000 bytes");

        // Force a small file size to make sure we travel in the right order between the files
        configuration.setFileSizeLimit("2000 bytes");

        // Make sure we don't delete files
        configuration.setTotalFileSizeLimit("200 MB");

        history.configure(configuration, new ProxyServiceDiscovery());

        int eventCount = 1000;

        List<DefaultLogEvent> events = new ArrayList<DefaultLogEvent>();
        for (int i = 0; i < eventCount; i++) {
            DefaultLogEvent event = LogEventBuilder.create(i, Logger.info, "event-" + i);
            events.add(event);
        }

        for (DefaultLogEvent event : events) {
            history.send(event);
        }

        HistoricalDataRequest historicalDataRequest = new HistoricalDataRequest(0, 10000000);
        historicalDataRequest.setMostRecentFirst(true);

//        final MutableInt mi = new MutableInt(0);

        final Bucket<HistoricalDataResponse> messages = new Bucket<HistoricalDataResponse>();
        QueueAwareLoggingMessageSender source = new QueueAwareLoggingMessageSender() {
            @Override public boolean isSendQueueEmpty() {
                return true;
            }

            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                HistoricalDataResponse response = (HistoricalDataResponse) message;
                DefaultLogEvent[] events1 = response.getEvents();
                for (DefaultLogEvent defaultLogEvent : events1) {
//                    Out.out("{} : {}", mi, defaultLogEvent);
//                    mi.increment();
                } messages.add(response);
            }
        };

        history.handleDataRequestStreaming(historicalDataRequest, source);

        // Unload the events from the messages
        int count = 0;
        List<DefaultLogEvent> responseEvents = new ArrayList<DefaultLogEvent>();
        for (LoggingMessage message : messages) {
            HistoricalDataResponse response = (HistoricalDataResponse) message;
            DefaultLogEvent[] events1 = response.getEvents();
            for (DefaultLogEvent defaultLogEvent : events1) {
//                Out.out("{} : {}", count++, defaultLogEvent);
                responseEvents.add(defaultLogEvent);
            }
        }

        assertThat(responseEvents.size(), is(eventCount));
        for (int i = 0; i < responseEvents.size(); i++) {
            assertThat(responseEvents.get(i), is(events.get(eventCount - i - 1)));
        }

    }

}
