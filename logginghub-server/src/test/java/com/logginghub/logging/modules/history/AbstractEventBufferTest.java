package com.logginghub.logging.modules.history;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.logging.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public abstract class AbstractEventBufferTest {

    private EventBuffer buffer = createBuffer();
    protected abstract EventBuffer createBuffer();
    
    @Test public void test_same_time() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(0, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(0, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(0, Logger.debug, "event5");

        buffer.addEvent(event1);
        buffer.addEvent(event2);
        buffer.addEvent(event3);
        buffer.addEvent(event4);
        buffer.addEvent(event5);

        List<LogEvent> events = new ArrayList<LogEvent>();
        buffer.extractEventsBetween(events, 0, 10000);
        assertThat(events.size(), is(5));
        assertThat(events.get(0), is((LogEvent)event1));
        assertThat(events.get(1), is((LogEvent)event2));
        assertThat(events.get(2), is((LogEvent)event3));
        assertThat(events.get(3), is((LogEvent)event4));
        assertThat(events.get(4), is((LogEvent)event5));

        List<HistoricalIndexElement> index = new ArrayList<HistoricalIndexElement>();
        buffer.extractIndexBetween(index, 0, 100000);
        assertThat(index.size(), is(1));
        assertThat(index.get(0).getInfoCount(), is(2));
        assertThat(index.get(0).getInterval(), is(1000L));
        assertThat(index.get(0).getOtherCount(), is(1));
        assertThat(index.get(0).getSevereCount(), is(1));
        assertThat(index.get(0).getWarningCount(), is(1));
        assertThat(index.get(0).getTime(), is(0L));

    }

    @Test public void test_index_time_range() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        buffer.addEvent(event1);
        buffer.addEvent(event2);
        buffer.addEvent(event3);
        buffer.addEvent(event4);
        buffer.addEvent(event5);

        List<LogEvent> events = new ArrayList<LogEvent>();
        buffer.extractEventsBetween(events, 0, 10000);
        assertThat(events.size(), is(5));
        assertThat(events.get(0), is((LogEvent)event1));
        assertThat(events.get(1), is((LogEvent)event2));
        assertThat(events.get(2), is((LogEvent)event3));
        assertThat(events.get(3), is((LogEvent)event4));
        assertThat(events.get(4), is((LogEvent)event5));

        List<HistoricalIndexElement> index = new ArrayList<HistoricalIndexElement>();
        buffer.extractIndexBetween(index, 0, 100000);
        assertThat(index.size(), is(3));

        assertThat(index.get(0).getTime(), is(0L));
        assertThat(index.get(0).getInterval(), is(1000L));
        assertThat(index.get(0).getOtherCount(), is(0));
        assertThat(index.get(0).getInfoCount(), is(1));
        assertThat(index.get(0).getSevereCount(), is(0));
        assertThat(index.get(0).getWarningCount(), is(1));

        assertThat(index.get(1).getTime(), is(1000L));
        assertThat(index.get(1).getInterval(), is(1000L));
        assertThat(index.get(1).getOtherCount(), is(0));
        assertThat(index.get(1).getInfoCount(), is(1));
        assertThat(index.get(1).getSevereCount(), is(1));
        assertThat(index.get(1).getWarningCount(), is(0));

        assertThat(index.get(2).getTime(), is(2000L));
        assertThat(index.get(2).getInterval(), is(1000L));
        assertThat(index.get(2).getOtherCount(), is(1));
        assertThat(index.get(2).getInfoCount(), is(0));
        assertThat(index.get(2).getSevereCount(), is(0));
        assertThat(index.get(2).getWarningCount(), is(0));

    }

    @Test public void test_search_sub_time_range() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        buffer.addEvent(event1);
        buffer.addEvent(event2);
        buffer.addEvent(event3);
        buffer.addEvent(event4);
        buffer.addEvent(event5);

        List<LogEvent> events = new ArrayList<LogEvent>();
        buffer.extractEventsBetween(events, 1000, 10000);
        assertThat(events.size(), is(3));
        assertThat(events.get(0), is((LogEvent)event3));
        assertThat(events.get(1), is((LogEvent)event4));
        assertThat(events.get(2), is((LogEvent)event5));

        List<HistoricalIndexElement> index = new ArrayList<HistoricalIndexElement>();
        buffer.extractIndexBetween(index, 1000, 100000);
        assertThat(index.size(), is(2));

        assertThat(index.get(0).getTime(), is(1000L));
        assertThat(index.get(0).getInterval(), is(1000L));
        assertThat(index.get(0).getOtherCount(), is(0));
        assertThat(index.get(0).getInfoCount(), is(1));
        assertThat(index.get(0).getSevereCount(), is(1));
        assertThat(index.get(0).getWarningCount(), is(0));

        assertThat(index.get(1).getTime(), is(2000L));
        assertThat(index.get(1).getInterval(), is(1000L));
        assertThat(index.get(1).getOtherCount(), is(1));
        assertThat(index.get(1).getInfoCount(), is(0));
        assertThat(index.get(1).getSevereCount(), is(0));
        assertThat(index.get(1).getWarningCount(), is(0));

    }

    @Test public void test_clear() throws Exception {

        DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
        DefaultLogEvent event2 = LogEventBuilder.create(500, Logger.warning, "event2");
        DefaultLogEvent event3 = LogEventBuilder.create(1000, Logger.severe, "event3");
        DefaultLogEvent event4 = LogEventBuilder.create(1999, Logger.info, "event4");
        DefaultLogEvent event5 = LogEventBuilder.create(2000, Logger.debug, "event5");

        buffer.addEvent(event1);
        buffer.addEvent(event2);
        buffer.addEvent(event3);
        buffer.addEvent(event4);
        buffer.addEvent(event5);

        List<LogEvent> events = new ArrayList<LogEvent>();
        buffer.extractEventsBetween(events, 1000, 10000);
        assertThat(events.size(), is(3));
        assertThat(events.get(0), is((LogEvent)event3));
        assertThat(events.get(1), is((LogEvent)event4));
        assertThat(events.get(2), is((LogEvent)event5));

        // when: I clear the events from the buffer
        buffer.clear();

        // and: I request the same event query again
        events.clear();
        buffer.extractEventsBetween(events, 1000, 10000);

        // then: the event list is empty
        assertThat(events.size(), is(0));
    }

}
