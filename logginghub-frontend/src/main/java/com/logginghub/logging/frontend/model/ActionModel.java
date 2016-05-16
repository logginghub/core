package com.logginghub.logging.frontend.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model encapsulation for the Action feature.
 */
public class ActionModel {

    private String path;
    private String name;
    private String command;

    private List<ArgumentModel> arguments = new CopyOnWriteArrayList<ArgumentModel>();

    public List<ArgumentModel> getArguments() {
        return arguments;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setArguments(List<ArgumentModel> arguments) {
        this.arguments = arguments;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public class ArgumentModel {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


}
