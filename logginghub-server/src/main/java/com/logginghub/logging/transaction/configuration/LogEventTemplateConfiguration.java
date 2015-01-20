package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.logging.Logger;

@XmlAccessorType(XmlAccessType.FIELD) public class LogEventTemplateConfiguration {

    // jshaw - this is being used in multiple places now, so dont put anything specific in here
    @XmlAttribute private String level = "info";
    @XmlAttribute private String sourceClassName = "";
    @XmlAttribute private String sourceMethodName = "";
    @XmlAttribute private String message = "Log event template default message";
    @XmlAttribute private String threadName = "";
    @XmlAttribute private String loggerName = "";
    @XmlAttribute private String sourceHost = NetUtils.getLocalHostname();
    @XmlAttribute private String sourceAddress = NetUtils.getLocalIP();
    @XmlAttribute private String sourceApplication = "";
    @XmlAttribute private String channel = "events";

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public DefaultLogEvent createEvent() {
        DefaultLogEvent event = LogEventBuilder.start()
                        .setChannel(channel)
                        .setLevel(Logger.parseLevel(level))
                        .setMessage(message)
                        .setSourceAddress(sourceAddress)
                        .setSourceHost(sourceHost)
                        .setSourceApplication(sourceApplication)
                        .setSourceClassName(sourceClassName)
                        .setSourceMethodName(sourceMethodName)
                        .setThreadName(threadName)
                        .setLoggerName(loggerName)
                        .toLogEvent();
        return event;
    }

}
