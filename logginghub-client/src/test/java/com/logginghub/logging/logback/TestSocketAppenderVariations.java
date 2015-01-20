package com.logginghub.logging.logback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doAnswer;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.logback.SocketAppender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;

@RunWith(CustomRunner.class)
public class TestSocketAppenderVariations {

    private Bucket<LoggingMessage> sentMessages;
    private Logger logger;
    private String threadName;

    @Test public void test_info() {
        logger.info("Info logging");
        verify(Level.INFO, "test_info", "Info logging");
    }

    @Test public void test_warn() {
        logger.warn("Warn logging");
        verify(Level.WARNING, "test_warn", "Warn logging");
    }

    @Test public void test_debug() {
        logger.debug("Debug logging");
        verify(Level.FINE, "test_debug", "Debug logging");
    }

    @Test public void test_error() {
        logger.error("Error logging");
        verify(Level.SEVERE, "test_error", "Error logging");
    }

    @Test public void test_trace() {
        logger.trace("Trace logging");
        verify(Level.FINEST, "test_trace", "Trace logging");
    }

    @Test public void test_warn_with_exception() {
        Exception e = new Exception("Deliberate exception");
        e.fillInStackTrace();
        
        logger.warn("Warn logging with exception", e);
        LogEvent logEvent = verify(Level.WARNING, "test_warn_with_exception", "Warn logging with exception");
        
        assertThat(logEvent.getFormattedException(), is(not(nullValue())));
        assertThat(logEvent.getFormattedException(), containsString("Deliberate exception"));
    }
    
    @Test public void test_info_with_mdc() {
        MDC.put("key", "value");
        logger.info("Info logging with MDC");
        LogEvent logEvent = verify(Level.INFO, "test_info_with_mdc", "Info logging with MDC");
        
        assertThat(logEvent.getFormattedObject(), is(not(nullValue())));
        assertThat(logEvent.getFormattedObject().length, is(1));
        assertThat(logEvent.getFormattedObject()[0], containsString("key : value"));
        MDC.clear();
    }
    
    private LogEvent verify(Level level, String method, String message) {
        sentMessages.waitForMessages(1);

        assertThat(sentMessages.size(), is(1));

        LoggingMessage loggingMessage = sentMessages.get(0);
        assertThat(loggingMessage, is(instanceOf(LogEventMessage.class)));

        LogEventMessage logEventMessage = (LogEventMessage) loggingMessage;

        LogEvent logEvent = logEventMessage.getLogEvent();
        assertThat(logEvent, is(not(nullValue())));
        assertThat(logEvent.getFlavour(), is("logback"));                
        assertThat(logEvent.getJavaLevel(), is(level));
        assertThat(logEvent.getLevel(), is(level.intValue()));
        assertThat(logEvent.getLevelDescription(), is(level.getName()));
        assertThat(logEvent.getOriginTime(), is(greaterThan(0L)));
        assertThat(logEvent.getLoggerName(), is("com.logginghub.logging.logback.LoggingTest"));
        assertThat(logEvent.getMessage(), is(message));
        assertThat(logEvent.getSequenceNumber(), is(0L));
        assertThat(logEvent.getSourceAddress(), is(NetUtils.getLocalIP()));
        assertThat(logEvent.getSourceApplication(), is("sourceApplication"));
        assertThat(logEvent.getSourceClassName(), is("com.logginghub.logging.logback.TestSocketAppenderVariations"));
        assertThat(logEvent.getSourceHost(), is(NetUtils.getLocalHostname()));
        assertThat(logEvent.getSourceMethodName(), is(method));
        assertThat(logEvent.getThreadName(), is(threadName));
        
        return logEvent;
    }

    @Before public void setup() throws LoggingMessageSenderException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure("src/test/resources/com/logginghub/logging/logback/socket_appender_config.xml");
        }
        catch (JoranException je) {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        logger = LoggerFactory.getLogger("com.logginghub.logging.logback.LoggingTest");

        Logger rootLogger = LoggerFactory.getLogger("ROOT");

        assertThat(rootLogger, is(instanceOf(ch.qos.logback.classic.Logger.class)));

        ch.qos.logback.classic.Logger actualLogger = (ch.qos.logback.classic.Logger) rootLogger;

        Appender<ILoggingEvent> appender = actualLogger.getAppender("socket");
        assertThat(appender, is(not(nullValue())));
        assertThat(appender, is(instanceOf(com.logginghub.logging.logback.SocketAppender.class)));

        final SocketAppender socketAppender = (com.logginghub.logging.logback.SocketAppender) appender;

        SocketClient mock = Mockito.mock(SocketClient.class);

        sentMessages = new Bucket<LoggingMessage>();

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                sentMessages.add((LoggingMessage) args[0]);
                return null;
            }
        }).when(mock).send(Mockito.any(LoggingMessage.class));

        socketAppender.setSocketClient(mock);

        this.threadName = Thread.currentThread().getName();
    }
    
    @After public void stop() {
        ((LoggerContext)LoggerFactory.getILoggerFactory()).stop();
    }
}
