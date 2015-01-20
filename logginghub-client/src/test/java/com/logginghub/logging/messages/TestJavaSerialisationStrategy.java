package com.logginghub.logging.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.messages.JavaSerialisationStrategy;
import com.logginghub.utils.ByteUtils;

public class TestJavaSerialisationStrategy {

    @Test public void testSerialise() throws Exception {

        DefaultLogEvent logEvent = LogEventBuilder.start().toLogEvent();
        ByteBuffer buffer = ByteBuffer.allocate((int) ByteUtils.kilobytes(100));
        
        JavaSerialisationStrategy javaSerialisationStrategy = new JavaSerialisationStrategy();
        
        javaSerialisationStrategy.serialise(buffer, logEvent);
        buffer.flip();
        
        LogEvent deserialised = (LogEvent) javaSerialisationStrategy.deserialise(buffer);
        
        assertThat((DefaultLogEvent)deserialised, is(logEvent));
    
    
    }


}
