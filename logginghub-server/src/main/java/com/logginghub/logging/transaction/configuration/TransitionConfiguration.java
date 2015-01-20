package com.logginghub.logging.transaction.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class TransitionConfiguration {
    @XmlElement List<TransitionConfiguration> transition = new ArrayList<TransitionConfiguration>();
    @XmlAttribute String timeout = "10 seconds";
    @XmlAttribute String state;

    public TransitionConfiguration(String state) {
        this.state = state;
    }
    
    public TransitionConfiguration(String state, String timeout) {
        this.state = state;
        this.timeout  = timeout;
    }

    public TransitionConfiguration() {}
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<TransitionConfiguration> getTransitions() {
        return transition;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

}
