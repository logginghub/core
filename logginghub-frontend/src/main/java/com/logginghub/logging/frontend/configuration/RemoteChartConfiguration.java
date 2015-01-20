package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author James
 *
 */
@XmlAccessorType(XmlAccessType.FIELD) public class RemoteChartConfiguration {
    
    @XmlAttribute private String filename = "remote.charting.xml";
    @XmlAttribute private String name = "Remote charting";
    @XmlAttribute private String environmentRef;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEnvironmentRef() {
        return environmentRef;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setEnvironmentRef(String environmentRef) {
        this.environmentRef = environmentRef;
    }
    
     public void setFilename(String filename) {
        this.filename = filename;
    }

}
