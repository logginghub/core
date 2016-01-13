package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by james on 12/01/2016.
 */
@XmlAccessorType(XmlAccessType.FIELD)  public class KeyValueConfiguration {
    @XmlAttribute private String key = null;
    @XmlAttribute private String value = null;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
