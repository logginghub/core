package com.logginghub.logging;

import java.util.logging.LogRecord;

import junit.framework.TestCase;

public class LogEventComparer
{
    public static void assertEquals(LogEvent a, LogEvent b)
    {
        assertEquals("", a, b);
    }
    
    public static void assertEquals(String description, LogEvent a, LogEvent b)
    {
        TestCase.assertEquals(description, a.getOriginTime(), b.getOriginTime());
        TestCase.assertEquals(description,a.getSequenceNumber(), b.getSequenceNumber());
        TestCase.assertEquals(description,a.getFormattedException(), b.getFormattedException());
        TestCase.assertEquals(description,a.getFormattedObject(), b.getFormattedObject());
        TestCase.assertEquals(description,a.getLevel(), b.getLevel());
        TestCase.assertEquals(description,a.getJavaLevel(), b.getJavaLevel());
        TestCase.assertEquals(description,a.getLoggerName(), b.getLoggerName());
        TestCase.assertEquals(description,a.getMessage(), b.getMessage());
        TestCase.assertEquals(description,a.getSourceApplication(), b.getSourceApplication());
        TestCase.assertEquals(description,a.getSourceClassName(), b.getSourceClassName());
        TestCase.assertEquals(description,a.getSourceHost(), b.getSourceHost());
        TestCase.assertEquals(description,a.getSourceMethodName(), b.getSourceMethodName());
        TestCase.assertEquals(description,a.getThreadName(), b.getThreadName());
    }

    public static void assertEquals(LogRecord expected, LogEvent actual)
    {
        TestCase.assertEquals(expected.getMillis(), actual.getOriginTime());
        TestCase.assertEquals(expected.getSequenceNumber(), actual.getSequenceNumber());
        TestCase.assertEquals(expected.getLevel(), actual.getJavaLevel());
        TestCase.assertEquals(expected.getLoggerName(), actual.getLoggerName());
        TestCase.assertEquals(expected.getMessage(), actual.getMessage());
        TestCase.assertEquals(expected.getSourceClassName(), actual.getSourceClassName());
        TestCase.assertEquals(expected.getSourceMethodName(), actual.getSourceMethodName());
    }

    public static boolean haveSameValues(LogEvent fullLogEvent, LogRecord logRecord)
    {
        boolean same = true;

        same &= fullLogEvent.getMessage().equals(logRecord.getMessage());
        same &= fullLogEvent.getLevel() == logRecord.getLevel().intValue();

        return same;
    }

    public static void assertEquals(LogEventCollection a, LogEventCollection b)
    {
        TestCase.assertEquals("Collections dont have the same number of items", a.size(), b.size());

        for(int i = 0; i < a.size(); i++)
        {
            LogEvent aEvent = a.get(i);
            LogEvent bEvent = b.get(i);
            assertEquals("LogEvent at index " + i + " didn't match", aEvent, bEvent);
        }

    }
}
