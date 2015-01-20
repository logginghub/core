package com.logginghub.logging.transaction.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.transaction.TransactionMonitorModule;
import com.logginghub.utils.module.Configures;

@Configures(TransactionMonitorModule.class) 
@XmlAccessorType(XmlAccessType.FIELD) public class TransactionMonitorConfiguration {

    @XmlElement List<StateEngineConfiguration> stateEngine = new ArrayList<StateEngineConfiguration>();
    @XmlAttribute String source;

    public List<StateEngineConfiguration> getEngines() {
        return stateEngine;
    }

    public String getSource() {
        return source;
    }

}
