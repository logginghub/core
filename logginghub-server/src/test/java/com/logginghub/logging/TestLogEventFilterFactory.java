package com.logginghub.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.LogEventFilterFactory;
import com.logginghub.logging.LogEventFilterFactory.LogField;
import com.logginghub.utils.StringMatcherFactory.MatcherType;

public class TestLogEventFilterFactory {

    private LogEvent event = LogEventBuilder.start()
                                            .setChannel("channel")
                                            .setFormattedException("FormattedException")
                                            .setFormattedObject("FormattedObject")
                                            .setLoggerName("LoggerName")
                                            .setMessage("Message")
                                            .setSourceAddress("SourceAddress")
                                            .setSourceApplication("SourceApplication")
                                            .setSourceClassName("SourceClassName")
                                            .setSourceHost("SourceHost")
                                            .setSourceMethodName("SourceMethodName")
                                            .setThreadName("ThreadName")
                                            .toLogEvent();
    
    private LogEvent nullEvent = LogEventBuilder.start()
                    .setChannel(null)
                    .setFormattedException(null)
                    .setFormattedObject((String)null)
                    .setLoggerName(null)
                    .setMessage(null)
                    .setSourceAddress(null)
                    .setSourceApplication(null)
                    .setSourceClassName(null)
                    .setSourceHost(null)
                    .setSourceMethodName(null)
                    .setThreadName(null)
                    .toLogEvent();

    @Test public void test() throws Exception {

        assertThat(LogEventFilterFactory.createFilterForField(LogField.Channel, MatcherType.StartsWith, "channel").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Channel, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedException, MatcherType.StartsWith, "FormattedException").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedException, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedObject, MatcherType.StartsWith, "FormattedObject").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedObject, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.LoggerName, MatcherType.StartsWith, "LoggerName").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.LoggerName, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Message, MatcherType.StartsWith, "Message").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Message, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceAddress, MatcherType.StartsWith, "SourceAddress").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceAddress, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceApplication, MatcherType.StartsWith, "SourceApplication").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceApplication, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceClassName, MatcherType.StartsWith, "SourceClassName").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceClassName, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceHost, MatcherType.StartsWith, "SourceHost").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceHost, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceMethodName, MatcherType.StartsWith, "SourceMethodName").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceMethodName, MatcherType.StartsWith, "no match").passes(event), is(false));
        
        assertThat(LogEventFilterFactory.createFilterForField(LogField.ThreadName, MatcherType.StartsWith, "ThreadName").passes(event), is(true));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.ThreadName, MatcherType.StartsWith, "no match").passes(event), is(false));
    }
    
    @Test public void test_null_object() throws Exception {
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Channel, MatcherType.StartsWith, "channel").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedException, MatcherType.StartsWith, "FormattedException").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedObject, MatcherType.StartsWith, "FormattedObject").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.LoggerName, MatcherType.StartsWith, "LoggerName").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Message, MatcherType.StartsWith, "Message").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceAddress, MatcherType.StartsWith, "SourceAddress").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceApplication, MatcherType.StartsWith, "SourceApplication").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceClassName, MatcherType.StartsWith, "SourceClassName").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceHost, MatcherType.StartsWith, "SourceHost").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceMethodName, MatcherType.StartsWith, "SourceMethodName").passes(nullEvent), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.ThreadName, MatcherType.StartsWith, "ThreadName").passes(nullEvent), is(false));
    }
    
    @Test public void test_null_filter() throws Exception {
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Channel, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedException, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.FormattedObject, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.LoggerName, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.Message, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceAddress, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceApplication, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceClassName, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceHost, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.SourceMethodName, MatcherType.StartsWith, null).passes(event), is(false));
        assertThat(LogEventFilterFactory.createFilterForField(LogField.ThreadName, MatcherType.StartsWith, null).passes(event), is(false));
    }

}
