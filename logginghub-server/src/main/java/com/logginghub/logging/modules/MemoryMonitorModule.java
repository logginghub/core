package com.logginghub.logging.modules;

import com.logginghub.logging.modules.configuration.MemoryMonitorConfiguration;
import com.logginghub.utils.MemorySnapshot;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class MemoryMonitorModule implements Module<MemoryMonitorConfiguration> {

    private MemoryMonitorConfiguration configuration;
    private WorkerThread thread;

    @Override public void start() {
        thread = MemorySnapshot.runMonitorToLogging(configuration.getThreshold());
    }

    @Override public void stop() {
        if(thread != null) {
            thread.stop();
        }
    }

    @Override public void configure(MemoryMonitorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
    }

}
