package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.RestfulListenerModule;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(RestfulListenerModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class RestfulListenerConfiguration {

    @XmlAttribute private String eventDestinationRef;
    @XmlAttribute private int port = VLPorts.getRestfulListenerPort();
    @XmlAttribute private String bindAddress;

    public RestfulListenerConfiguration() {}

    public void setEventDestinationRef(String eventDestinationRef) {
        this.eventDestinationRef = eventDestinationRef;
    }

    public String getEventDestinationRef() {
        return eventDestinationRef;
    }

    public int getPort() {
        return port;         
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getBindAddress() {
        return bindAddress;
    }
    
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

}
