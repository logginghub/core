package com.logginghub.logging.repository.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import com.logginghub.utils.ResourceUtils;

/**
 * Documentation at http://www.vertexlabs.co.uk:8111/wiki/view/loggingrepository
 * @author James
 *
 */
@SuppressWarnings("restriction") @XmlRootElement  @XmlAccessorType(XmlAccessType.FIELD)  public class LocalDiskRepositoryConfiguration {
    
    @XmlAttribute private long fileDurationMilliseconds = 10 * 1000;
    @XmlAttribute private String dataFolder = "logdata/";
    @XmlAttribute private String hubConnectionString = "localhost:58770,localhost:58770";
    @XmlAttribute private boolean overrideEventTime = false;
    @XmlAttribute private String prefix = "";

    public void setOverrideEventTime(boolean overrideEventTime) {
        this.overrideEventTime = overrideEventTime;
    }
     
    public boolean getOverrideEventTime() {
        return overrideEventTime;
    }
    
    public void setHubConnectionString(String hubConnectionString) {
        this.hubConnectionString = hubConnectionString;
    }
    
    public String getHubConnectionString() {
        return hubConnectionString;
    }
    
    public long getFileDurationMilliseconds() {
        return fileDurationMilliseconds;
    }

    public void setFileDurationMilliseconds(long fileDurationMilliseconds) {
        this.fileDurationMilliseconds = fileDurationMilliseconds;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }
    
    public static LocalDiskRepositoryConfiguration loadConfiguration(String configurationPath) {
        try {
            InputStream resource = ResourceUtils.openStream(configurationPath);
            JAXBContext context = JAXBContext.newInstance(LocalDiskRepositoryConfiguration.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(new DefaultValidationEventHandler() {
                @Override public boolean handleEvent(ValidationEvent event) {
             
                    System.out.println(event);
                    
                    return super.handleEvent(event);
                }
            });
            LocalDiskRepositoryConfiguration configuration = (LocalDiskRepositoryConfiguration) um.unmarshal(new InputStreamReader(resource));
            return configuration;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from " + configurationPath, e);
        }
    }

    public void setPrefix(String name) {
        this.prefix = name;
    }
    
    public String getPrefix() {
        return prefix;
    }

}
