package com.logginghub.logging;

import com.logginghub.utils.VLPorts;

@Deprecated
/**
 * 
 * @author James
 * @deprecated Use VLPorts instead.
 */
public class LoggingPorts {

    public static int getSocketHubDefaultPort() {
        return VLPorts.getSocketHubDefaultPort();
    }

    public static int getTelemetryHubDefaultPort() {
        return VLPorts.getTelemetryHubDefaultPort();
    }

    public static int getTelemetryMessaging3HubDefaultPort() {
        return VLPorts.getTelemetryMessaging3HubDefaultPort();
    }
}
