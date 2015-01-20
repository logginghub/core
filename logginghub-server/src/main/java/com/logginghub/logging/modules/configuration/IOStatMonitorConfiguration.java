package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.modules.IOStatMonitorModule;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Configures;

@Configures(IOStatMonitorModule.class)
@XmlAccessorType(XmlAccessType.FIELD) public class IOStatMonitorConfiguration {
    @XmlAttribute private String command = "iostat -d -k -x 1";
    @XmlAttribute private String simulationResource = "samples/iostat.txt";
    @XmlAttribute private boolean simulating = false;
    @XmlAttribute private boolean logRawEvents = false;
    @XmlAttribute private String prefix = "iostat - ";
    @XmlAttribute private int levelForRawEvents = Logger.info;
    @XmlAttribute private String channel;
    @XmlAttribute private String destination;
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public String getChannel() {
        return channel;
    }

    public boolean isLogRawEvents() {
        return logRawEvents;
    }

    public void setLogRawEvents(boolean logRawEvents) {
        this.logRawEvents = logRawEvents;
    }

    public void setLevelForRawEvents(int levelForRawEvents) {
        this.levelForRawEvents = levelForRawEvents;
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
        return "IOStatConfiguration [command=" +
               command +
               ", simulationResource=" +
               simulationResource +
               ", simulating=" +
               simulating +
               ", logRawEvents=" +
               logRawEvents +
               ", levelForRawEvents=" +
               levelForRawEvents +
               "]";
    }

    public int getLevelForRawEvents() {
        return levelForRawEvents;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDestination() {
        return destination;
         
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }

}
