package com.logginghub.logging.hub.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.modules.configuration.LoggingBridgeConfiguration;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(SocketHub.class)
@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement(name ="hubConfiguration")  public class LegacySocketHubConfiguration {

    @XmlAttribute  private int port = VLPorts.getSocketHubDefaultPort();
    @XmlAttribute  private int messaging3TelemetryPort = 0;
    @XmlAttribute  private int restfulListenerPort = 0;
    @XmlAttribute  private int maximumClientSendQueueSize = SocketConnection.writeBufferDefaultSize;
//    @XmlAttribute private boolean outputTelemetryToLoggingStream = false;
    @XmlAttribute private boolean outputStats = true;
//    @XmlElementWrapper(name="socketTextReaders") @XmlElement(name="socketTextReaderConfiguration") private List<SocketTextReaderConfiguration> socketTextReaders = new ArrayList<SocketTextReaderConfiguration>();
    @XmlElementWrapper(name = "exportBridges") @XmlElement(name="exportBridge") private List<LoggingBridgeConfiguration> exportBridges = new ArrayList<LoggingBridgeConfiguration>();
    @XmlElementWrapper(name = "importBridges") @XmlElement(name="importBridge") private List<LoggingBridgeConfiguration> importBridges = new ArrayList<LoggingBridgeConfiguration>();
    @XmlElement(name="aggregatedFileLogConfiguration") private RollingFileLoggerConfiguration aggregatedFileLogConfiguration = null; 
    @XmlElement(name="timeStampAggregatedFileLogConfiguration") private TimestampVariableRollingFileLoggerConfiguration timeStampAggregatedFileLogConfiguration = null;
//    @XmlElement TransactionMonitorConfiguration stateEngineConfiguration = null;
    
//    @XmlElement List<GeneratorConfiguration> generator = new ArrayList<GeneratorConfiguration>();
//    @XmlElement List<LoggingContainerConfiguration> container = new ArrayList<LoggingContainerConfiguration>();
    
    @XmlElement List<FilterConfiguration> filter = new ArrayList<FilterConfiguration>();
    
    public List<FilterConfiguration> getFilters() {
        return filter;
    }
    
//    public List<LoggingContainerConfiguration> getContainers() {
//        return container;
//    }
    
//    public List<GeneratorConfiguration> getGenerators() {
//        return generator;
//    }
    
//    public List<SocketTextReaderConfiguration> getSocketTextReaders() {
//        return socketTextReaders;
//    }

    public int getPort() {
        return port;
    }

//    public boolean isOutputTelemetryToLoggingStream() {
//        return outputTelemetryToLoggingStream;
//    }
    
//    public void setOutputTelemetryToLoggingStream(boolean outputTelemetryToLoggingStream) {
//        this.outputTelemetryToLoggingStream = outputTelemetryToLoggingStream;
//    }
    
    public void setMaximumClientSendQueueSize(int maximumClientSendQueueSize) {
        this.maximumClientSendQueueSize = maximumClientSendQueueSize;
    }
    
    public int getMaximumClientSendQueueSize() {
        return maximumClientSendQueueSize;
    }
    
    public RollingFileLoggerConfiguration getAggregatedFileLogConfiguration() {
        return aggregatedFileLogConfiguration;
    }

    public TimestampVariableRollingFileLoggerConfiguration getTimeStampAggregatedFileLogConfiguration() {
        return timeStampAggregatedFileLogConfiguration;
    }

    public void setPort(int port) {
        this.port = port;
    }

//    public void setSocketTextReaders(List<SocketTextReaderConfiguration> socketTextReaders) {
//        this.socketTextReaders = socketTextReaders;
//    }

    public void setAggregatedFileLogConfiguration(RollingFileLoggerConfiguration aggregatedFileLogConfiguration) {
        this.aggregatedFileLogConfiguration = aggregatedFileLogConfiguration;
    }

    public void setTimeStampAggregatedFileLogConfiguration(TimestampVariableRollingFileLoggerConfiguration timeStampAggregatedFileLogConfiguration) {
        this.timeStampAggregatedFileLogConfiguration = timeStampAggregatedFileLogConfiguration;
    }

    public void writeToFile(File file) {        
        FileUtils.ensurePathExists(file);
        JAXBConfiguration.writeConfiguration(this, file.getAbsolutePath());
    }
    
    public static LegacySocketHubConfiguration fromFile(File file) {        
        LegacySocketHubConfiguration configuration = JAXBConfiguration.loadConfiguration(LegacySocketHubConfiguration.class, file.getAbsolutePath());
        return configuration;
    }

    public int getMessaging3TelemtryPort() {
        return messaging3TelemetryPort;
    }
    
    public List<LoggingBridgeConfiguration> getImportBridges() {
        return importBridges;
    }
    
    public List<LoggingBridgeConfiguration> getExportBridges() {
        return exportBridges;
    }
    
    public void setExportBridges(List<LoggingBridgeConfiguration> exportBridges) {
        this.exportBridges = exportBridges;
    }
    
    public void setImportBridges(List<LoggingBridgeConfiguration> importBridges) {
        this.importBridges = importBridges;
    }

    public int getRestfulListenerPort() {
        return restfulListenerPort;
    }

//    public TransactionMonitorConfiguration getStateEngineConfiguration() {
//        return stateEngineConfiguration;
//    }
//    
//    public void setStateEngineConfiguration(TransactionMonitorConfiguration stateEngineConfiguration) {
//        this.stateEngineConfiguration = stateEngineConfiguration;
//    }
    
    public boolean isOutputStats() {
        return outputStats;
    }
    
    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }
}
