package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.telemetry.configuration.HubConfiguration;



public interface EnvironmentNotificationListener {
    /**
     * We've established a connection to this particular hub
     * @param hubConfiguration 
     */
    void onHubConnectionEstablished(HubConfiguration hubConfiguration);
    
    /**
     * We've lost a connection to this particular hub
     * @param hubConfiguration 
     */
    void onHubConnectionLost(HubConfiguration hubConfiguration);
    
    /**
     * We've lost connection with all hubs associated with this environment
     */
    void onEnvironmentConnectionLost();
    
    /**
     * We've established a connection with at least one of the hubs associated with this environment
     */
    void onEnvironmentConnectionEstablished();
    
    /**
     * We've established a connection with all of the hubs associated with this environment
     */
    void onTotalEnvironmentConnectionEstablished();
    
    /**
     * We've lost a connection and we are no longer connected to all of the hubs associated with this environment
     */
    void onTotalEnvironmentConnectionLost();
}
