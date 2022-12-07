package com.logginghub.logging.telemetry;

import com.logginghub.utils.Source;
import com.logginghub.utils.data.DataStructure;

public interface MachineTelemetryGenerator {
    Source<DataStructure> getDataStructureMultiplexer();
}
