package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class EntryStateConfiguration {

    @XmlElement StateCaptureConfiguration capture;
    
    public StateCaptureConfiguration getEventCapture() {
        return capture;
    }
    
}
