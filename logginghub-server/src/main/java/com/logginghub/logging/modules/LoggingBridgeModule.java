package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.FilterConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messages.ConnectionTypeMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messaging.*;
import com.logginghub.logging.modules.configuration.LoggingBridgeConfiguration;
import com.logginghub.logging.servers.FilterHelper;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.Is;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoggingBridgeModule implements Module<LoggingBridgeConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(LoggingBridgeModule.class);

    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.Ignore);

    private SocketClientManager socketClientManager;

    private LoggingBridgeConfiguration configuration;

    //    private Stream<LogEvent> importEventStream = new Stream<LogEvent>();

    private FilterHelper excludeFilter = new FilterHelper();

    private SocketClient client;

    private SocketHubInterface socketHubInterface;

    //    private SocketConnection source = new SocketConnection();

    private Throttler throttler = new Throttler(10, TimeUnit.SECONDS);

    private InternalConnection internal = new InternalConnection("LoggingBridgeModule", SocketConnection.CONNECTION_TYPE_HUB_BRIDGE);


    public LoggingBridgeModule() {
    }

    public void stop() {
        if (socketClientManager != null) {
            socketClientManager.stop();
        }

        if (client != null) {
            client.close();
        }
    }

    public ExceptionPolicy getExceptionPolicy() {
        return exceptionPolicy;
    }

    @Override
    public void start() {
        client = new SocketClient("LoggingBridgeModule");
        final InetSocketAddress connectionPoint = new InetSocketAddress(configuration.getHost(),
                                                                        configuration.getPort());
        client.addConnectionPoint(connectionPoint);

        if (configuration.isImportEvents()) {

            client.setAutoGlobalSubscription(false);
            client.setAutoSubscribe(false);

            // Setup the client to send the connection type message _before_ subscribing to events
            client.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
                @Override
                public void onConnectionEstablished() {
                    try {
                        client.sendBlocking(new ConnectionTypeMessage("client connection",
                                                                      SocketConnection.CONNECTION_TYPE_HUB_BRIDGE));
                        client.sendBlocking(new SubscriptionRequestMessage());
                    } catch (LoggingMessageSenderException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionLost(String reason) {

                }
            });

            client.addLogEventListener(new LogEventListener() {
                @Override
                public void onNewLogEvent(LogEvent t) {
                    // We've received an event from an import bridge - send it to the hub
                    if (!excludeFilter.passes(t)) {
                        logger.fine("Importing event : '{}'", t);
                        socketHubInterface.processLogEvent(new LogEventMessage(t), internal);
                    } else {
                        logger.fine("Not importing event (failed filter) : '{}'", t);
                    }
                }
            });
        }

        socketClientManager = new SocketClientManager(client);
        socketClientManager.setExceptionHandler(new ExceptionHandler() {
            @Override
            public void handleException(String action, Throwable t) {
                t.printStackTrace();
            }
        });
        socketClientManager.start();

    }

    @Override
    public void configure(LoggingBridgeConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        Is.notNullOrEmpty(configuration.getHost(), "The 'host' attribute in the <bridge /> element must be set");

        List<FilterConfiguration> filters = configuration.getFilters();
        for (FilterConfiguration filterConfiguration : filters) {
            excludeFilter.addFilter(filterConfiguration);
        }

        final ThreadLocal<LogEvent> loopProtection = new ThreadLocal<LogEvent>();

        socketHubInterface = discovery.findService(SocketHubInterface.class, configuration.getEventDestinationRef());

        //        if (configuration.isImportEvents()) {


        //            @SuppressWarnings("unchecked") final Destination<LogEvent> destination = discovery.findService(Destination.class,
        //                                                                                                           LogEvent.class,
        //                                                                                                           configuration
        //                                                                                                                   .getEventDestinationRef());

        //            importEventStream.addDestination(new Destination<LogEvent>() {
        //                @Override
        //                public void send(LogEvent t) {
        //                    if (!excludeFilter.passes(t)) {
        //                        // Flag this event so if we are importing and exporting we wont send the
        //                        // same one out
        //                        loopProtection.set(t);
        //
        //                        logger.fine("Importing event : '{}'", t);
        //                        destination.send(t);
        //                    } else {
        //                        logger.fine("Not importing event (failed filter) : '{}'", t);
        //                    }
        //                }
        //            });
        //        }

        if (configuration.isExportEvents()) {

            socketHubInterface.addAndSubscribeLocalListener(new FilteredMessageSender() {
                @Override
                public int getLevelFilter() {
                    return Logger.all;
                }

                @Override
                public int getConnectionType() {
                    return SocketConnection.CONNECTION_TYPE_NORMAL;
                }

                @Override
                public void send(LogEvent logEvent) {
                    try {
                        client.send(new LogEventMessage(logEvent));
                    } catch (LoggingMessageSenderException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void send(LoggingMessage message) throws LoggingMessageSenderException {
                    client.send(message);
                }
            });

            //            @SuppressWarnings("unchecked") Source<LogEvent> source = discovery.findService(Source.class,
            //                                                                                           LogEvent.class,
            //                                                                                           configuration.getEventSourceRef());

            //            source.addDestination(new Destination<LogEvent>() {
            //                @Override
            //                public void send(LogEvent t) {
            //                    if (loopProtection.get() == t) {
            //                        // Ignore this, we brought the event in so the last thing we want to do is
            //                        // send it back the other way!
            //                    } else {
            //                        if (!excludeFilter.passes(t)) {
            //                            try {
            //                                logger.fine("Exporting event : '{}'", t);
            //                                client.send(new LogEventMessage(t));
            //                            } catch (LoggingMessageSenderException e) {
            //                                exceptionPolicy.handle(e);
            //                            }
            //                        } else {
            //                            logger.fine("Not exporting event (failed filter) : '{}'", t);
            //                        }
            //                    }
            //                }
            //
            //            });
        }
    }


}
