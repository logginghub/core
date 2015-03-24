package com.logginghub.logging.frontend.regexbuilder;

import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketClientManagerListener;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.swing.MainFrame;

import java.net.InetSocketAddress;
import java.util.List;

public class RegexBuilderLauncher {
    public static void main(String[] args) {

        String configurationPath = "logging.frontend.xml";
        String environment = "local";

        if (args.length > 0) {
            configurationPath = args[0];
        }

        if (args.length > 1) {
            environment = args[1];
        }

        LoggingFrontendConfiguration configuration = LoggingFrontendConfiguration.loadConfiguration(configurationPath);

        final MainFrame frame = new MainFrame("RegexBuilder");
        RegexBuilderPanel regexBuilderPanel = new RegexBuilderPanel();
        frame.setSize(0.5f);
        frame.getContentPane().add(regexBuilderPanel);
        frame.setVisible(true);


        List<EnvironmentConfiguration> environments = configuration.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            if (environments.size() == 1 || environmentConfiguration.getName().equals(environment)) {
                List<HubConfiguration> hubs = environmentConfiguration.getHubs();
                for (final HubConfiguration hubConfiguration : hubs) {
                    SocketClient client = new SocketClient();
                    InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress(hubConfiguration.getHost(), hubConfiguration.getPort());
                    client.addConnectionPoint(inetSocketAddress);
                    client.addLogEventListener(regexBuilderPanel);
                    client.setAutoSubscribe(true);

                    SocketClientManager manager = new SocketClientManager(client);
                    manager.addSocketClientManagerListener(new SocketClientManagerListener() {
                        @Override
                        public void onStateChanged(SocketClientManager.State fromState, SocketClientManager.State toState) {
                            Out.out("Connection to '{}:{}' : from state '{}' to state '{}'",
                                    hubConfiguration.getHost(),
                                    hubConfiguration.getPort(),
                                    fromState,
                                    toState);
                        }
                    });
                    manager.start();
                }
            }
        }

    }
}
