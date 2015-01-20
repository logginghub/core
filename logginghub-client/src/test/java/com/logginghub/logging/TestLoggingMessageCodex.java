package com.logginghub.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventCollection;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messages.LogEventCollectionMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messaging.LoggingMessageCodex;
import com.logginghub.logging.messaging.LoggingMessageCodex.Flags;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.sof.SofException;

@RunWith(CustomRunner.class) public class TestLoggingMessageCodex {

    @Test public void test_sof() throws PartialMessageException, SofException {
        LoggingMessageCodex codex = new LoggingMessageCodex();

        codex.getSofConfiguration().registerType(SofObject.class, 100);
        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        SofObject object = new SofObject("hello");
        codex.encode(buffer, object);

        buffer.flip();

        SofObject decoded = (SofObject) codex.decode(buffer.getBuffer());

        assertThat(decoded.getMessage(), is("hello"));
    }

    @Test public void test_sof_with_unknown_object() throws PartialMessageException, SofException {
        LoggingMessageCodex codex = new LoggingMessageCodex();

        codex.getSofConfiguration().registerType(SofObject.class, 100);
        codex.getSofConfiguration().registerType(SofObject2.class, 101);

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        SofObject object1 = new SofObject("hello 1");
        SofObject2 object2 = new SofObject2("hello 2");
        SofObject object3 = new SofObject("hello 3");

        codex.encode(buffer, object1);
        codex.encode(buffer, object2);
        codex.encode(buffer, object3);

        buffer.flip();

        // Create another codex that doesn't know about one of those messages
        LoggingMessageCodex codex2 = new LoggingMessageCodex();
        codex2.getSofConfiguration().registerType(SofObject.class, 100);
        
        SofObject decoded1 = (SofObject) codex2.decode(buffer.getBuffer());
        SofObject2 decoded2 = (SofObject2) codex2.decode(buffer.getBuffer());
        SofObject decoded3 = (SofObject) codex2.decode(buffer.getBuffer());

        assertThat(decoded1.getMessage(), is("hello 1"));
        assertThat(decoded2, is(nullValue()));
        assertThat(decoded3.getMessage(), is("hello 3"));
    }

    @Test public void testEventEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApp");

        decoder.encode(buffer, event);

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventMessage);

        LogEventMessage message = (LogEventMessage) decoded;

        LogEventComparer.assertEquals(event, message.getLogEvent());
    }

    @Test public void testEventCollectionEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event1 = LogEventFactory.createFullLogEvent1("TestApp");
        LogEvent event2 = LogEventFactory.createFullLogEvent2("TestApp");
        LogEventCollection collection = new LogEventCollection();
        collection.add(event1);
        collection.add(event2);

        decoder.encode(buffer, collection);

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventCollectionMessage);

        LogEventCollectionMessage message = (LogEventCollectionMessage) decoded;

        LogEventComparer.assertEquals(collection, message.getLogEventCollection());
    }

    @Test public void testCompressedEventEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApp");

        decoder.encode(buffer, event, EnumSet.of(Flags.Compressed));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventMessage);

        LogEventMessage message = (LogEventMessage) decoded;

        LogEventComparer.assertEquals(event, message.getLogEvent());
    }

    @Test public void testCompressedEventCollectionEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event1 = LogEventFactory.createFullLogEvent1("TestApp");
        LogEvent event2 = LogEventFactory.createFullLogEvent2("TestApp");
        LogEventCollection collection = new LogEventCollection();
        collection.add(event1);
        collection.add(event2);

        decoder.encode(buffer, collection, EnumSet.of(Flags.Compressed));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventCollectionMessage);

        LogEventCollectionMessage message = (LogEventCollectionMessage) decoded;

        LogEventComparer.assertEquals(collection, message.getLogEventCollection());
    }

    @Test public void testEncryptedEventEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApp");

        decoder.encode(buffer, event, EnumSet.of(Flags.Encrypted));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventMessage);

        LogEventMessage message = (LogEventMessage) decoded;

        LogEventComparer.assertEquals(event, message.getLogEvent());
    }

    @Test public void testEncryptedEventCollectionEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event1 = LogEventFactory.createFullLogEvent1("TestApp");
        LogEvent event2 = LogEventFactory.createFullLogEvent2("TestApp");
        LogEventCollection collection = new LogEventCollection();
        collection.add(event1);
        collection.add(event2);

        decoder.encode(buffer, collection, EnumSet.of(Flags.Encrypted));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventCollectionMessage);

        LogEventCollectionMessage message = (LogEventCollectionMessage) decoded;

        LogEventComparer.assertEquals(collection, message.getLogEventCollection());
    }

    @Test public void testCompressedAndEncryptedEventEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApp");

        decoder.encode(buffer, event, EnumSet.of(Flags.Encrypted, Flags.Compressed));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventMessage);

        LogEventMessage message = (LogEventMessage) decoded;

        LogEventComparer.assertEquals(event, message.getLogEvent());
    }

    @Test public void testEncryptedAndCompresedEventCollectionEncoding() throws PartialMessageException {
        LoggingMessageCodex decoder = new LoggingMessageCodex();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        LogEvent event1 = LogEventFactory.createFullLogEvent1("TestApp");
        LogEvent event2 = LogEventFactory.createFullLogEvent2("TestApp");
        LogEventCollection collection = new LogEventCollection();
        collection.add(event1);
        collection.add(event2);

        decoder.encode(buffer, collection, EnumSet.of(Flags.Encrypted, Flags.Compressed));

        buffer.flip();

        LoggingMessage decoded = decoder.decode(buffer.getBuffer());
        assertTrue("Correct class", decoded instanceof LogEventCollectionMessage);

        LogEventCollectionMessage message = (LogEventCollectionMessage) decoded;

        LogEventComparer.assertEquals(collection, message.getLogEventCollection());
    }
}
