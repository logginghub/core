package com.logginghub.utils.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.logging.Logger;


public class TestLogger {

    @Test public void testSetLevels() {
        assertThat(Logger.getLoggerFor("a").getLevel(), is(Logger.deferToRoot));
        assertThat(Logger.getLoggerFor("").getLevel(), is(Logger.info));
    }
}
