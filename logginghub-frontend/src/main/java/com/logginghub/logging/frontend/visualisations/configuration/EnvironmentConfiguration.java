package com.logginghub.logging.frontend.visualisations.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.frontend.configuration.HubConfiguration;

@XmlAccessorType(XmlAccessType.FIELD) public class EnvironmentConfiguration {
    
    @XmlElement List<PatternConfiguration> pattern = new ArrayList<PatternConfiguration>();
    @XmlElement List<HubConfiguration> hub = new ArrayList<HubConfiguration>();
    @XmlAttribute private String name= "";
    
    public List<PatternConfiguration> getPatterns() {
        return pattern;
    }
    
    public List<HubConfiguration> getHubs() {
        return hub;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
