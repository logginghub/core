package com.logginghub.logging.frontend.model;

import com.logginghub.logging.messaging.SocketClientManager.State;

public class ConnectionStateChangedEvent {
    private State state;
    private String host;

    public ConnectionStateChangedEvent(State toState, String host) {
        this.state = toState;
        this.host = host;
    }
    
    public String getHost() {
        return host;
    }
    
    public State getState() {
        return state;
    }
}
