package com.logginghub.logging.telemetry;

public interface TelemetryHelper {
    void setSourceApplication(String actualValue);

    MachineTelemetryGenerator startMachineTelemetryGenerator();

    ProcessTelemetryGenerator startProcessTelemetryGenerator(String sourceApplication);

    void stop();
}
