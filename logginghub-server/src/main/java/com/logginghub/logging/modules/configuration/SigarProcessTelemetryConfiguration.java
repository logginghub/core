package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.SigarProcessTelemetryModule;
import com.logginghub.utils.module.Configures;

@Configures(SigarProcessTelemetryModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class SigarProcessTelemetryConfiguration {
    @XmlAttribute private String interval = "30 seconds";
    @XmlAttribute private String destination = null;
    @XmlAttribute private String prefix = "Sigar Process - ";
    @XmlAttribute private String channel = "telemetry/sigar/process";
    
    public String getInterval() {
        return interval;
    }

    @Override public String toString() {
        return "SigarProcessTelemetryConfiguration [interval=" + interval + "]";
    }

    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getChannel() {
        return channel;
    }

    public String getPrefix() {
        return prefix;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    
}
