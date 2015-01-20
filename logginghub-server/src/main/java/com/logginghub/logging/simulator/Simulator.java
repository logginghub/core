package com.logginghub.logging.simulator;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.SimulatorConfiguration;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.logging.transaction.HubConnector;
import com.logginghub.logging.transaction.configuration.HubConnectorConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class Simulator {

    public static void main(String[] args) {
        com.logginghub.logging.generator.nextgen.Simulator generator = new com.logginghub.logging.generator.nextgen.Simulator();
        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        HubConnector connector = new HubConnector();
        HubConnectorConfiguration configuration = new HubConnectorConfiguration();
        configuration.getHubs().add(new HubConfiguration("localhost", 58770));
        connector.configure(configuration, discovery);
        discovery.bind(Destination.class, LogEvent.class, connector);
        generator.configure(new SimulatorConfiguration(), discovery);
        generator.start();
    }

}
