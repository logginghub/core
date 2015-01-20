package com.logginghub.logging.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.logginghub.logging.transaction.configuration.TransitionConfiguration;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Is;
import com.logginghub.utils.TimeUtils;

public class StateNodeModel {

    private ValueStripper2 stripper;
    private List<StateNodeModel> transitions = new ArrayList<StateNodeModel>();
    private long timeout;
    
    @Override public String toString() {
        return "StateNodeModel [name=" + stripper.getPatternName() + ", pattern=" + stripper.getRawPattern() + ", timeout=" + timeout + "]";
    }

    public void buildFromConfiguration(TransitionConfiguration transition, Map<String, ValueStripper2> strippers) {

        String state = transition.getState();
        this.stripper = strippers.get(state);

        Is.notNull(stripper,
                   "You've got a state transition using state '{}', but haven't defined a <stateCapture name=\"{}\" /> element to go with it",
                   state,
                   state);

        
        List<TransitionConfiguration> transitions = transition.getTransitions();
        for (TransitionConfiguration transitionConfiguration : transitions) {
            StateNodeModel child = new StateNodeModel();
            child.buildFromConfiguration(transitionConfiguration, strippers);
            this.transitions.add(child);
        }
        
        timeout = TimeUtils.parseInterval(transition.getTimeout());
    }
    
    public ValueStripper2 getStripper() {
        return stripper;
    }
    
    public List<StateNodeModel> getTransitions() {
        return transitions;
    }
    
    public long getTimeout() {
        return timeout;
    }

    public boolean isEndState() {
        return transitions.size() == 0;         
    }

}
