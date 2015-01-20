package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.VMStatMonitorModule;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Configures;

@Configures(VMStatMonitorModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class VMStatMonitorConfiguration {
    @XmlAttribute private String command = "vmstat -n 1";
    @XmlAttribute private String simulationResource = "samples/vmstat.txt";
    @XmlAttribute private boolean simulating = false;    
    @XmlAttribute private boolean logRawEvents = false;
    @XmlAttribute private String prefix = "vmstat - ";
    @XmlAttribute private int levelForRawEvents = Logger.info;
    @XmlAttribute private String channel;
    @XmlAttribute private String destination;
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public boolean isLogRawEvents() {
        return logRawEvents;
    }

    public void setLogRawEvents(boolean logRawEvents) {
        this.logRawEvents = logRawEvents;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getLevelForRawEvents() {
        return levelForRawEvents;
    }

    public void setLevelForRawEvents(int levelForRawEvents) {
        this.levelForRawEvents = levelForRawEvents;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isSimulating() {
        return simulating;
    }
    
    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSimulationResource() {
        return simulationResource;
    }

    public void setSimulationResource(String simulationResource) {
        this.simulationResource = simulationResource; 
    }

    @Override public String toString() {
        return "VMStatConfiguration [command=" + command + ", simulationResource=" + simulationResource + ", simulating=" + simulating + "]";
    }
    
    

}
