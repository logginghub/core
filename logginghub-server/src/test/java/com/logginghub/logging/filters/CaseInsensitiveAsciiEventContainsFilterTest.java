package com.logginghub.logging.filters;

import com.logginghub.logging.DefaultLogEvent;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by james on 16/12/2015.
 */
public class CaseInsensitiveAsciiEventContainsFilterTest {

    private DefaultLogEvent eventA;
    private DefaultLogEvent eventB;

    @Before
    public void setup() {
        eventA = new DefaultLogEvent();
        eventB = new DefaultLogEvent();

        eventA.setMessage("one two three");
        eventB.setMessage("one TWO four");
    }

    @Test
    public void test() {
        CaseInsensitiveAsciiEventContainsFilter filter = new CaseInsensitiveAsciiEventContainsFilter("two");

        assertThat(filter.passes(eventA), is(true));
        assertThat(filter.passes(eventB), is(true));
    }

}