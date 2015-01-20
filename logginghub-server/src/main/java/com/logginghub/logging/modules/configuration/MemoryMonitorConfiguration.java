package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.MemoryMonitorModule;
import com.logginghub.utils.module.Configures;

@Configures(MemoryMonitorModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class MemoryMonitorConfiguration {

    @XmlAttribute private int threshold = 95;

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
    
    public int getThreshold() {
        return threshold;
         
    }

}
