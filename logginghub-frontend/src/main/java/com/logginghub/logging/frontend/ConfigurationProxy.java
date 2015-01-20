package com.logginghub.logging.frontend;

import java.io.File;

import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.utils.Metadata;

/**
 * Refactoring proxy class to try and wrap up all aspects of configuration and
 * model access behind one facade to enable smooth refactoring
 * 
 * @author James
 * 
 */
public class ConfigurationProxy {

    private Metadata dynamicSettings;
    private String propertiesName;
    private LoggingFrontendConfiguration loggingFrontendConfiguration;
    private String parsersLocation;

    /**
     * This represents the first ton of refactors...
     * @param settings
     * @param xmlConfiguration
     * @param newConfigurationModel
     * @param propertiesName
     * @param loggingFrontendConfiguration
     * @param configurationFile 
     */
    public ConfigurationProxy(Metadata settings,
                              String propertiesName,
                              LoggingFrontendConfiguration loggingFrontendConfiguration, String parsersLocation) {
        this.dynamicSettings = settings;
        this.propertiesName = propertiesName;
        this.loggingFrontendConfiguration = loggingFrontendConfiguration;
        this.parsersLocation = parsersLocation;
    }

    public ConfigurationProxy(Metadata metadata, LoggingFrontendConfiguration configuration) {
        this.dynamicSettings = metadata;
        this.loggingFrontendConfiguration = configuration;
        
    }
    

    public String getParsersLocation() {
        return parsersLocation;
    }
    
    public LoggingFrontendConfiguration getLoggingFrontendConfiguration() {
        return loggingFrontendConfiguration;
    }

//    public XmlConfigurationModel getNewConfigurationModel() {
//        return newConfigurationModel;
//    }

    public String getPropertiesName() {
        return propertiesName;
    }

    public Metadata getDynamicSettings() {
        return dynamicSettings;
    }

    @Override public String toString() {
        return "ConfigurationProxy [dynamicSettings=" + dynamicSettings + ", propertiesName=" + propertiesName + ", loggingFrontendConfiguration=" + loggingFrontendConfiguration + "]";
    }
    
    
    
}
