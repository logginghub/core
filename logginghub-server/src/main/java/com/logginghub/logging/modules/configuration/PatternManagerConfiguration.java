package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.PatternManagerModule;
import com.logginghub.utils.module.Configures;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD) @Configures(PatternManagerModule.class) public class PatternManagerConfiguration {

    @XmlAttribute String dataFile = "patterns.json";

    public PatternManagerConfiguration() {}

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

}
