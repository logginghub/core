package com.logginghub.logging;

import com.logginghub.logging.telemetry.MachineTelemetryGenerator;
import com.logginghub.logging.telemetry.ProcessTelemetryGenerator;
import com.logginghub.logging.telemetry.TelemetryHelper;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Source;
import com.logginghub.utils.data.DataStructure;

/**
 * Telemetry helper that does nothing to allow us to turn off sigar completely.
 */
public class NoopTelemetryHelper implements TelemetryHelper {
    @Override
    public void setSourceApplication(String actualValue) {

    }

    @Override
    public MachineTelemetryGenerator startMachineTelemetryGenerator() {
        return new MachineTelemetryGenerator() {
            @Override
            public Source<DataStructure> getDataStructureMultiplexer() {
                return new Multiplexer<DataStructure>();
            }
        };
    }

    @Override
    public ProcessTelemetryGenerator startProcessTelemetryGenerator(String sourceApplication) {
        return new ProcessTelemetryGenerator() {
            @Override
            public Source<DataStructure> getDataStructureMultiplexer() {
                return new Multiplexer<DataStructure>();
            }
        };
    }

    @Override
    public void stop() {

    }
}
