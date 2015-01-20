package com.logginghub.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.LoggingMessageInputStream;
import com.logginghub.logging.messaging.LoggingMessageOutputStream;
import com.logginghub.testutils.CustomRunner;

@RunWith(CustomRunner.class) public class TestLoggingMessageStreamEncoding {
    @Test public void testStreamSingle() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LoggingMessageOutputStream outputStream = new LoggingMessageOutputStream(baos);

        LogEvent logEvent = LogEventFactory.createFullLogEvent1("testApp");
        outputStream.write(logEvent);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        LoggingMessageInputStream input = new LoggingMessageInputStream(bais);
        LoggingMessage message = input.readLogEvent();
        LogEvent readLogEvent = ((LogEventMessage) message).getLogEvent();

        LogEventComparer.assertEquals(logEvent, readLogEvent);
    }

    @Test public void testStreamSingleMassive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LoggingMessageOutputStream outputStream = new LoggingMessageOutputStream(baos);

        LogEvent logEvent = LogEventFactory.createFullLogEventBig("testApp");
        outputStream.write(logEvent);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        LoggingMessageInputStream input = new LoggingMessageInputStream(bais);
        LoggingMessage message = input.readLogEvent();
        LogEvent readLogEvent = ((LogEventMessage) message).getLogEvent();

        LogEventComparer.assertEquals(logEvent, readLogEvent);
    }
}
