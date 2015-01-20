package com.logginghub.logging.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messaging.LogEventCodex;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.ExpandingByteBuffer;

@RunWith(CustomRunner.class)
public class TestLogEventCodex {

    private DefaultLogEvent event1 = LogEventFactory.createFullLogEvent1();
    private DefaultLogEvent event2 = LogEventFactory.createFullLogEvent1();
    private ExpandingByteBuffer buffer = new ExpandingByteBuffer();

    @Before public void setup() {
        event1.setPid(0);
        event2.setPid(1);
        
        event1.setChannel("channel1");
        event2.setChannel("channel2");
    }
    
    @Test public void test_encode_decode() throws PartialMessageException {
        LogEventCodex.encodeInternal_version1(buffer, event1);
        LogEventCodex.encodeInternal_version1(buffer, event1);
                
        buffer.flip();
        
        LogEvent decoded1 = LogEventCodex.decode(buffer.getBuffer());
        LogEvent decoded2 = LogEventCodex.decode(buffer.getBuffer());
        
        assertThat(decoded1.equals(event1), is(true));
        assertThat(decoded2.equals(event1), is(true));
        
        assertThat(decoded1.getPid(), is(0));
        assertThat(decoded2.getPid(), is(0));
        
        assertThat(decoded1.getChannel(), is(nullValue()));
        assertThat(decoded2.getChannel(), is(nullValue()));
    }    
    
    @Test public void test_encode_decode_with_pid() throws PartialMessageException {
        
        LogEventCodex.encodeInternal_version1_with_pid(buffer, event1);
        LogEventCodex.encodeInternal_version1_with_pid(buffer, event2);
        
        buffer.flip();
        
        LogEvent decoded1 = LogEventCodex.decode(buffer.getBuffer());
        LogEvent decoded2 = LogEventCodex.decode(buffer.getBuffer());
        
        assertThat(decoded1.equals(event1), is(true));
        assertThat(decoded2.equals(event2), is(true));
        
        assertThat(decoded1.getPid(), is(0));
        assertThat(decoded2.getPid(), is(1));
        
        assertThat(decoded1.getChannel(), is(nullValue()));
        assertThat(decoded2.getChannel(), is(nullValue()));
    }
    
 @Test public void test_encode_decode_with_channel_and_pid() throws PartialMessageException {
        
        LogEventCodex.encodeInternal_version1_with_channel_and_pid(buffer, event1);
        LogEventCodex.encodeInternal_version1_with_channel_and_pid(buffer, event2);
        
        buffer.flip();
        
        LogEvent decoded1 = LogEventCodex.decode(buffer.getBuffer());
        LogEvent decoded2 = LogEventCodex.decode(buffer.getBuffer());
        
        assertThat(decoded1.equals(event1), is(true));
        assertThat(decoded2.equals(event2), is(true));
        
        assertThat(decoded1.getPid(), is(0));
        assertThat(decoded2.getPid(), is(1));
        
        assertThat(decoded1.getChannel(), is("channel1"));
        assertThat(decoded2.getChannel(), is("channel2"));
    }

}
