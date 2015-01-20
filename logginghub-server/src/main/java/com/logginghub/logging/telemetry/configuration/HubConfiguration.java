package com.logginghub.logging.telemetry.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.utils.VLPorts;

@XmlAccessorType(XmlAccessType.FIELD) public class HubConfiguration {
    @XmlAttribute private String host;
    @XmlAttribute private int port = VLPorts.getSocketHubDefaultPort();
    @XmlAttribute private String name;

    public HubConfiguration() {
    }
    
    public HubConfiguration(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
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
        return "HubConfiguration [host=" + host + ", port=" + port + "]";
    }

}
