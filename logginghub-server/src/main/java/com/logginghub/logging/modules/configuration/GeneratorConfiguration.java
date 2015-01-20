package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.modules.GeneratorModule;
import com.logginghub.utils.module.Configures;

@Configures(GeneratorModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class GeneratorConfiguration {

//    @XmlAttribute private int rateMin = 10;
//    @XmlAttribute private int rateMax = 20;
//    @XmlAttribute private int trendMin = 10;
//    @XmlAttribute private int trendMax = 20;
//    @XmlAttribute private boolean random = false;
    
//    @XmlElement private LogEventTemplateConfiguration template = new LogEventTemplateConfiguration();
    @XmlAttribute private String destination;
    
    @XmlElement private List<GeneratorMessageConfiguration> message = new ArrayList<GeneratorMessageConfiguration>();
    

    public List<GeneratorMessageConfiguration> getMessages() {
        return message;
    }
    
    public void setMessages(List<GeneratorMessageConfiguration> message) {
        this.message = message;
    }
    

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    
}
