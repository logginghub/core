package com.logginghub.logging.frontend.modules;

import java.util.List;

import com.logginghub.logging.messaging.SocketClient;

public interface SocketClientDirectAccessService {
    /**
     * Break through the abstraction layer and access the SocketClient API directly
     */
    List<SocketClient> getDirectAccess();
}
