package com.logginghub.logging.filters;

import com.logginghub.logging.DefaultLogEvent;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


@SuppressWarnings("Duplicates")
public class MultipleCaseSensitiveEventContainsFilterTest {
    private DefaultLogEvent eventA;
    private DefaultLogEvent eventB;
    private DefaultLogEvent eventC;
    private DefaultLogEvent eventD;
    private DefaultLogEvent eventE;

    private FilterFactory factory = new FilterFactory(false, false);

    @Before
    public void setup() {
        eventA = new DefaultLogEvent();
        eventB = new DefaultLogEvent();
        eventC = new DefaultLogEvent();
        eventD = new DefaultLogEvent();
        eventE = new DefaultLogEvent();

        eventA.setMessage("one two three");
        eventB.setMessage("one two four");

        eventC.setSourceApplication("one");
        eventC.setSourceClassName("two");
        eventC.setMessage("three");

        eventD.setMessage("four");
        eventE.setMessage("one four");
    }

    @Test
    public void test() {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false, factory);
        filter.setEventContainsString("+one +two -three");

        assertThat(filter.passes(eventA), is(false));
        assertThat(filter.passes(eventB), is(true));
        assertThat(filter.passes(eventC), is(false));
        assertThat(filter.passes(eventD), is(false));
    }

    @Test
    public void test_case_insensitive_by_default() {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false, factory);
        filter.setEventContainsString("+ONE +TWO -THREE");

        assertThat(filter.passes(eventA), is(false));
        assertThat(filter.passes(eventB), is(true));
        assertThat(filter.passes(eventC), is(false));
        assertThat(filter.passes(eventD), is(false));
    }

    @Test
    public void test_firstItemAutomaticPlus() {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false, factory);
        filter.setEventContainsString("one -three");

        assertThat(filter.passes(eventA), is(false));
        assertThat(filter.passes(eventB), is(true));
        assertThat(filter.passes(eventC), is(false));
        assertThat(filter.passes(eventD), is(false));
        assertThat(filter.passes(eventE), is(true));

    }
}
