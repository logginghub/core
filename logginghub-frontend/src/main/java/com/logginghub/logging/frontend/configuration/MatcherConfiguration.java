package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class MatcherConfiguration {

    @XmlAttribute private String value;
    @XmlAttribute private String legend;
    
    public String getValue() {
        return value;
    }
    
    public String getLegend() {
        return legend;
    }
    
    public void setLegend(String legend) {
        this.legend = legend;
    }
    
    public void setValue(String value) {
        this.value = value;
    }    
}
