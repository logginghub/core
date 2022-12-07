package com.logginghub.logging.telemetry;

/**
 * @author James
 * 
 */
public class SigarTelemetryHelper implements TelemetryHelper {

    private SigarMachineTelemetryGenerator machineTelemetryGenerator;
    private SigarProcessTelemetryGenerator processTelemetryGenerator;

    public SigarTelemetryHelper() {
        try {
            SigarHelper.loadLibrary();
        }catch(UnsatisfiedLinkError e) {
            // No sigar libraries available, we'll have to fallback to something else?
        }
    }

    public SigarMachineTelemetryGenerator startMachineTelemetryGenerator() {
        if (machineTelemetryGenerator != null) {
            machineTelemetryGenerator.stop();
        }

        machineTelemetryGenerator = new SigarMachineTelemetryGenerator();
        machineTelemetryGenerator.start();
        return machineTelemetryGenerator;
    }

    public SigarProcessTelemetryGenerator startProcessTelemetryGenerator(String processName) {
        if (processTelemetryGenerator != null) {
            processTelemetryGenerator.stop();
        }

        processTelemetryGenerator = new SigarProcessTelemetryGenerator(processName);
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
