package com.logginghub.logging.transaction.configuration;

import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.SocketHubConfiguration;
import com.logginghub.logging.hub.configuration.TimestampFixedRollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.modules.configuration.AggregatedDiskHistoryConfiguration;
import com.logginghub.logging.modules.configuration.AggregatorConfiguration;
import com.logginghub.logging.modules.configuration.BinaryImportConfiguration;
import com.logginghub.logging.modules.configuration.BinaryProcessorConfiguration;
import com.logginghub.logging.modules.configuration.BinaryWriterConfiguration;
import com.logginghub.logging.modules.configuration.ChannelSubscriptionsConfiguration;
import com.logginghub.logging.modules.configuration.DiskHistoryConfiguration;
import com.logginghub.logging.modules.configuration.DiskHistoryIndexConfiguration;
import com.logginghub.logging.modules.configuration.DiskValidationConfiguration;
import com.logginghub.logging.modules.configuration.ExternalFileMonitorConfiguration;
import com.logginghub.logging.modules.configuration.ExternalProcessMonitorConfiguration;
import com.logginghub.logging.modules.configuration.GeneratorConfiguration;
import com.logginghub.logging.modules.configuration.IOStatMonitorConfiguration;
import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
import com.logginghub.logging.modules.configuration.InternalLoggingConfiguration;
import com.logginghub.logging.modules.configuration.LoggingBridgeConfiguration;
import com.logginghub.logging.modules.configuration.MemoryMonitorConfiguration;
import com.logginghub.logging.modules.configuration.PatternManagerConfiguration;
import com.logginghub.logging.modules.configuration.PatternisedDiskHistoryConfiguration;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.logging.modules.configuration.RestfulListenerConfiguration;
import com.logginghub.logging.modules.configuration.SQLExtractConfiguration;
import com.logginghub.logging.modules.configuration.SigarMachineTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.SigarProcessTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.SimulatorConfiguration;
import com.logginghub.logging.modules.configuration.SocketTextReaderConfiguration;
import com.logginghub.logging.modules.configuration.StackHistoryConfiguration;
import com.logginghub.logging.modules.configuration.TelemetryConfiguration;
import com.logginghub.logging.modules.configuration.TelemetryOutputConfiguration;
import com.logginghub.logging.modules.configuration.VMStatMonitorConfiguration;
import com.logginghub.logging.modules.configuration.WebFrontendConfiguration;
import com.logginghub.logging.modules.configuration.ZeroCopyHubConfiguration;
import com.logginghub.utils.JAXBConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "container") @XmlAccessorType(XmlAccessType.FIELD) public class LoggingContainerConfiguration {

    @XmlElement private List<LoggingContainerConfiguration> container = new ArrayList<LoggingContainerConfiguration>();
    
    
    @XmlElement private List<LoggingBridgeConfiguration> bridge = new ArrayList<LoggingBridgeConfiguration>();
    
    @XmlElement private List<SocketTextReaderConfiguration> socketTextReader = new ArrayList<SocketTextReaderConfiguration>();
    
    @XmlElement private List<MemoryMonitorConfiguration> memoryMonitor = new ArrayList<MemoryMonitorConfiguration>();
    
    @XmlElement private List<RestfulListenerConfiguration> restfulListener = new ArrayList<RestfulListenerConfiguration>();
    
    @XmlElement private List<TelemetryConfiguration> telemetry = new ArrayList<TelemetryConfiguration>();
    @XmlElement private List<TelemetryOutputConfiguration> telemetryOutput = new ArrayList<TelemetryOutputConfiguration>();
    
    @XmlElement private List<InternalLoggingConfiguration> internalLogging = new ArrayList<InternalLoggingConfiguration>();

    @XmlElement private List<StackHistoryConfiguration> stackHistory = new ArrayList<StackHistoryConfiguration>();
    
    @XmlElement private List<TransactionMonitorConfiguration> transactionMonitor = new ArrayList<TransactionMonitorConfiguration>();
    @XmlElement private List<HubConnectorConfiguration> hubConnector = new ArrayList<HubConnectorConfiguration>();
    @XmlElement private List<EmailConnectorConfiguration> emailConnector = new ArrayList<EmailConnectorConfiguration>();
    @XmlElement private List<SimulatorConfiguration> simulator = new ArrayList<SimulatorConfiguration>();

    @XmlElement private List<RollingFileLoggerConfiguration> rollingFileLogger = new ArrayList<RollingFileLoggerConfiguration>();
    @XmlElement private List<TimestampVariableRollingFileLoggerConfiguration> timestampVariableRollingFileLogger = new ArrayList<TimestampVariableRollingFileLoggerConfiguration>();
    @XmlElement private List<TimestampFixedRollingFileLoggerConfiguration> timestampFixedRollingFileLogger = new ArrayList<TimestampFixedRollingFileLoggerConfiguration>();

    @XmlElement private List<AggregatorConfiguration> aggregator = new ArrayList<AggregatorConfiguration>();
    @XmlElement private List<PatterniserConfiguration> patterniser = new ArrayList<PatterniserConfiguration>();
    
    @XmlElement private List<InMemoryHistoryConfiguration> inMemoryHistory = new ArrayList<InMemoryHistoryConfiguration>();
    @XmlElement private List<ChannelSubscriptionsConfiguration> channelSubscriptions = new ArrayList<ChannelSubscriptionsConfiguration>();
    @XmlElement private List<BinaryImportConfiguration> binaryImport = new ArrayList<BinaryImportConfiguration>();
    @XmlElement private List<BinaryWriterConfiguration> binaryWriter = new ArrayList<BinaryWriterConfiguration>();
    
    @XmlElement private List<BinaryProcessorConfiguration> binaryProcessor = new ArrayList<BinaryProcessorConfiguration>();
    @XmlElement private List<ZeroCopyHubConfiguration> zeroCopyHub = new ArrayList<ZeroCopyHubConfiguration>();
    
    @XmlElement private List<DiskHistoryConfiguration> diskHistory = new ArrayList<DiskHistoryConfiguration>();
    @XmlElement private List<DiskHistoryIndexConfiguration> diskHistoryIndex = new ArrayList<DiskHistoryIndexConfiguration>();
    @XmlElement private List<DiskValidationConfiguration> diskValidation = new ArrayList<DiskValidationConfiguration>();
    
    @XmlElement private List<PatternisedDiskHistoryConfiguration> patternisedDiskHistory = new ArrayList<PatternisedDiskHistoryConfiguration>();
    @XmlElement private List<AggregatedDiskHistoryConfiguration> aggregatedDiskHistory = new ArrayList<AggregatedDiskHistoryConfiguration>();
    
    @XmlElement private List<SocketHubConfiguration> socketHub = new ArrayList<SocketHubConfiguration>();
    
    @XmlElement private List<HubStackCaptureConfiguration> stackCapture = new ArrayList<HubStackCaptureConfiguration>();
    
    @XmlElement private List<SQLExtractConfiguration> sqlExtract = new ArrayList<SQLExtractConfiguration>();
    
    @XmlElement private List<WebFrontendConfiguration> webFrontend = new ArrayList<WebFrontendConfiguration>();
    
    @XmlElement private List<PatternManagerConfiguration> patternManager = new ArrayList<PatternManagerConfiguration>();
    
    @XmlElement private List<VMStatMonitorConfiguration>  vmstatMonitor = new ArrayList<VMStatMonitorConfiguration>();
    @XmlElement private List<IOStatMonitorConfiguration>  iostatMonitor = new ArrayList<IOStatMonitorConfiguration>();
    
    @XmlElement private List<ExternalFileMonitorConfiguration>  externalFileMonitor = new ArrayList<ExternalFileMonitorConfiguration>();
    @XmlElement private List<ExternalProcessMonitorConfiguration>  externalProcessMonitor = new ArrayList<ExternalProcessMonitorConfiguration>();
    
    @XmlElement private List<SigarProcessTelemetryConfiguration> sigarProcessTelemetryModule= new ArrayList<SigarProcessTelemetryConfiguration>();
    @XmlElement private List<SigarMachineTelemetryConfiguration> sigarMachineTelemetryModule= new ArrayList<SigarMachineTelemetryConfiguration>();
    
    
    @XmlElement List<GeneratorConfiguration> generator = new ArrayList<GeneratorConfiguration>();
    
    public List<BinaryProcessorConfiguration> getBinaryProcessors() {
        return binaryProcessor;
    }
    
    public List<BinaryWriterConfiguration> getBinaryWriters() {
        return binaryWriter;
    }
    
    public List<LoggingBridgeConfiguration> getBridges() {
        return bridge;
    }
    
    public List<SocketTextReaderConfiguration> getSocketTextReaders() {
        return socketTextReader;
    }
    
    public List<RestfulListenerConfiguration> getRestfulListeners() {
        return restfulListener;
    }
    
    public List<TelemetryOutputConfiguration> getTelemetryOutput() {
        return telemetryOutput;
    }
    
    public List<TelemetryConfiguration> getTelemetry() {
        return telemetry;
    }
    
    public List<HubStackCaptureConfiguration> getStackCapture() {
        return stackCapture;
    }
    
    public List<SocketHubConfiguration> getSocketHubs() {
        return socketHub;
    }
    
    public List<ChannelSubscriptionsConfiguration> getChannelSubscriptions() {
        return channelSubscriptions;
    }
    
    public List<BinaryImportConfiguration> getBinaryImport() {
        return binaryImport;
    }
    
    public List<InMemoryHistoryConfiguration> getInMemoryHistories() {
        return inMemoryHistory;
    }
    
    public List<AggregatorConfiguration> getAggregators() {
        return aggregator;
    }
    
    public List<PatterniserConfiguration> getPatternisers() {
        return patterniser;
    }
    
    public List<GeneratorConfiguration> getGenerators() {
        return generator;
    }
    
    public List<LoggingContainerConfiguration> getContainers() {
        return container;
    }

    public List<TransactionMonitorConfiguration> getTransactionMonitors() {
        return transactionMonitor;
    }

    public List<HubConnectorConfiguration> getHubConnectors() {
        return hubConnector;
    }

    public List<EmailConnectorConfiguration> getEmailConnectors() {
        return emailConnector;
    }

    public List<SimulatorConfiguration> getSimulators() {
        return simulator;
    }

    public List<RollingFileLoggerConfiguration> getRollingFileLoggers() {
        return rollingFileLogger;
    }

    public List<TimestampVariableRollingFileLoggerConfiguration> getTimestampVariableRollingFileLoggers() {
        return timestampVariableRollingFileLogger;
    }

    public List<TimestampFixedRollingFileLoggerConfiguration> getTimestampFixedRollingFileLoggers() {
        return timestampFixedRollingFileLogger;
    }
    
    public List<MemoryMonitorConfiguration> getMemoryMonitors() {
        return memoryMonitor;
    }

    public static LoggingContainerConfiguration fromString(String instructions) {
        return JAXBConfiguration.loadConfigurationFromString(LoggingContainerConfiguration.class, instructions);
    }

    public List<DiskHistoryConfiguration> getDiskHistories() {
        return diskHistory;
    }
    
    public static LoggingContainerConfiguration fromResource(String resource) {
        return JAXBConfiguration.loadConfiguration(LoggingContainerConfiguration.class, resource);
    }

    public List<DiskHistoryIndexConfiguration> getDiskHistoryIndex() {
        return diskHistoryIndex;
    }

    public List<DiskValidationConfiguration> getDiskValidations() {
        return diskValidation;
    }
    
    public List<SQLExtractConfiguration> getSqlExtract() {
        return sqlExtract;
    }
    
    public List<WebFrontendConfiguration> getWebFrontends() {
        return webFrontend;
    }
    
    public List<ZeroCopyHubConfiguration> getZeroCopyHubs() {
        return zeroCopyHub;
    }
    
    public List<PatternManagerConfiguration> getPatternManagers() {
        return patternManager;
    }
    
    public List<InternalLoggingConfiguration> getInternalLogging() {
        return internalLogging;
    }
    
    public List<PatternisedDiskHistoryConfiguration> getPatternisedDiskHistory() {
        return patternisedDiskHistory;
    }
    
    public List<AggregatedDiskHistoryConfiguration> getAggregatedDiskHistory() {
        return aggregatedDiskHistory;
    }
    
    public List<VMStatMonitorConfiguration> getVmstatMonitor() {
        return vmstatMonitor;
    }
    
    public List<IOStatMonitorConfiguration> getIostatMonitor() {
        return iostatMonitor;
    }
    
    public List<ExternalFileMonitorConfiguration> getExternalFileMonitor() {
        return externalFileMonitor;
    }
    
    public List<ExternalProcessMonitorConfiguration> getExternalProcessMonitor() {
        return externalProcessMonitor;
    }
    
    public List<SigarMachineTelemetryConfiguration> getSigarMachineTelemetryModule() {
        return sigarMachineTelemetryModule;
    }
    
    public List<SigarProcessTelemetryConfiguration> getSigarProcessTelemetryModule() {
        return sigarProcessTelemetryModule;
    }

    public List<StackHistoryConfiguration> getStackHistory() {
        return stackHistory;
    }
}
