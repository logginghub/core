package com.logginghub.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.logging.Logger;

public class LogEventFactory {
    private static final String TEST_APPLICATION = "TestApplication";

    private static InetAddress localhost;
    static {
        try {
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Failed to resolve local host address", e);
        }
    }

    public static DefaultLogEvent createFullLogEvent1() {
        return createFullLogEvent1(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent2() {
        return createFullLogEvent2(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent3() {
        return createFullLogEvent3(TEST_APPLICATION);
    }

    public static DefaultLogEvent createFullLogEvent1(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(LogRecordFactory.getLogRecord1(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEvent2(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(LogRecordFactory.getLogRecord2(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEvent3(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(LogRecordFactory.getLogRecord3(), sourceApplication, localhost);
        return event;
    }

    /**
     * Returns a event with a message > 64k
     * 
     * @param sourceApplication
     * @return
     */
    public static DefaultLogEvent createFullLogEventMassive(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(LogRecordFactory.getLogRecordMassive(), sourceApplication, localhost);
        return event;
    }

    /**
     * Returns a event with a message > 10k
     * 
     * @param sourceApplication
     * @return
     */
    public static DefaultLogEvent createFullLogEventBig(String sourceApplication) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.populateFromLogRecord(LogRecordFactory.getLogRecordBig(), sourceApplication, localhost);
        return event;
    }

    public static DefaultLogEvent createFullLogEventMassive() {
        return createFullLogEventMassive(TEST_APPLICATION);
    }

    public static DefaultLogEvent createRandomEvent() {

        double random = Math.random();
        int percent = (int) (random * 1000);

        Level level = null;

        if (percent >= 998) {
            level = Level.SEVERE;
        }
        else if (percent > 990) {
            level = Level.WARNING;
        }
        else if (percent > 200) {
            level = Level.INFO;
        }
        else if (percent > 100) {
            level = Level.FINE;
        }
        else if (percent >50 ) {
            level = Level.FINER;
        }
        else {
            level = Level.FINEST;
        }

        DefaultLogEvent logEvent = LogEventBuilder.start().setLevel(level.intValue()).setMessage("Random event").setSourceApplication("Source applcation").toLogEvent();        
        return logEvent;

    }

    public static DefaultLogEvent createLogEvent(String message) {
        DefaultLogEvent event = createFullLogEvent1();
        event.setMessage(message);
        return event;
         
    }

    public static DefaultLogEvent createLogEvent(long time, String message) {
        DefaultLogEvent event = createFullLogEvent1();
        event.setMessage(message);
        event.setLocalCreationTimeMillis(time);
        return event;
    }
    
    public static PatternisedLogEvent createPatternisedEvent() {
        PatternisedLogEvent patternised = new PatternisedLogEvent();
        patternised.setChannel("channel-patternised1");
        patternised.setLevel(Logger.info);
        patternised.setLoggerName("loggerName");
        patternised.setPatternID(2);
        patternised.setPid(234);
        patternised.setSequenceNumber(34566);
        patternised.setSourceAddress("sourceAddress");
        patternised.setSourceApplication("sourceApplication");
        patternised.setSourceClassName("sourceClassName");
        patternised.setSourceHost("sourceHost");
        patternised.setSourceMethodName("sourceMethodName");
        patternised.setThreadName("threadName");
        patternised.setTime(1000);
        patternised.setVariables(new String[] { "a", "b", "c"});
        return patternised;
    }
}
