package com.logginghub.logging.logeventformatters.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.logeventformatters.log4j.Log4jPatternLogEventFormatter;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.StringUtils;

@RunWith(CustomRunner.class) public class TestLog4jPatternLogEventFormatter {

    private Log4jPatternLogEventFormatter patternLayout;

    @Test public void testFormat() {
        patternLayout = new Log4jPatternLogEventFormatter("%d %-4r [%t] %a %h %i %-5p %c %M %x - %m%n");
        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();

        if (OSUtils.isWindows()) {
            assertThat(patternLayout.format(event),
                       is(endsWith(StringUtils.format("[{}] TestApplication {} {} INFO   getLogRecord1  - This is mock record 1\r\n",
                                                      Thread.currentThread().getName(),
                                                      NetUtils.getLocalHostname(),
                                                      NetUtils.getLocalIP()))));
        }
        else {
            assertThat(patternLayout.format(event),
                       is(endsWith(StringUtils.format("[{}] TestApplication {} {} INFO   getLogRecord1  - This is mock record 1\n",
                                                      Thread.currentThread().getName(),
                                                      NetUtils.getLocalHostname(),
                                                      NetUtils.getLocalIP()))));
        }
    }

}