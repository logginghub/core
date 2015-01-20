package com.logginghub.logging.modules;

import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.Logger;

public class LogEventFixture1 {

    public static final DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "Message from LogEventFixture : Event 1");
    public static final DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "Message from LogEventFixture : Event 2");
    public static final DefaultLogEvent event3 = LogEventBuilder.create(500, Logger.fine, "Message from LogEventFixture : Event 3");
    public static final DefaultLogEvent event4 = LogEventBuilder.create(999, Logger.severe, "Message from LogEventFixture : Event 4");

    public static final DefaultLogEvent event5 = LogEventBuilder.create(1000, Logger.info, "Message from LogEventFixture : Event 5");
    public static final DefaultLogEvent event6 = LogEventBuilder.create(1100, Logger.info, "Message from LogEventFixture : Event 6");
    public static final DefaultLogEvent event7 = LogEventBuilder.create(1200, Logger.info, "Message from LogEventFixture : Event 7");
    public static final DefaultLogEvent event8 = LogEventBuilder.create(1300, Logger.info, "Message from LogEventFixture : Event 8");
    public static final DefaultLogEvent event9 = LogEventBuilder.create(1400, Logger.info, "Message from LogEventFixture : Event 9");
    public static final DefaultLogEvent event10 = LogEventBuilder.create(1500, Logger.info, "Message from LogEventFixture : Event 10");
    public static final DefaultLogEvent event11 = LogEventBuilder.create(1600, Logger.info, "Message from LogEventFixture : Event 11");
    public static final DefaultLogEvent event12 = LogEventBuilder.create(1700, Logger.info, "Message from LogEventFixture : Event 12");
    public static final DefaultLogEvent event13 = LogEventBuilder.create(1800, Logger.info, "Message from LogEventFixture : Event 13");
    public static final DefaultLogEvent event14 = LogEventBuilder.create(1900, Logger.info, "Message from LogEventFixture : Event 14");

    public static final DefaultLogEvent event15 = LogEventBuilder.create(2100, Logger.info, "Message from LogEventFixture : Event 15");
    public static final DefaultLogEvent event16 = LogEventBuilder.create(3200, Logger.info, "Message from LogEventFixture : Event 16");
    public static final DefaultLogEvent event17 = LogEventBuilder.create(4300, Logger.info, "Message from LogEventFixture : Event 17");

    public static List<DefaultLogEvent> getEvents() {
        List<DefaultLogEvent> events = ReflectionUtils.extractAllFields(null, LogEventFixture1.class, DefaultLogEvent.class);
        return events;
    }

}
