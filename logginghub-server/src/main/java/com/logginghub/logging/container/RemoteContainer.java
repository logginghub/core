package com.logginghub.logging.container;

import java.net.InetSocketAddress;

import com.logginghub.logging.transaction.configuration.LoggingContainerConfiguration;
import com.logginghub.messaging.Level3AsyncServer;
import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.ServerConnectionListener;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

public class RemoteContainer implements Module<RemoteContainerConfiguration> {

    private RemoteContainerConfiguration configuration;
    
    private static final Logger logger = Logger.getLoggerFor(RemoteContainer.class);

    @Override public void configure(RemoteContainerConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
    }

    @Override public void start() {

        Level3AsyncServer server = new Level3AsyncServer(configuration.getPort(), "Is this a name?");
        server.bind().awaitForever();

        logger.info("Bound, waiting for connections...");
        server.addConnectionListener(new ServerConnectionListener() {
            
            @Override public void onNewConnection(ServerHandler serverHandler) {
                logger.info("New connection....");
            }
            
            @Override public void onDisconnection(ServerHandler serverHandler) {}
            
            @Override public void onBound(InetSocketAddress address) {}
            
            @Override public void onBindFailure(InetSocketAddress address, Exception e) {}
        });
        
        server.addMessageListener(new ServerMessageListener() {
            @Override public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
                if(message instanceof MessageWrapper) {
                    MessageWrapper messageWrapper = (MessageWrapper) message;
                    String instructions = messageWrapper.getPayload().toString();
                    startContainer(instructions);
                }
            }
        });

    }

    protected void startContainer(final String instructions) {
        logger.info("Starting container with instructions : {}", instructions);
        
        // TODO : encapsulate this container and thread so we can talk to it and kill it again later
        
        WorkerThread.execute("ContainerThread", new Runnable() {
            
            @Override public void run() {
                
                logger.info("Parsing configuration...");
                LoggingContainerConfiguration fromString = LoggingContainerConfiguration.fromString(instructions);
                LoggingContainer container = new LoggingContainer();
                
                ServiceDiscovery serviceDiscovery = new PeerServiceDiscovery(container);
                logger.info("Configuring container...");
                container.configure(fromString, serviceDiscovery);
                
                logger.info("Starting container...");
                container.start();
                
                ThreadUtils.sleep(TimeUtils.parseInterval("1 minute"));
                
                logger.info("Stopping container...");
                container.stop();
            }
        });
        
    }

    @Override public void stop() {}

    public static void main(String[] args) {
        RemoteContainerConfiguration configuration = new RemoteContainerConfiguration();
        RemoteContainer container = new RemoteContainer();
        container.configure(configuration, null);
        container.start();
    }

}
