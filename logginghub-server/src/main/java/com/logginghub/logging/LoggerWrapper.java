package com.logginghub.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerWrapper
{
    private final Logger m_logger;

    public LoggerWrapper(Logger logger)
    {
        m_logger = logger;
    }

    public static LoggerWrapper getLogger(String name)
    {
        Logger logger = Logger.getLogger(name);
        LoggerWrapper wrapper = new LoggerWrapper(logger);
        return wrapper;
    }

    public void fine(Object msg)
    {
        if(m_logger.isLoggable(Level.FINE))
        {
            LogRecord lr = new LogRecord(Level.FINE, msg.toString());
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void fine(String format, Object... objects)
    {
        if(m_logger.isLoggable(Level.FINE))
        {
            LogRecord lr = new LogRecord(Level.FINE, String.format(format, objects));
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void finer(Object msg)
    {
        if(m_logger.isLoggable(Level.FINER))
        {
            LogRecord lr = new LogRecord(Level.FINER, msg.toString());
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void finer(String format, Object... objects)
    {
        if(m_logger.isLoggable(Level.FINER))
        {
            LogRecord lr = new LogRecord(Level.FINER, String.format(format, objects));
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void finest(Object msg)
    {
        if(m_logger.isLoggable(Level.FINEST))
        {
            LogRecord lr = new LogRecord(Level.FINEST, msg.toString());
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void finest(String format, Object... objects)
    {
        if(m_logger.isLoggable(Level.FINEST))
        {
            LogRecord lr = new LogRecord(Level.FINEST, String.format(format, objects));
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void warning(Object msg)
    {
        if(m_logger.isLoggable(Level.WARNING))
        {
            LogRecord lr = new LogRecord(Level.WARNING, msg.toString());
            inferCaller(lr);
            m_logger.log(lr);
        }
    }
    
    public void warning(String format, Object... objects)
    {
        if(m_logger.isLoggable(Level.WARNING))
        {
            LogRecord lr = new LogRecord(Level.WARNING, String.format(format, objects));
            inferCaller(lr);
            m_logger.log(lr);
        }
    }

    /**
     * Java logging is so crap. In order to get the ... logging stuff done we have to 
     * lift this shitty stack trace inspector code from log record...
     * @param lr
     */
    private void inferCaller(LogRecord lr)
    {        
        // Get the stack trace.
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        
        // First, search back to a method in the Logger class.
        int ix = 0;
        while(ix < stack.length)
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if(cname.equals(LoggerWrapper.class.getName()))
            {
                break;
            }
            ix++;
        }
        // Now search for the first frame before the "Logger" class.
        while(ix < stack.length)
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if(!cname.equals(LoggerWrapper.class.getName()))
            {
                // We've found the relevant frame.
                lr.setSourceClassName(cname);
                lr.setSourceMethodName(frame.getMethodName());
                return;
            }
            ix++;
        }
        // We haven't found a suitable frame, so just punt. This is
        // OK as we are only committed to making a "best effort" here.
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public void log(Level fine, String string)
    {
        m_logger.log(fine, string);
    } 
}
