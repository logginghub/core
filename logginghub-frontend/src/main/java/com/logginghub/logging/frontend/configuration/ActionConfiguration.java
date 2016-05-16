package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration encapsulation for an action, which is taken against a selected event.
 */
@XmlAccessorType(XmlAccessType.FIELD) public class ActionConfiguration {
    @XmlElement List<ArgumentConfiguration> argument = new ArrayList<ArgumentConfiguration>();
    @XmlAttribute private String name;
    @XmlAttribute private String path;
    @XmlAttribute private String command;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ArgumentConfiguration> getArguments() {
        return argument;
    }

    public void setArguments(List<ArgumentConfiguration> arguments) {
        this.argument = arguments;
    }
}
