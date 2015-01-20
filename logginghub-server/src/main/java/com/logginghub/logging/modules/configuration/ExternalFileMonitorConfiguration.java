package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.ExternalFileMonitorModule;
import com.logginghub.utils.module.Configures;

@Configures(ExternalFileMonitorModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class ExternalFileMonitorConfiguration {
    @XmlAttribute private String path;
    @XmlAttribute private String prefix = "";
    @XmlAttribute private String postfix = "";
    @XmlAttribute private String level = "info";
    @XmlAttribute private String channel;
    @XmlAttribute private boolean replayAll = false;
    @XmlAttribute private String destination = null;
    
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isReplayAll() {
        return replayAll;
    }

    public void setReplayAll(boolean replayAll) {
        this.replayAll = replayAll;
    }

    public String getChannel() {
        return channel;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    @Override public String toString() {
        return "ExternalFileConfiguration [path=" + path + ", prefix=" + prefix + ", postfix=" + postfix + "]";
    }

    public String getDestination() {
        return destination;
         
    }

}
