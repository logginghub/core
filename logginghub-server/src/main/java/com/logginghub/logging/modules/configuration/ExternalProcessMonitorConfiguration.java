package com.logginghub.logging.modules.configuration;

import com.logginghub.logging.modules.ExternalProcessMonitorModule;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Configures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Configures(ExternalProcessMonitorModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class ExternalProcessMonitorConfiguration {

    @XmlAttribute private String name = "";
    @XmlAttribute private String command;
    @XmlAttribute private String prefix = "";
    @XmlAttribute private String postfix = "";
    @XmlAttribute private boolean continuous = true;
    @XmlAttribute private String interval = "30 seconds";
    @XmlAttribute private String simulationResource = null;
    @XmlAttribute private boolean simulating = false;    
    @XmlAttribute private String valueEnum = "Custom";
    @XmlAttribute private int valueCode = -1;
    @XmlAttribute private boolean logRawEvents = false;
    @XmlAttribute private boolean logRawEventErrors = false;
    @XmlAttribute private boolean sendTelemetryEvents = true;
    @XmlAttribute private int levelForRawEvents = Logger.info;
    @XmlAttribute private int levelForRawEventErrors = Logger.warning;
    @XmlAttribute private String channel;
    @XmlAttribute private String destination = null;

    public boolean isSendTelemetryEvents() {
        return sendTelemetryEvents;
    }
    
    public void setSendTelemetryEvents(boolean sendTelemetryEvents) {
        this.sendTelemetryEvents = sendTelemetryEvents;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public void setLevelForRawEvents(int levelForRawEvents) {
        this.levelForRawEvents = levelForRawEvents;
    }
    
    public void setLogRawEvents(boolean logRawEvents) {
        this.logRawEvents = logRawEvents;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setValueCode(int valueCode) {
        this.valueCode = valueCode;
    }
    
    public void setValueEnum(String valueEnum) {
        this.valueEnum = valueEnum;
    }
    
    public int getLevelForRawEvents() {
        return levelForRawEvents;
    }

    public int getLevelForRawEventErrors() {
        return levelForRawEventErrors;
    }

    public void setLevelForRawEventErrors(int levelForRawEventErrors) {
        this.levelForRawEventErrors = levelForRawEventErrors;
    }

    public void setLogRawEventErrors(boolean logRawEventErrors) {
        this.logRawEventErrors = logRawEventErrors;
    }

    public boolean isLogRawEvents() {
        return logRawEvents;
    }

    public boolean isLogRawEventErrors() {
        return logRawEventErrors;
    }

    public int getValueCode() {
        return valueCode;
    }
    
    public String getValueEnum() {
        return valueEnum;
    }
    
    public String getName() {
        return name;
    }

    public void setSimulationResource(String simulationResource) {
        this.simulationResource = simulationResource;
    }

    public String getSimulationResource() {
        return simulationResource;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return interval;
    }
    
    public boolean isSimulating() {
        return simulating;
    }
    
    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }

    @Override public String toString() {
        return "ExternalProcessConfiguration [name=" +
               name +
               ", command=" +
               command +
               ", prefix=" +
               prefix +
               ", postfix=" +
               postfix +
               ", continuous=" +
               continuous +
               ", interval=" +
               interval +
               ", simulationResource=" +
               simulationResource +
               ", simulating=" +
               simulating +
               ", valueEnum=" +
               valueEnum +
               ", valueCode=" +
               valueCode +
               "]";
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
