package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class VariableConfiguration {
    
    @XmlAttribute private String name;
    @XmlAttribute private String values;
    @XmlAttribute private String type;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getValues() {
        return values;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setValues(String values) {
        this.values = values;
    }

}
