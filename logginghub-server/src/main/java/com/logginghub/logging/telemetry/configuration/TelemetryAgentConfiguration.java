package com.logginghub.logging.telemetry.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.logging.modules.configuration.ExternalFileMonitorConfiguration;
import com.logginghub.logging.modules.configuration.ExternalProcessMonitorConfiguration;
import com.logginghub.logging.modules.configuration.IOStatMonitorConfiguration;
import com.logginghub.logging.modules.configuration.SigarMachineTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.SigarProcessTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.VMStatMonitorConfiguration;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.VLPorts;

@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD) public class TelemetryAgentConfiguration {

    @XmlAttribute private String processName = "TelemetryAgent";
    @XmlAttribute private String host = "localhost";
    @XmlAttribute private int port = VLPorts.getTelemetryMessaging3HubDefaultPort();
    @XmlAttribute private boolean enableVMStat = true;
    @XmlAttribute private boolean enableIOStat = true;
    @XmlAttribute private boolean enableNetstat = true;
    @XmlAttribute private boolean enableNetstatStatistics = true;
    @XmlAttribute private boolean enableTop = true;

    @XmlElement VMStatMonitorConfiguration vmstatConfiguration;
    @XmlElement IOStatMonitorConfiguration iostatConfiguration;
    
    @XmlElement SigarProcessTelemetryConfiguration processTelemetryConfiguration;
    @XmlElement SigarMachineTelemetryConfiguration machineTelemetryConfiguration;
    
    @XmlElement private List<HubConfiguration> hub = new ArrayList<HubConfiguration>();
    
    @XmlElement private List<ExternalProcessMonitorConfiguration> externalProcess = new ArrayList<ExternalProcessMonitorConfiguration>();
    @XmlElement private List<ExternalFileMonitorConfiguration> externalFile = new ArrayList<ExternalFileMonitorConfiguration>();

    public List<HubConfiguration> getHubs() {
        return hub;
    }
    
    public SigarMachineTelemetryConfiguration getMachineTelemetryConfiguration() {
        return machineTelemetryConfiguration;
    }
    
    public SigarProcessTelemetryConfiguration getProcessTelemetryConfiguration() {
        return processTelemetryConfiguration;
    }
    
    public void setIoStatConfiguration(IOStatMonitorConfiguration ioStatConfiguration) {
        this.iostatConfiguration = ioStatConfiguration;
    }
    
    public void setVmStatConfiguration(VMStatMonitorConfiguration vmStatConfiguration) {
        this.vmstatConfiguration = vmStatConfiguration;
    }
    
    public IOStatMonitorConfiguration getIoStatConfiguration() {
        return iostatConfiguration;
    }
    
    public VMStatMonitorConfiguration getVmStatConfiguration() {
        return vmstatConfiguration;
    }

    public List<ExternalProcessMonitorConfiguration> getExternalProcesses() {
        return externalProcess;
    }
    
    public List<ExternalFileMonitorConfiguration> getExternalFiles() {
        return externalFile;
    }

    public boolean isEnableNetstatStatistics() {
        return enableNetstatStatistics;
    }

    public void setEnableNetstatStatistics(boolean enableNetstatStatistics) {
        this.enableNetstatStatistics = enableNetstatStatistics;
    }

    public boolean isEnableNetstat() {
        return enableNetstat;
    }

    public boolean isEnableTop() {
        return enableTop;
    }

    public void setEnableNetstat(boolean enableNetstat) {
        this.enableNetstat = enableNetstat;
    }

    public void setEnableTop(boolean enableTop) {
        this.enableTop = enableTop;
    }

    public boolean isEnableIOStat() {
        return enableIOStat;
    }

    public void setEnableIOStat(boolean enableIOStat) {
        this.enableIOStat = enableIOStat;
    }

    public boolean isEnableVMStat() {
        return enableVMStat;
    }

    public void setEnableVMStat(boolean enableVMStat) {
        this.enableVMStat = enableVMStat;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProcessName() {
        return processName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public static TelemetryAgentConfiguration load(String path) {
        return JAXBConfiguration.loadConfiguration(TelemetryAgentConfiguration.class, path);
    }

}
