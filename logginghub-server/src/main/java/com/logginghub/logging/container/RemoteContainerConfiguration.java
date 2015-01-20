package com.logginghub.logging.container;

import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(RemoteContainer.class)
public class RemoteContainerConfiguration {

    @XmlAttribute private int port = VLPorts.getContainerDefaultPort();

    public int getPort() {
        return port;
         
    }

}
