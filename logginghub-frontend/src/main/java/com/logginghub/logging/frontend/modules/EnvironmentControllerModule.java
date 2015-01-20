package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.frontend.modules.configuration.EnvironmentControllerConfiguration;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class EnvironmentControllerModule implements Module<EnvironmentControllerConfiguration> {

    @Override public void configure(EnvironmentControllerConfiguration configuration, ServiceDiscovery discovery) {}

    @Override public void start() {}

    @Override public void stop() {}

}
