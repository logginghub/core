package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.WebFrontendModule;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(WebFrontendModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class WebFrontendConfiguration {

    @XmlAttribute private int port = VLPorts.getWebFrontendPort();

    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
}
