package com.logginghub.logging.adaptors.slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import com.logginghub.logging.adaptors.slf4j.Logger;
import com.logginghub.logging.adaptors.slf4j.LoggerFactory;
import com.logginghub.utils.Bucket;

public class LoggerTest
{
    @Test public void test()
    {
        final Bucket<LoggingEvent> loggingEvents = new Bucket<LoggingEvent>();
        LogManager.getRootLogger().addAppender(new AppenderSkeleton()
        {
            @Override public boolean requiresLayout()
            {
                return false;
            }

            @Override public void close()
            {

            }

            @Override protected void append(LoggingEvent event)
            {
                loggingEvents.add(event);
            }
        });

        Logger logger = LoggerFactory.getLogger(LoggerTest.class);
        String msg = "this is a test message";
        logger.info(msg);
        
        
        assertThat(loggingEvents.size(), is(1));
        
        LoggingEvent loggingEvent = loggingEvents.get(0);
        
        String message = loggingEvent.getMessage().toString();
        assertThat(message, is(msg));
        assertThat(loggingEvent.getLevel(), is(Level.INFO));
        assertThat(loggingEvent.getLoggerName(), is(LoggerTest.class.getName()));
        
    }
}
