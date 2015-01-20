package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@SuppressWarnings("restriction") @XmlAccessorType(XmlAccessType.FIELD) public class BadEventsReportConfiguration {

    @XmlAttribute private String name;   
    @XmlElement(name="rollupRegex") private List<String> rollupRegexs = new ArrayList<String>();
    
    public String getName() {
        return name;
    }
    
    public List<String> getRollupRegexs() {
        return rollupRegexs;
    }
}
