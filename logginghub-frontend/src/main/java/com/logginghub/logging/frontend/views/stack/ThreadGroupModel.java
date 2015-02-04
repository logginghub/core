package com.logginghub.logging.frontend.views.stack;

import java.util.regex.Pattern;

import com.logginghub.utils.WildcardMatcher;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;

public class ThreadGroupModel extends Observable {
    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableProperty<String> matcher = createStringProperty("matcher", "");
    private ObservableProperty<Boolean> regex = createBooleanProperty("regex", false);
    private ObservableProperty<Boolean> enabled = createBooleanProperty("enabled", false);

    public ThreadGroupModel() {}
    
    public ThreadGroupModel(String name) {
        this.name.set(name);
    }
    
    public ObservableProperty<Boolean> getEnabled() {
        return enabled;
    }
    
    public ObservableProperty<String> getName() {
        return name;
    }

    public ObservableProperty<String> getMatcher() {
        return matcher;
    }
    
    public ObservableProperty<Boolean> getRegex() {
        return regex;
    }

    public boolean passes(String threadName) {
        
        boolean passes;
        if(regex.get()) {
            // TODO : cache the compilation
            passes = Pattern.matches(matcher.get(), threadName);
        }else{
            passes = WildcardMatcher.matches(matcher.get(), threadName);
        }
        
        return passes;
         
    }
}
