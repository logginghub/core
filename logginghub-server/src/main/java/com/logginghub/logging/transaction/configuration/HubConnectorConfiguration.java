package com.logginghub.logging.transaction.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.logging.transaction.HubConnector;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(HubConnector.class) @XmlAccessorType(XmlAccessType.FIELD) public class HubConnectorConfiguration {

    @XmlElement private List<HubConfiguration> hub = new ArrayList<HubConfiguration>();
    @XmlAttribute String name = "";
    @XmlAttribute String channels = "";
    @XmlAttribute String host = "";
    @XmlAttribute int port = VLPorts.getSocketHubDefaultPort();
    @XmlAttribute boolean globalSubscription = false;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isGlobalSubscription() {
        return globalSubscription;
    }

    public void setGlobalSubscription(boolean globalSubscription) {
        this.globalSubscription = globalSubscription;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HubConfiguration> getHubs() {
        return hub;
    }

}
