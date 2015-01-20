package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.modules.LoggingBridgeModule;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(LoggingBridgeModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class LoggingBridgeConfiguration {

    @XmlAttribute private boolean importEvents = true;
    @XmlAttribute private boolean exportEvents = false;

    // jshaw - DO NOT DEFAULT TO LOCALHOST! It creates a feedback loop in a single hub
    @XmlAttribute private String host;
    @XmlAttribute private int port = VLPorts.getSocketHubDefaultPort();
    @XmlAttribute private String eventDestinationRef;
    @XmlAttribute private String eventSourceRef;

    @XmlElement private List<FilterConfiguration> filter = new ArrayList<FilterConfiguration>();

    public LoggingBridgeConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public LoggingBridgeConfiguration() {}

    public boolean isExportEvents() {
        return exportEvents;
    }

    public boolean isImportEvents() {
        return importEvents;
    }

    public void setExportEvents(boolean exportEvents) {
        this.exportEvents = exportEvents;
    }

    public void setImportEvents(boolean importEvents) {
        this.importEvents = importEvents;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getEventDestinationRef() {
        return eventDestinationRef;
    }

    public void setEventDestinationRef(String eventDestinationRef) {
        this.eventDestinationRef = eventDestinationRef;
    }

    public void setEventSourceRef(String eventSourceRef) {
        this.eventSourceRef = eventSourceRef;
    }

    public String getEventSourceRef() {
        return eventSourceRef;
    }

    public List<FilterConfiguration> getFilters() {
        return filter;
    }
}
