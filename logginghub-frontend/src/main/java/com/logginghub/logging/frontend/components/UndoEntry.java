package com.logginghub.logging.frontend.components;

public class UndoEntry {

    private String commandValue;
    
    public UndoEntry(String commandValue) {
        this.commandValue = commandValue;        
    }
    
    public String getCommandValue() {
        return commandValue;
    }
    
}
