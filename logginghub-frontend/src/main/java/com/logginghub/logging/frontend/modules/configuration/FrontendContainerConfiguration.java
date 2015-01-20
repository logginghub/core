package com.logginghub.logging.frontend.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.utils.JAXBConfiguration;

@XmlRootElement(name = "container") @XmlAccessorType(XmlAccessType.FIELD) public class FrontendContainerConfiguration {

    @XmlElement private List<EnvironmentControllerConfiguration> environmentController = new ArrayList<EnvironmentControllerConfiguration>();
    @XmlElement private List<EnvironmentConfiguration> environment= new ArrayList<EnvironmentConfiguration>();
    
    @XmlElement private List<FrontendContainerConfiguration> container = new ArrayList<FrontendContainerConfiguration>();
    @XmlElement private List<MainFrameConfiguration> mainFrame = new ArrayList<MainFrameConfiguration>();
    
    public List<FrontendContainerConfiguration> getContainers() {
        return container;
    }

    public List<MainFrameConfiguration> getMainFrames() {
        return mainFrame;
    }
    
    public List<EnvironmentControllerConfiguration> getEnvironmentControllers() {
        return environmentController;
    }
    
    public List<EnvironmentConfiguration> getEnvironments() {
        return environment;
    }
    
//    @XmlElement private List<TransactionMonitorConfiguration> transactionMonitor = new ArrayList<TransactionMonitorConfiguration>();
//    @XmlElement private List<HubConnectorConfiguration> hubConnector = new ArrayList<HubConnectorConfiguration>();
//    @XmlElement private List<EmailConnectorConfiguration> emailConnector = new ArrayList<EmailConnectorConfiguration>();
//    @XmlElement private List<SimulatorConfiguration> simulator = new ArrayList<SimulatorConfiguration>();
//
//    @XmlElement private List<RollingFileLoggerConfiguration> rollingFileLogger = new ArrayList<RollingFileLoggerConfiguration>();
//    @XmlElement private List<TimestampVariableRollingFileLoggerConfiguration> timestampVariableRollingFileLogger = new ArrayList<TimestampVariableRollingFileLoggerConfiguration>();
//    @XmlElement private List<TimestampFixedRollingFileLoggerConfiguration> timestampFixedRollingFileLogger = new ArrayList<TimestampFixedRollingFileLoggerConfiguration>();
//
//    @XmlElement private List<AggregatorConfiguration> aggregator = new ArrayList<AggregatorConfiguration>();
//    @XmlElement private List<PatterniserConfiguration> patterniser = new ArrayList<PatterniserConfiguration>();
//    
//    @XmlElement List<GeneratorConfiguration> generator = new ArrayList<GeneratorConfiguration>();
//    
//    public List<AggregatorConfiguration> getAggregators() {
//        return aggregator;
//    }
//    
//    public List<PatterniserConfiguration> getPatternisers() {
//        return patterniser;
//    }
//    
//    public List<GeneratorConfiguration> getGenerators() {
//        return generator;
//    }
//    
//    public List<FrontendContainerConfiguration> getContainers() {
//        return container;
//    }
//
//    public List<TransactionMonitorConfiguration> getTransactionMonitors() {
//        return transactionMonitor;
//    }
//
//    public List<HubConnectorConfiguration> getHubConnectors() {
//        return hubConnector;
//    }
//
//    public List<EmailConnectorConfiguration> getEmailConnectors() {
//        return emailConnector;
//    }
//
//    public List<SimulatorConfiguration> getSimulators() {
//        return simulator;
//    }
//
//    public List<RollingFileLoggerConfiguration> getRollingFileLoggers() {
//        return rollingFileLogger;
//    }
//
//    public List<TimestampVariableRollingFileLoggerConfiguration> getTimestampVariableRollingFileLoggers() {
//        return timestampVariableRollingFileLogger;
//    }
//
//    public List<TimestampFixedRollingFileLoggerConfiguration> getTimestampFixedRollingFileLoggers() {
//        return timestampFixedRollingFileLogger;
//    }

    public static FrontendContainerConfiguration fromString(String instructions) {
        return JAXBConfiguration.loadConfigurationFromString(FrontendContainerConfiguration.class, instructions);
    }

    public static FrontendContainerConfiguration fromResource(String resource) {
        return JAXBConfiguration.loadConfiguration(FrontendContainerConfiguration.class, resource);
    }

}
