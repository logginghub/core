package com.logginghub.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.Dictionary;

public class TestDictionaryLogEvent {

    @Test public void testFromLogEvent() throws Exception {

        DefaultLogEvent logEvent = LogEventBuilder.start().setMessage("This is a log message").toLogEvent();
        
        Dictionary dictionary = new Dictionary();

        DictionaryLogEvent dictionaryEvent = DictionaryLogEvent.fromLogEvent(logEvent, dictionary);
        
        DefaultLogEvent decoded = dictionaryEvent.toLogEvent(dictionary);
        
        assertThat(decoded, is(logEvent));
    }

}
