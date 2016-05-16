package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Configuration encapsulation for an argument, potentially to a command line process or Action.
 */
@XmlAccessorType(XmlAccessType.FIELD) public class ArgumentConfiguration {
    @XmlAttribute private String value;

    public String getValue() {
        return value;
    }
}
