package com.logginghub.logging.filters;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.logging.Logger;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

//@Ignore // Benchmarks
public class MultipleEventContainsFilterTest
{

    private FilterFactory factory = new FilterFactory(false, false);

    @Test public void test()
    {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false, factory);
        filter.setEventContainsString("+one +two -three");

        DefaultLogEvent eventA = new DefaultLogEvent();
        eventA.setMessage("one two three");
        assertThat(filter.passes(eventA), is(false));

        eventA.setMessage("one two four");
        assertThat(filter.passes(eventA), is(true));

        eventA.setSourceApplication("one");
        eventA.setSourceClassName("two");
        eventA.setMessage("three");
        assertThat(filter.passes(eventA), is(false));

        eventA.setMessage("four");
        assertThat(filter.passes(eventA), is(true));
    }

    @Test public void test_commas_for_or() {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false, factory);
        filter.setEventContainsString("one,two");

        DefaultLogEvent eventA = LogEventBuilder.create(0, Logger.info, "one");
        DefaultLogEvent eventB = LogEventBuilder.create(0, Logger.info, "two");
        DefaultLogEvent eventC = LogEventBuilder.create(0, Logger.info, "three");

        assertThat(filter.passes(eventA), is(true));
        assertThat(filter.passes(eventB), is(true));
        assertThat(filter.passes(eventC), is(false));
    }


}
