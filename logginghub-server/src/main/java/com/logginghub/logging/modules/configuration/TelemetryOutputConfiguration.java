package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.TelemetryOutputModule;
import com.logginghub.utils.module.Configures;

@Configures(TelemetryOutputModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class TelemetryOutputConfiguration {

    @XmlAttribute String eventDestinationRef;
    
    public void setEventDestinationRef(String eventDestinationRef) {
        this.eventDestinationRef = eventDestinationRef;
    }
    
    public String getEventDestinationRef() {
        return eventDestinationRef;
    }

}
