package com.logginghub.logging.messages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.messages.KryoSerialisationStrategy;
import com.logginghub.utils.ByteUtils;

public class TestKryoSerialisationStrategy {

    @Test public void testSerialise() throws Exception {

        DefaultLogEvent logEvent = LogEventBuilder.start().toLogEvent();
        ByteBuffer buffer = ByteBuffer.allocate((int) ByteUtils.kilobytes(100));
        
        KryoSerialisationStrategy serialisationStrategy = new KryoSerialisationStrategy();
        
        serialisationStrategy.serialise(buffer, logEvent);
        buffer.flip();
        
        LogEvent deserialised = (LogEvent) serialisationStrategy.deserialise(buffer);
        
        assertThat((DefaultLogEvent)deserialised, is(logEvent));
    
    
    }


}
