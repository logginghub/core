package com.logginghub.logging.hub.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(SocketHub.class)
@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement(name ="hubConfiguration")  public class SocketHubConfiguration {

    @XmlAttribute  private int port = VLPorts.getSocketHubDefaultPort();
//    @XmlAttribute  private int telemetryPort = 0;
//    @XmlAttribute  private int messaging3TelemetryPort = 0;
    @XmlAttribute  private int restfulListenerPort = 0;
    @XmlAttribute  private int maximumClientSendQueueSize = SocketConnection.writeBufferDefaultSize;
//    @XmlAttribute private boolean outputTelemetryToLoggingStream = false;
    @XmlAttribute private boolean outputStats = true;
    @XmlAttribute private String statsInterval = "1 minute";
    
    @XmlElement List<FilterConfiguration> filter = new ArrayList<FilterConfiguration>();
    
    public List<FilterConfiguration> getFilters() {
        return filter;
    }
    
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
    
    public void setPort(int port) {
        this.port = port;
    }

    public void writeToFile(File file) {        
        FileUtils.ensurePathExists(file);
        JAXBConfiguration.writeConfiguration(this, file.getAbsolutePath());
    }
    
    public static SocketHubConfiguration fromFile(File file) {        
        SocketHubConfiguration configuration = JAXBConfiguration.loadConfiguration(SocketHubConfiguration.class, file.getAbsolutePath());
        return configuration;
    }

//    public int getMessaging3TelemtryPort() {
//        return messaging3TelemetryPort;
//    }
    
//    public int getTelemetryPort() {
//        return telemetryPort;
//    }
//    
//    public void setTelemetryPort(int telemetryPort) {
//        this.telemetryPort = telemetryPort;
//    }

    public int getRestfulListenerPort() {
        return restfulListenerPort;
    }

    public boolean isOutputStats() {
        return outputStats;
    }
    
    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }
    
    public String getStatsInterval() {
        return statsInterval;
    }
    
    public void setStatsInterval(String statsInterval) {
        this.statsInterval = statsInterval;
    }
}
