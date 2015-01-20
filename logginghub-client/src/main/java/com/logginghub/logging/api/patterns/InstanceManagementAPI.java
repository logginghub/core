package com.logginghub.logging.api.patterns;

import com.logginghub.utils.Destination;

public interface InstanceManagementAPI {
    
    void sendPing();        
    
    void addPingListener(Destination<PingResponse> destination);
    
}
