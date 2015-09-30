package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.modules.configuration.WebFrontendConfiguration;
import com.logginghub.logging.modules.web.WebFrontendController;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Source;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.web.JettyLauncher;

public class WebFrontendModule implements Module<WebFrontendConfiguration> {

    private WebFrontendConfiguration configuration;
    private JettyLauncher jetty;
    private WebFrontendController controller = new WebFrontendController();

    private static final Logger logger = Logger.getLoggerFor(WebFrontendModule.class);

    @Override public void start() {
        stop();


        try {
            jetty = JettyLauncher.launchNonBlocking(controller, configuration.getPort());
        }
        catch (Exception e) {
            logger.warn(e, "Failed to start embedded jetty server - web front end not started.");
            // TODO : should we throw this? Its going to get the module starter into a right pickle if we do?
        }
    }

    @Override public void stop() {
        if (jetty != null) {
            jetty.stop();
            jetty = null;
        }
    }

    
    
    @Override public void configure(WebFrontendConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        controller.setServiceDiscovery(discovery);
        
        PatternManagerService patternManager = discovery.findService(PatternManagerService.class);
        controller.setPatternManager(patternManager);
        
        ChannelMessagingService channelMessaging = discovery.findService(ChannelMessagingService.class);
        controller.setChannelMessaging(channelMessaging);
        
        Source<LogEvent> fullEventStream = discovery.findService(Source.class, LogEvent.class);
        fullEventStream.addDestination(new Destination<LogEvent>() {
            @Override public void send(LogEvent t) {
                controller.broadcastEvent(t);
            }
        });
    }

}
