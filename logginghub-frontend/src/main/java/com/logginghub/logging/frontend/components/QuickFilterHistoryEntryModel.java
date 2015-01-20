package com.logginghub.logging.frontend.components;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class QuickFilterHistoryEntryModel extends Observable{

    private ObservableProperty<String> command = new ObservableProperty<String>(null, this);
    private ObservableLong lastUsed = new ObservableLong(0L, this);
    private ObservableLong count = new ObservableLong(0L, this);
    
    private ObservableProperty<Boolean> userDefined = new ObservableProperty<Boolean>(false, this);
    
    public QuickFilterHistoryEntryModel() {}
    
    public QuickFilterHistoryEntryModel(String command) {
        this.command.set(command);
    }

    public QuickFilterHistoryEntryModel(String command, boolean userDefined) {
        this.command.set(command);
        this.userDefined.set(userDefined);
    }
    
    public ObservableProperty<String> getCommand() {
        return command;
    }
    
    public ObservableLong getCount() {
        return count;
    }
    
    public ObservableLong getLastUsed() {
        return lastUsed;
    }
    
    public ObservableProperty<Boolean> getUserDefined() {
        return userDefined;
    }
    
}


