package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class ChunkerConfiguration {
    
    @XmlElement int interval = 1000;
    @XmlElement(name="parser") List<ParserConfiguration> parserConfigurations = new ArrayList<ParserConfiguration>();

    public List<ParserConfiguration> getParserConfigurations() {
        return parserConfigurations;
    }
    
    public void setParserConfigurations(List<ParserConfiguration> parserConfigurations) {
        this.parserConfigurations = parserConfigurations;
    }
    
    public void setInterval(int interval) {
        this.interval = interval;
    }
    
    public int getInterval() {
        return interval;
    }

    @Override public String toString() {
        return "ChunkerConfiguration [interval=" + interval + ", parserConfigurations=" + parserConfigurations + "]";
    }
    
    
}
