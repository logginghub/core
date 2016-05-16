package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.logging.Logger;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by james on 16/05/2016.
 */
public class LogEventTemplateReplacerTest {

    @Test
    public void test() {

        LogEvent event = LogEventBuilder.logEvent()
                                        .hubTime(1)
                                        .message("m")
                                        .metadata("metadata1", "metadata1value")
                                        .originTime(2)
                                        .sequence(3)
                                        .setChannel("channel")
                                        .setFormattedException("formattedException")
                                        .setFormattedObject("object")
                                        .setLevel(Logger.info)
                                        .hubTime(4)
                                        .setLoggerName("loggername")
                                        .setPid(5)
                                        .setSourceAddress("sourceaddress")
                                        .setSourceApplication("sourceapp")
                                        .setSourceClassName("sourceclass")
                                        .setSourceHost("sourcehost")
                                        .setSourceMethodName("sourcemethod")
                                        .setThreadName("threadname")
                                        .toLogEvent();

        assertThat(LogEventTemplateReplacer.replace("", event), is(""));
        assertThat(LogEventTemplateReplacer.replace("${message}", event), is("m"));
        assertThat(LogEventTemplateReplacer.replace("${metadata1}", event), is("metadata1value"));
        assertThat(LogEventTemplateReplacer.replace("${originTime}", event), is("01/01/1970 00:00:00.002"));
        assertThat(LogEventTemplateReplacer.replace("${sequence}", event), is("3"));
        assertThat(LogEventTemplateReplacer.replace("${channel}", event), is("channel"));
        assertThat(LogEventTemplateReplacer.replace("${formattedException}", event), is("formattedException"));
        assertThat(LogEventTemplateReplacer.replace("${formattedObject}", event), is("[object]"));
        assertThat(LogEventTemplateReplacer.replace("${level}", event), is("INFO"));
        assertThat(LogEventTemplateReplacer.replace("${hubTime}", event), is("01/01/1970 00:00:00.004"));
        assertThat(LogEventTemplateReplacer.replace("${loggerName}", event), is("loggername"));
        assertThat(LogEventTemplateReplacer.replace("${pid}", event), is("5"));
        assertThat(LogEventTemplateReplacer.replace("${sourceAddress}", event), is("sourceaddress"));
        assertThat(LogEventTemplateReplacer.replace("${sourceApplication}", event), is("sourceapp"));
        assertThat(LogEventTemplateReplacer.replace("${sourceClassName}", event), is("sourceclass"));
        assertThat(LogEventTemplateReplacer.replace("${sourceHost}", event), is("sourcehost"));
        assertThat(LogEventTemplateReplacer.replace("${sourceMethodName}", event), is("sourcemethod"));
        assertThat(LogEventTemplateReplacer.replace("${threadName}", event), is("threadname"));

        assertThat(LogEventTemplateReplacer.replace("Hello ${threadName} message ${message} test", event), is("Hello threadname message m test"));


    }

}