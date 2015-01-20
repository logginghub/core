package com.logginghub.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Provides some out of the box log records.
 * @author admin
 */
public class LogRecordFactory
{
    public static LogRecord getLogRecord1()
    {
        LogRecord record = new LogRecord(Level.INFO, "This is mock record 1");
        record.setSourceClassName("LogEventFactory");
        record.setSourceMethodName("getLogRecord1");
        return record;
    }
    
    public static LogRecord getLogRecord2()
    {
        LogRecord record = new LogRecord(Level.INFO, "This is mock record 2");
        record.setSourceClassName("LogEventFactory");
        record.setSourceMethodName("getLogRecord2");
        return record;
    }
    
    public static LogRecord getLogRecord3()
    {
        LogRecord record = new LogRecord(Level.INFO, "This is mock record 3");
        record.setSourceClassName("LogEventFactory");
        record.setSourceMethodName("getLogRecord3");
        return record;
    }
    
    /**
     * Returns a record with a message > 64k
     * @param sourceApplication
     * @return
     */
    public static LogRecord getLogRecordMassive()
    {
        int size = 64000;
        StringBuilder builder = new StringBuilder();
        while(builder.length() < size)
        {
            builder.append("This is a bit of text thats going to get repeated a lot. ");
        }
        
        LogRecord record = new LogRecord(Level.INFO, builder.toString());
        return record;
    }
    
    /**
     * Returns a record with a message > 10k
     * @param sourceApplication
     * @return
     */
    public static LogRecord getLogRecordBig()
    {
        int size = 10000;
        StringBuilder builder = new StringBuilder();
        while(builder.length() < size)
        {
            builder.append("This is a bit of text thats going to get repeated a lot. ");
        }
        
        LogRecord record = new LogRecord(Level.INFO, builder.toString());
        record.setSourceClassName("LogEventFactory");
        record.setSourceMethodName("getLogRecordBig");
        
        return record;
    }
}
