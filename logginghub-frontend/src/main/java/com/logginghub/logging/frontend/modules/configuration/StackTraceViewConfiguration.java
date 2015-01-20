package com.logginghub.logging.frontend.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.frontend.modules.StackTraceViewModule;
import com.logginghub.utils.module.Configures;

@Configures(StackTraceViewModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class StackTraceViewConfiguration {

    @XmlAttribute String environmentRef; 
    @XmlAttribute String layout;

    public void setEnvironmentRef(String environmentRef) {
        this.environmentRef = environmentRef;
    }
    
    public String getEnvironmentRef() {
        return environmentRef;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    public String getLayout() {
        return layout;
         
    }

}
