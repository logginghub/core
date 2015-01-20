package com.logginghub.logging.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.LoggingMessageCollectionMessage;
import com.logginghub.logging.messaging.AggregatingSender;
import com.logginghub.logging.messaging.AggregatingSenderException;
import com.logginghub.logging.utils.LoggingMessageBucket;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.ExceptionHandler;

@RunWith(CustomRunner.class)
@Ignore("There is some sort of race condition on the byte trigger test") public class TestAggreatingSender {
    private LoggingMessageBucket m_messageBucket;
    private Bucket<Throwable> m_exceptionBucket;
    private AggregatingSender m_aggregatingSender;
    private ExceptionHandler m_exceptionHandler;

    @Before public void setup() {
        m_messageBucket = new LoggingMessageBucket();

        LoggingMessageSender sender = new LoggingMessageSender() {
            public void send(LoggingMessage message) throws LoggingMessageSenderException {
                m_messageBucket.onNewLoggingMessage(message);
            }
        };

        m_exceptionBucket = new Bucket<Throwable>();
        m_exceptionHandler = new ExceptionHandler() {
            public void handleException(String message, Throwable t) {
                m_exceptionBucket.add(t);
            }
        };

        m_aggregatingSender = new AggregatingSender(sender, m_exceptionHandler);
    }

    @After public void teardown() {
        m_aggregatingSender.stop();
    }

    @Test public void testAccessors() {
        m_aggregatingSender.setTriggerInterval(-1);
        m_aggregatingSender.setTriggerBytes(-1);
        m_aggregatingSender.setTriggerSize(2);

        assertEquals(-1, m_aggregatingSender.getTriggerInterval());
        assertEquals(-1, m_aggregatingSender.getTriggerBytes());
        assertEquals(2, m_aggregatingSender.getTriggerSize());
    }

    @Test public void testCountTrigger() throws LoggingMessageSenderException {
        m_aggregatingSender.setTriggerInterval(-1);
        m_aggregatingSender.setTriggerBytes(-1);
        m_aggregatingSender.setTriggerSize(2);

        LogEventMessage message = new LogEventMessage(LogEventFactory.createFullLogEvent1());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_aggregatingSender.send(message);
        m_messageBucket.waitForMessages(1, 5, TimeUnit.SECONDS);
        assertEquals(1, m_messageBucket.size());

        LoggingMessage loggingMessage = m_messageBucket.getEvents().get(0);
        LoggingMessageCollectionMessage aggregated = (LoggingMessageCollectionMessage) loggingMessage;
        assertEquals(2, aggregated.getMessages().size());

        m_aggregatingSender.send(message);
        assertEquals(1, m_messageBucket.size());

        m_aggregatingSender.send(message);
        m_messageBucket.waitForMessages(2, 5, TimeUnit.SECONDS);
        assertEquals(2, m_messageBucket.size());

        loggingMessage = m_messageBucket.getEvents().get(0);
        aggregated = (LoggingMessageCollectionMessage) loggingMessage;
        assertEquals(2, aggregated.getMessages().size());

        assertEquals(0, m_exceptionBucket.size());
    }

    @Test public void testBytesTrigger() throws LoggingMessageSenderException {
        m_aggregatingSender.setTriggerInterval(-1);
        m_aggregatingSender.setTriggerBytes(1024);
        m_aggregatingSender.setTriggerSize(-1);

        LogEventMessage message = new LogEventMessage(LogEventFactory.createFullLogEvent1());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        LogEventMessage bigMessage = new LogEventMessage(LogEventFactory.createFullLogEventMassive());
        m_aggregatingSender.send(bigMessage);
        m_messageBucket.waitForMessages(1, 5, TimeUnit.SECONDS);

        LoggingMessage loggingMessage = m_messageBucket.getEvents().get(0);
        LoggingMessageCollectionMessage aggregated = (LoggingMessageCollectionMessage) loggingMessage;
        assertEquals(3, aggregated.getMessages().size());

        assertEquals(0, m_exceptionBucket.size());
    }

    @Test public void testTimeTrigger() throws LoggingMessageSenderException, InterruptedException {
        m_aggregatingSender.setTriggerInterval(500);
        m_aggregatingSender.setTriggerBytes(-1);
        m_aggregatingSender.setTriggerSize(-1);

        LogEventMessage message = new LogEventMessage(LogEventFactory.createFullLogEvent1());

        // Calling this will reset the timer
        m_aggregatingSender.setTriggerInterval(500);

        // We need to give the aggregation thread time to reset to that new time
        Thread.sleep(10);

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        long start = System.currentTimeMillis();
        m_messageBucket.waitForMessages(1, 5, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(1, m_messageBucket.size());
        assertTrue("Elapsed time should have been around 500ms but was " + elapsed, elapsed > 400 && elapsed < 600);

        LoggingMessage loggingMessage = m_messageBucket.getEvents().get(0);
        LoggingMessageCollectionMessage aggregated = (LoggingMessageCollectionMessage) loggingMessage;
        assertEquals(3, aggregated.getMessages().size());

        assertEquals(0, m_exceptionBucket.size());
    }

    @Test public void testSendError() throws LoggingMessageSenderException {
        LoggingMessageSender sender = new LoggingMessageSender() {
            public void send(LoggingMessage message) throws LoggingMessageSenderException {
                throw new LoggingMessageSenderException("Failed to send message");
            }
        };

        m_aggregatingSender = new AggregatingSender(sender, m_exceptionHandler);

        m_aggregatingSender.setTriggerInterval(-1);
        m_aggregatingSender.setTriggerBytes(-1);
        m_aggregatingSender.setTriggerSize(2);

        LogEventMessage message = new LogEventMessage(LogEventFactory.createFullLogEvent1());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_aggregatingSender.send(message);
        assertEquals(0, m_messageBucket.size());

        m_exceptionBucket.waitForMessages(1, 5, TimeUnit.SECONDS);
        assertEquals(1, m_exceptionBucket.size());
        assertEquals(0, m_messageBucket.size());

        Throwable throwable = m_exceptionBucket.getEvents().get(0);
        AggregatingSenderException exception = (AggregatingSenderException) throwable;
        assertEquals(2, exception.getMessageCollection().getMessages().size());
    }
}
