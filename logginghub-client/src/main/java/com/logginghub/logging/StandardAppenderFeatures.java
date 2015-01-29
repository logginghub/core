package com.logginghub.logging;

public interface StandardAppenderFeatures {

    void setFailureDelay(long failureDelay);
    void setFailureDelayMaximum(long failureDelayMaximum);
    void setFailureDelayMultiplier(double failureDelayMultiplier);
    void setWriteQueueOverflowPolicy(String policy);
    void setUseDispatchThread(boolean value);
    void setForceFlush(boolean b);
    void setPublishMachineTelemetry(boolean publishMachineTelemetry);
    void setPublishProcessTelemetry(boolean publishProcessTelemetry);
    void setMaximumQueuedMessages(int maximumQueuedMessages);
    void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp);
    void setSourceApplication(String sourceApplication);
    void setEnvironment(String environment);
    void setInstanceNumber(int instanceNumber);
    void setHost(String host);
    void setCpuLogging(boolean value);
    void setGCLogging(String path);
    void setHeapLogging(boolean value);

}
