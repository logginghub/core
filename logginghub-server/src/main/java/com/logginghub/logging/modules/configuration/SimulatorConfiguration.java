package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.generator.nextgen.Simulator;
import com.logginghub.utils.module.Configures;

@Configures(Simulator.class) @XmlAccessorType(XmlAccessType.FIELD) public class SimulatorConfiguration {
    @XmlAttribute String eventDestination;

    public String getEventDestination() {
        return eventDestination;
    }

    public void setEventDestination(String eventDestination) {
        this.eventDestination = eventDestination;
    }
}
