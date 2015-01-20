package com.logginghub.logging.logeventformatters;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.logging.Level;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(CustomRunner.class) public class TestFullEventSingleLineTextFormatter {

    private FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();

    @Test public void testFormat() {

        LogEvent logEvent = LogEventBuilder.start()
                                           .setMessage("message")
                                           .setLocalCreationTimeMillis(1)
                                           .setLevel(Level.INFO.intValue())
                                           .toLogEvent();
        String format = formatter.format(logEvent);
        assertThat(format,
                   is(StringUtils.format("{} {} {} {} {} {} {} {}  {}",
                                         "01-Jan-1970 01:00:00.001",
                                         StringUtils.padRight(NetUtils.getLocalHostname(), 15),
                                         StringUtils.padRight(NetUtils.getLocalIP(), 15),
                                         StringUtils.padRight("0", 7),
                                         StringUtils.padRight("TestApplication", 15),
                                         StringUtils.padRight("LogEventFactory.getLogRecord1()", 10),
                                         StringUtils.padRight(Thread.currentThread().getName(), 15),
                                         StringUtils.padRight("INFO", 10),
                                         "message")));

    }


}
