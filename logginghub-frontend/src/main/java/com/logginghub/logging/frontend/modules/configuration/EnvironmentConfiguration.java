package com.logginghub.logging.frontend.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.frontend.modules.EnvironmentModule;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.utils.module.Configures;

@Configures(EnvironmentModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class EnvironmentConfiguration {
    
    @XmlAttribute String name = "environment";
    @XmlElement private List<HubConfiguration> hub = new ArrayList<HubConfiguration>();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public List<HubConfiguration> getHubs() {
        return hub;
    }
}
