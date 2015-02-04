package com.logginghub.logging.frontend.views.stack;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

public class SingleThreadViewModel extends Observable {

    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableProperty<String> state = createStringProperty("state", "");
    private ObservableProperty<String> stack = createStringProperty("stack", "");
    
    private ObservableProperty<String> environment = createStringProperty("environment", "");
    private ObservableProperty<String> host = createStringProperty("environment", "");
    private ObservableProperty<String> instanceType = createStringProperty("environment", "");
    private ObservableInteger instanceNumber = createIntProperty("instanceNumber", 0);

    public SingleThreadViewModel() {}
    
    public SingleThreadViewModel(String name) {
        this.name.set(name);
    }

    public ObservableProperty<String> getEnvironment() {
        return environment;
    }
    
    public ObservableProperty<String> getHost() {
        return host;
    }
    
    public ObservableInteger getInstanceNumber() {
        return instanceNumber;
    }
    
    public ObservableProperty<String> getInstanceType() {
        return instanceType;
    }
    
    public ObservableProperty<String> getName() {
        return name;
    }
    
    public ObservableProperty<String> getStack() {
        return stack;
    }
    
    public ObservableProperty<String> getState() {
        return state;
    }
    
    @Override public String toString() {
        return name.get();         
    }
}
