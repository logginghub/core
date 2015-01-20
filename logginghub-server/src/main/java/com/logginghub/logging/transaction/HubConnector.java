package com.logginghub.logging.transaction;

import java.net.InetSocketAddress;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.logging.transaction.configuration.HubConnectorConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.NamedModule;
import com.logginghub.utils.Source;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;

@Provides(LogEvent.class) public class HubConnector implements NamedModule<HubConnectorConfiguration>, Source<LogEvent>, Destination<LogEvent>, LoggingMessageSender {

    private static final Logger logger = Logger.getLoggerFor(HubConnector.class);

    private HubConnectorConfiguration configuration;
    private SocketClientManager socketClientManager;
    private SocketClient client;
    private ExceptionPolicy policy = new ExceptionPolicy(Policy.Ignore);

    @Override public void configure(HubConnectorConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        this.configuration = configuration;

        client = new SocketClient("hubConnector");

        List<HubConfiguration> hubs = configuration.getHubs();
        for (HubConfiguration hubConfiguration : hubs) {
            client.addConnectionPoint(new InetSocketAddress(hubConfiguration.getHost(), hubConfiguration.getPort()));
        }

        if (StringUtils.isNotNullOrEmpty(configuration.getHost())) {
            client.addConnectionPoint(new InetSocketAddress(configuration.getHost(), configuration.getPort()));
        }

        socketClientManager = new SocketClientManager(client);

    }

    @Override public void start() {
        socketClientManager.start();
        if (configuration.getChannels().length() > 0) {
            try {
                client.subscribe(configuration.getChannels().split(","));
            }
            catch (LoggingMessageSenderException e) {
                logger.warn(e, "Failed to subscribe to one or more channels : '{}'", configuration.getChannels());
            }
        }
    }

    @Override public void stop() {
        socketClientManager.stop();
    }

    @Override public String getName() {
        return configuration.getName();
    }

    @Override public void addDestination(Destination<LogEvent> listener) {
        // TODO : warning if there is no subsription setup!
        client.addDestination(listener);
    }

    @Override public void removeDestination(Destination<LogEvent> listener) {
        client.addDestination(listener);
    }

    @Override public void send(LogEvent t) {
        try {
            client.send(new LogEventMessage(t));
        }
        catch (LoggingMessageSenderException e) {
            policy.handle(e);
        }
    }

    @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
        client.send(message);
    }

}
