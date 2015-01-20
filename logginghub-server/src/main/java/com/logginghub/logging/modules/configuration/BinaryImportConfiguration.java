package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.BinaryImporterModule;
import com.logginghub.utils.module.Configures;

@Configures(BinaryImporterModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class BinaryImportConfiguration {

    @XmlAttribute private String file = "hub.binary";
    @XmlAttribute private String destinationRef;
    @XmlAttribute private boolean outputStats = true;

    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }
    
    public boolean isOutputStats() {
        return outputStats;
    }
    
    public void setDestinationRef(String destinationRef) {
        this.destinationRef = destinationRef;
    }

    public String getDestinationRef() {
        return destinationRef;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

}
