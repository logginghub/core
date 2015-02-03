package com.logginghub.logging.frontend.views.historicalevents;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TimeFieldTest {

    @Test public void testRoundTo() throws Exception {

        assertThat(TimeField.roundTo(8, 10), is(10));
        assertThat(TimeField.roundTo(1, 10), is(0));
        assertThat(TimeField.roundTo(5, 10), is(10));

        assertThat(TimeField.roundTo(80, 100), is(100));
        assertThat(TimeField.roundTo(10, 100), is(0));
        assertThat(TimeField.roundTo(50, 100), is(100));

    }
}