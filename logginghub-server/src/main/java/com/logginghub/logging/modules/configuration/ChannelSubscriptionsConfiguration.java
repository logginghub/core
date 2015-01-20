package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.ChannelSubscriptionsModule;
import com.logginghub.utils.module.Configures;

@Configures(ChannelSubscriptionsModule.class)  @XmlAccessorType(XmlAccessType.FIELD) public class ChannelSubscriptionsConfiguration {

    @XmlAttribute private String messageSourceRef;

    public String getMessageSourceRef() {
        return messageSourceRef;
    }
    
    public void setMessageSourceRef(String messageSourceRef) {
        this.messageSourceRef = messageSourceRef;
    }
    
}
