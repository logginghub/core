package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.modules.InMemoryHistoryModule;
import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ProxyServiceDiscovery;

public class TestInMemoryHistoryModule {

    private InMemoryHistoryConfiguration configuration = new InMemoryHistoryConfiguration();
    private InMemoryHistoryModule history = new InMemoryHistoryModule();

    @Test public void test_bad_configuration() throws Exception {

        configuration.setBlockSize("-1");

        try {
            history.configure(configuration, new ProxyServiceDiscovery());
            fail();
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("The blockSize attribute of the inMemoryHistory configuration must be greater than zero - value is currently '-1'"));
        }

        configuration.setBlockSize("10K");
        configuration.setMaximumSize("8K");

        try {
            history.configure(configuration, new ProxyServiceDiscovery());
            fail();
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("The maximumSize attribute must be larger than the blockSize attribute - current values are '8K' and '10K' respectively"));
        }

        // This one just generates a warning now
        configuration.setBlockSize("10M");
        configuration.setMaximumSize("12M");
        history.configure(configuration, new ProxyServiceDiscovery());

        // This is more memory than we have
        configuration.setBlockSize("10M");
        configuration.setMaximumSize("8PB");

        try {
            history.configure(configuration, new ProxyServiceDiscovery());
            fail();
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is(startsWith("The maximumSize (8 PB) is too large compared to the maximum amount of memory available to the JVM (")));

            assertThat(e.getMessage(),
                       is(endsWith("with 50 MB reserved for GC headroom.) Please reduce the maximumSize or increase the JVM heap size using the -Xmx???m option.")));

        }

        // This is is too little max size
        configuration.setBlockSize("100K");
        configuration.setMaximumSize("499K");

        try {
            history.configure(configuration, new ProxyServiceDiscovery());
            fail();
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("The maximumSize attribute (499K) must be larger than 1 MB - otherwise we risk not being able to store multiple blocks containing larger events"));
        }

        // This is is too little block size
        configuration.setBlockSize("10K");
        configuration.setMaximumSize("499M");

        try {
            history.configure(configuration, new ProxyServiceDiscovery());
            fail();
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("The blockSize attribute (10K) must be larger than 100 KB - otherwise we risk not being able to store larger events"));
        }

    }

    @Test public void test_same_time() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(0, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(0, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(0, Logger.debug, "event5");

        history.handleNewEvent(event1);
        history.handleNewEvent(event2);
        history.handleNewEvent(event3);
        history.handleNewEvent(event4);
        history.handleNewEvent(event5);

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
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

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.handleNewEvent(event1);
        history.handleNewEvent(event2);
        history.handleNewEvent(event3);
        history.handleNewEvent(event4);
        history.handleNewEvent(event5);

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(event1));
        assertThat(events[1], is(event2));
        assertThat(events[2], is(event3));
        assertThat(events[3], is(event4));
        assertThat(events[4], is(event5));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
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

        history.configure(configuration, new ProxyServiceDiscovery());

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        history.handleNewEvent(event1);
        history.handleNewEvent(event2);
        history.handleNewEvent(event3);
        history.handleNewEvent(event4);
        history.handleNewEvent(event5);

        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(1000, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(3));
        assertThat(events[0], is(event3));
        assertThat(events[1], is(event4));
        assertThat(events[2], is(event5));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(1000, 10000));
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

    @Test public void test_states_empty() throws Exception {

        history.configure(configuration, new ProxyServiceDiscovery());

        // Run each method once when empty
        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(1000, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(0));

        final Bucket<LoggingMessage> batches = new Bucket<LoggingMessage>();
        QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                batches.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(new HistoricalDataRequest(1000, 10000), sender);
        // We get a response saying its the last batch of zero items
        assertThat(batches.size(), is(1));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(1000, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(0));

        // Send an event in
        history.handleNewEvent(LogEventFixture1.event1);

        // Repeat with single event
        dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
        events = dataResponse.getEvents();
        assertThat(events.length, is(1));

        batches.clear();
        sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                batches.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(new HistoricalDataRequest(0, 10000), sender);
        assertThat(batches.size(), is(1));

        indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
        elements = indexResponse.getElements();
        assertThat(elements.length, is(1));
    }

    @Test public void test_states_fill_first_block() throws Exception {
        configuration.setMaximumSize("1500B");
        configuration.setBlockSize("500B");
        configuration.setDisableSafetyChecks(true);
        configuration.setStreamingBatchSize(3);

        history.configure(configuration, new ProxyServiceDiscovery());

        // Send some events in to fill up the first block
        Iterator<DefaultLogEvent> iterator = LogEventFixture1.getEvents().iterator();
        while (history.getBlockSequence() == 0) {
            history.handleNewEvent(iterator.next());
        }

        // We've not done anything fancy to check the sizes of the actual events or blocks, so a lot
        // of the assertions that follow are just magic numbers that have been double checked

        // Check the methods
        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(5));
        assertThat(events[0], is(LogEventFixture1.event1));
        assertThat(events[1], is(LogEventFixture1.event2));
        assertThat(events[2], is(LogEventFixture1.event3));
        assertThat(events[3], is(LogEventFixture1.event4));
        assertThat(events[4], is(LogEventFixture1.event5));

        final Bucket<HistoricalDataResponse> batches = new Bucket<HistoricalDataResponse>();
        QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                batches.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(new HistoricalDataRequest(0, 10000), sender);
        assertThat(batches.size(), is(2));
        assertThat(batches.get(0).getEvents().length, is(3));
        assertThat(batches.get(0).getEvents()[0], is(LogEventFixture1.event1));
        assertThat(batches.get(0).getEvents()[1], is(LogEventFixture1.event2));
        assertThat(batches.get(0).getEvents()[2], is(LogEventFixture1.event3));
        assertThat(batches.get(1).getEvents().length, is(2));
        assertThat(batches.get(1).getEvents()[0], is(LogEventFixture1.event4));
        assertThat(batches.get(1).getEvents()[1], is(LogEventFixture1.event5));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(2));

        // TODO : check the index items?

    }

    @Test public void test_states_fill_all_blocks() throws Exception {
        // Nasty fixed sizes that only work on windows :/

        // TODO : rewrite to work on linux
        
        if (OSUtils.isWindows()) {
            configuration.setMaximumSize("800B");
            configuration.setBlockSize("500B");
            configuration.setDisableSafetyChecks(true);
            configuration.setStreamingBatchSize(3);

            history.configure(configuration, new ProxyServiceDiscovery());

            // Send some events in to fill up the first block
            Iterator<DefaultLogEvent> iterator = LogEventFixture1.getEvents().iterator();
            while (history.getBlockSequence() < 3) {
                history.handleNewEvent(iterator.next());
            }

            // We've not done anything fancy to check the sizes of the actual events or blocks, so a
            // lot
            // of the assertions that follow are just magic numbers that have been double checked

            // Check the methods
            HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(0, 10000));
            DefaultLogEvent[] events = dataResponse.getEvents();
            assertThat(events.length, is(9));
            assertThat(events[0], is(LogEventFixture1.event5));
            assertThat(events[1], is(LogEventFixture1.event6));
            assertThat(events[2], is(LogEventFixture1.event7));
            assertThat(events[3], is(LogEventFixture1.event8));
            assertThat(events[4], is(LogEventFixture1.event9));
            assertThat(events[5], is(LogEventFixture1.event10));
            assertThat(events[6], is(LogEventFixture1.event11));
            assertThat(events[7], is(LogEventFixture1.event12));
            assertThat(events[8], is(LogEventFixture1.event13));

            final Bucket<HistoricalDataResponse> batches = new Bucket<HistoricalDataResponse>();
            QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
                @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                    batches.add((HistoricalDataResponse) message);
                }

                @Override public boolean isSendQueueEmpty() {
                    return true;
                }
            };

            history.handleDataRequestStreaming(new HistoricalDataRequest(0, 10000), sender);
            assertThat(batches.size(), is(3));
            assertThat(batches.get(0).getEvents().length, is(3));
            assertThat(batches.get(0).getEvents()[0], is(LogEventFixture1.event5));
            assertThat(batches.get(0).getEvents()[1], is(LogEventFixture1.event6));
            assertThat(batches.get(0).getEvents()[2], is(LogEventFixture1.event7));
            assertThat(batches.get(1).getEvents().length, is(3));
            assertThat(batches.get(1).getEvents()[0], is(LogEventFixture1.event8));
            assertThat(batches.get(1).getEvents()[1], is(LogEventFixture1.event9));
            assertThat(batches.get(1).getEvents()[2], is(LogEventFixture1.event10));
            assertThat(batches.get(2).getEvents().length, is(3));
            assertThat(batches.get(2).getEvents()[0], is(LogEventFixture1.event11));
            assertThat(batches.get(2).getEvents()[1], is(LogEventFixture1.event12));
            assertThat(batches.get(2).getEvents()[2], is(LogEventFixture1.event13));

            HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(0, 10000));
            HistoricalIndexElement[] elements = indexResponse.getElements();
            assertThat(elements.length, is(3));

            // TODO : check the index items?
        }

    }

    @Test public void test_event_fills_up_block() throws Exception {
        configuration.setMaximumSize("800B");
        configuration.setBlockSize("5B");
        configuration.setDisableSafetyChecks(true);

        history.configure(configuration, new ProxyServiceDiscovery());

        history.handleNewEvent(LogEventFixture1.event1);

        // Make sure it was ignored
        HistoricalDataResponse dataResponse = history.handleDataRequest(new HistoricalDataRequest(1000, 10000));
        DefaultLogEvent[] events = dataResponse.getEvents();
        assertThat(events.length, is(0));

        final Bucket<LoggingMessage> batches = new Bucket<LoggingMessage>();
        QueueAwareLoggingMessageSender sender = new QueueAwareLoggingMessageSender() {
            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                batches.add((HistoricalDataResponse) message);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        history.handleDataRequestStreaming(new HistoricalDataRequest(1000, 10000), sender);
        assertThat(batches.size(), is(1));

        HistoricalIndexResponse indexResponse = history.handleIndexRequest(new HistoricalIndexRequest(1000, 10000));
        HistoricalIndexElement[] elements = indexResponse.getElements();
        assertThat(elements.length, is(0));

    }

    @Test public void test_minus_one_error() {

    }

}
