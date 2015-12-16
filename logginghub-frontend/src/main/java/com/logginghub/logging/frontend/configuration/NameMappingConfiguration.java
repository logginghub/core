package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * XML configuration mapping object for being able to rename things (column names, level names etc)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NameMappingConfiguration {
    @XmlAttribute private String from;
    @XmlAttribute private String to;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}

