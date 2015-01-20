package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.BinaryWriterModule;
import com.logginghub.utils.module.Configures;

@Configures(BinaryWriterModule.class) @XmlAccessorType(XmlAccessType.FIELD)public class BinaryWriterConfiguration {

    @XmlAttribute private String fileDuration = "10 minutes";
    @XmlAttribute private String folder = "binarylogs/";
    @XmlAttribute private String filename = "hub.binary";
    @XmlAttribute private String eventSourceRef;
    
    public String getFolder() {
        return folder;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFileDuration(String fileDuration) {
        this.fileDuration = fileDuration;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
        
    public String getFileDuration() {
        return fileDuration;
    }

    public String getEventSourceRef() {
        return eventSourceRef;
    }
    
    public void setEventSourceRef(String eventSourceRef) {
        this.eventSourceRef = eventSourceRef;
    }

}
