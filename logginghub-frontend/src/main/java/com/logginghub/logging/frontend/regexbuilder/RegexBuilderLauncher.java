package com.logginghub.logging.frontend.regexbuilder;

import java.net.InetSocketAddress;
import java.util.List;

import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.swing.MainFrame;

public class RegexBuilderLauncher {
    public static void main(String[] args) {
        
        String configurationPath = "logging.frontend.xml";
        String environment = "local";
        
        if(args.length > 0){
            configurationPath = args[0];            
        }
        
        if(args.length > 1){
            environment = args[1];
        }
        
        LoggingFrontendConfiguration configuration = LoggingFrontendConfiguration.loadConfiguration(configurationPath);
        
        MainFrame frame = new MainFrame("RegexBuilder");
        RegexBuilderPanel regexBuilderPanel = new RegexBuilderPanel();
        frame.setSize(0.5f);
        frame.getContentPane().add(regexBuilderPanel);
        frame.setVisible(true);


        List<EnvironmentConfiguration> environments = configuration.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            if(environmentConfiguration.getName().equals(environment)){
                List<HubConfiguration> hubs = environmentConfiguration.getHubs();
                for (HubConfiguration hubConfiguration : hubs) {
                    SocketClient client = new SocketClient();
                    client.addConnectionPoint(new InetSocketAddress(hubConfiguration.getHost(), hubConfiguration.getPort()));
                    client.addLogEventListener(regexBuilderPanel);
                    client.setAutoSubscribe(true);
                    
                    SocketClientManager manager = new SocketClientManager(client);
                    manager.start();
                }
            }
        }

    }
}
