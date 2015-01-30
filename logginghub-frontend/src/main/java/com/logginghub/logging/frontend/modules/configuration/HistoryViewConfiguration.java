package com.logginghub.logging.frontend.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.frontend.views.historical.HistoryViewModule;
import com.logginghub.utils.module.Configures;

@Configures(HistoryViewModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class HistoryViewConfiguration extends ComponentConfiguration {

    @XmlAttribute String environmentRef;
    
    public String getEnvironmentRef() {
        return environmentRef;
    }
    
    public void setEnvironmentRef(String environmentRef) {
        this.environmentRef = environmentRef;
    }
}
