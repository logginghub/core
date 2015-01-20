package com.logginghub.logging.frontend.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.model.LogEventContainer;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.filter.Filter;

public class TestLogEventContainer {

    private LogEventContainer container = new LogEventContainer();

    private LogEvent hundredByteEvent1;
    private LogEvent hundredByteEvent2;
    private LogEvent hundredByteEvent3;
    private LogEvent hundredByteEvent4;
    private LogEvent hundredByteEvent5;

    @Test public void test_vs_yourkit() {
        DefaultLogEvent event = new DefaultLogEvent();
        event.setThreadName("logEventPublisher hub 3-thread-0");
        event.setMessage("Message 2744724");
        event.setSourceAddress("127.0.1.1");
        event.setSourceApplication("Simulator");
        event.setLoggerName("logger");
        event.setSourceHost("server");
        event.setSourceClassName("?");
        event.setSourceMethodName("?");
        
        // This isn't quite right, but its a lot better than it was
//        assertThat(LogEventContainer.sizeof(event), is(536L));
        assertThat(LoggingUtils.sizeof(event), is(496L));
    }
    
    @Test public void test_sizeof() {
        assertThat(LoggingUtils.sizeof(new DefaultLogEvent()), is(64L));
        assertThat(LoggingUtils.sizeof(hundredByteEvent1), is(256L));
        assertThat(LoggingUtils.sizeof(hundredByteEvent2), is(256L));
        assertThat(LoggingUtils.sizeof(hundredByteEvent3), is(256L));
        assertThat(LoggingUtils.sizeof(hundredByteEvent4), is(256L));
        assertThat(LoggingUtils.sizeof(hundredByteEvent5), is(256L));
    }

    @Test public void test_empty() {
        assertThat(container.size(), is(0));
    }

    @Test public void test_adding_item() {
        container.add(hundredByteEvent1);
        assertThat(container.size(), is(1));
    }

    @Test public void test_adding_items_over_threshold() {
        container.setThreshold(768);
        assertThat(container.getThreshold(), is(768L));
        assertThat(container.getCurrentLevel(), is(0L));

        container.add(hundredByteEvent1);
        assertThat(container.size(), is(1));
        assertThat(container.getCurrentLevel(), is(256L));

        container.add(hundredByteEvent2);
        assertThat(container.size(), is(2));
        assertThat(container.getCurrentLevel(), is(512L));

        container.add(hundredByteEvent3);
        assertThat(container.size(), is(3));
        assertThat(container.getCurrentLevel(), is(768L));

        container.add(hundredByteEvent4);
        assertThat(container.size(), is(3));
        assertThat(container.getCurrentLevel(), is(768L));

        assertThat(container.get(0), is(hundredByteEvent2));
        assertThat(container.get(1), is(hundredByteEvent3));
        assertThat(container.get(2), is(hundredByteEvent4));

        container.add(hundredByteEvent5);
        assertThat(container.size(), is(3));
        assertThat(container.getCurrentLevel(), is(768L));

        assertThat(container.get(0), is(hundredByteEvent3));
        assertThat(container.get(1), is(hundredByteEvent4));
        assertThat(container.get(2), is(hundredByteEvent5));
    }

    @Test public void test_applying_filter() {
        container.add(hundredByteEvent1, hundredByteEvent2, hundredByteEvent3, hundredByteEvent4, hundredByteEvent5);
        assertThat(container.size(), is(5));

        LogEventContainer filteredIn = new LogEventContainer();
        LogEventContainer filteredOut = new LogEventContainer();

        container.applyFilter(new Filter<LogEvent>() {
            @Override public boolean passes(LogEvent event) {
                return event.getLevel() == 1;
            }
        }, filteredIn, filteredOut);

        assertThat(container.size(), is(5));
        assertThat(filteredIn.size(), is(3));
        assertThat(filteredOut.size(), is(2));

        assertThat(container.get(0), is(hundredByteEvent1));
        assertThat(container.get(1), is(hundredByteEvent2));
        assertThat(container.get(2), is(hundredByteEvent3));
        assertThat(container.get(3), is(hundredByteEvent4));
        assertThat(container.get(4), is(hundredByteEvent5));

        assertThat(filteredIn.get(0), is(hundredByteEvent1));
        assertThat(filteredIn.get(1), is(hundredByteEvent3));
        assertThat(filteredIn.get(2), is(hundredByteEvent5));

        assertThat(filteredOut.get(0), is(hundredByteEvent2));
        assertThat(filteredOut.get(1), is(hundredByteEvent4));
    }

    @Test public void test_adding_items_over_threshold_with_filter() {

    }

    @Before public void setup() {
        hundredByteEvent1 = new DefaultLogEvent();
        ((DefaultLogEvent) hundredByteEvent1).setMessage("ee1");
        ((DefaultLogEvent) hundredByteEvent1).setLoggerName("l");
        ((DefaultLogEvent) hundredByteEvent1).setSourceAddress("h");
        ((DefaultLogEvent) hundredByteEvent1).setSourceClassName("c");
        ((DefaultLogEvent) hundredByteEvent1).setSourceMethodName("?");        
        ((DefaultLogEvent) hundredByteEvent1).setLevel(1);

        hundredByteEvent2 = new DefaultLogEvent();
        ((DefaultLogEvent) hundredByteEvent2).setMessage("ee2");
        ((DefaultLogEvent) hundredByteEvent2).setLoggerName("l");
        ((DefaultLogEvent) hundredByteEvent2).setSourceAddress("h");
        ((DefaultLogEvent) hundredByteEvent2).setSourceClassName("c");
        ((DefaultLogEvent) hundredByteEvent2).setSourceMethodName("?");
        ((DefaultLogEvent) hundredByteEvent2).setLevel(2);

        hundredByteEvent3 = new DefaultLogEvent();
        ((DefaultLogEvent) hundredByteEvent3).setMessage("ee3");
        ((DefaultLogEvent) hundredByteEvent3).setLoggerName("l");
        ((DefaultLogEvent) hundredByteEvent3).setSourceAddress("h");
        ((DefaultLogEvent) hundredByteEvent3).setSourceClassName("c");
        ((DefaultLogEvent) hundredByteEvent3).setSourceMethodName("?");
        ((DefaultLogEvent) hundredByteEvent3).setLevel(1);

        hundredByteEvent4 = new DefaultLogEvent();
        ((DefaultLogEvent) hundredByteEvent4).setMessage("ee4");
        ((DefaultLogEvent) hundredByteEvent4).setLoggerName("l");
        ((DefaultLogEvent) hundredByteEvent4).setSourceAddress("h");
        ((DefaultLogEvent) hundredByteEvent4).setSourceClassName("c");
        ((DefaultLogEvent) hundredByteEvent4).setSourceMethodName("?");
        ((DefaultLogEvent) hundredByteEvent4).setLevel(2);

        hundredByteEvent5 = new DefaultLogEvent();
        ((DefaultLogEvent) hundredByteEvent5).setMessage("ee5");
        ((DefaultLogEvent) hundredByteEvent5).setLoggerName("l");
        ((DefaultLogEvent) hundredByteEvent5).setSourceAddress("h");
        ((DefaultLogEvent) hundredByteEvent5).setSourceClassName("c");
        ((DefaultLogEvent) hundredByteEvent5).setSourceMethodName("?");
        ((DefaultLogEvent) hundredByteEvent5).setLevel(1);
    }

}
