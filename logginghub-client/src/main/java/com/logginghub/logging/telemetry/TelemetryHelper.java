package com.logginghub.logging.telemetry;

/**
 * @author James
 * 
 */
public class TelemetryHelper {

    private MachineTelemetryGenerator machineTelemetryGenerator;
    private ProcessTelemetryGenerator processTelemetryGenerator;

    public TelemetryHelper() {
        SigarHelper.loadLibrary();
    }

    public MachineTelemetryGenerator startMachineTelemetryGenerator() {
        if (machineTelemetryGenerator != null) {
            machineTelemetryGenerator.stop();
        }

        machineTelemetryGenerator = new MachineTelemetryGenerator();
        machineTelemetryGenerator.start();
        return machineTelemetryGenerator;
    }

    public ProcessTelemetryGenerator startProcessTelemetryGenerator(String processName) {
        if (processTelemetryGenerator != null) {
            processTelemetryGenerator.stop();
        }

        processTelemetryGenerator = new ProcessTelemetryGenerator(processName);
        processTelemetryGenerator.start();
        return processTelemetryGenerator;
    }

    public void setSourceApplication(String sourceApplication) {
        if (processTelemetryGenerator != null) {
            processTelemetryGenerator.setProcessName(sourceApplication);
        }
    }

    public void stop() {
        if (machineTelemetryGenerator != null) {
            machineTelemetryGenerator.stop();
            machineTelemetryGenerator = null;
        }

        if (processTelemetryGenerator != null) {
            processTelemetryGenerator.stop();
            processTelemetryGenerator = null;
        }

    }

}
