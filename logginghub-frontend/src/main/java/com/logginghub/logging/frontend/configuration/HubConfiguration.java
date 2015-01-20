package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.utils.VLPorts;

@XmlAccessorType(XmlAccessType.FIELD) public class HubConfiguration {
    @XmlAttribute private String name;
    @XmlAttribute private String host;
    @XmlAttribute private int port = VLPorts.getSocketHubDefaultPort();
    @XmlAttribute private boolean overrideTime = false;
    @XmlAttribute private boolean debug = false;
    @XmlAttribute private String channel;

    public HubConfiguration() {}

    public HubConfiguration(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public HubConfiguration(String name) {
        this.name = name;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return channel;
    }

    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public boolean getOverrideTime() {
        return overrideTime;
    }

    public void setOverrideTime(boolean overrideTime) {
        this.overrideTime = overrideTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override public String toString() {
        return "HubConfiguration [name=" + name + ", host=" + host + ", port=" + port + "]";
    }

}
