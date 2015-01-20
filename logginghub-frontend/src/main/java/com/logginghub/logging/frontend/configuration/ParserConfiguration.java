package com.logginghub.logging.frontend.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD) public class ParserConfiguration {
    
    @XmlElement(name = "format") private ParserFormatConfiguration formatConfiguration = new ParserFormatConfiguration();
    
    @XmlElementWrapper(name = "patterns") @XmlElement(name = "pattern") private List<PatternConfiguration> patterns = new ArrayList<PatternConfiguration>();

    public ParserFormatConfiguration getFormatConfiguration() {
        return formatConfiguration;
    }
    
    public List<PatternConfiguration> getPatterns() {
        return patterns;
    }
    
    
    public void setFormatConfiguration(ParserFormatConfiguration formatConfiguration) {
        this.formatConfiguration = formatConfiguration;
    }
    
    public void setPatterns(List<PatternConfiguration> patterns) {
        this.patterns = patterns;
    }

    @Override public String toString() {
        return "ParserConfiguration [formatConfiguration=" + formatConfiguration + ", patterns=" + patterns + "]";
    }
    
    
    
}
