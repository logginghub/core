package com.logginghub.logging.modules;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.logging.messages.StackTraceItem;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.logging.transaction.configuration.HubStackCaptureConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class HubStackCaptureModule implements Module<HubStackCaptureConfiguration> {

    private StackCaptureController controller;
    private ChannelMessagingService channelSubscriptions;
    private Destination<ChannelMessage> strobeRequestHandler;
    private HubStackCaptureConfiguration configuration;
    private SocketHubInterface socketHubInterface;
    private Destination<ChannelMessage> snapshotsHandler;

    public HubStackCaptureModule() {}

    public void configure(HubStackCaptureConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        // jshaw - massive hack due to the container thing being a shit idea. For some reason if you ask for the logging message sender, sometimes you get given one of the file loggers :/
        LoggingMessageSender loggingMessageSender = (LoggingMessageSender) discovery.findService(SocketHubInterface.class,
                configuration.getDestinationRef());

        this.controller = new StackCaptureController();

        controller.configure(loggingMessageSender,
                TimeUtils.parseInterval(configuration.getSnapshotBroadcastInterval()),
                TimeUtils.parseInterval(configuration.getSnapshotRequestInterval()),
                configuration.isRespondToRequests(),
                configuration.getEnvironment(),
                configuration.getHost(),
                configuration.getInstanceType(),
                configuration.getInstanceNumber(),
                0
                // TODO : get pid from somewhere, another service?
                            );

        channelSubscriptions = discovery.findService(ChannelMessagingService.class);
        socketHubInterface = discovery.findService(SocketHubInterface.class);
    }

    public void start() {
        stop();

        if (configuration.isOutputToLog()) {
            snapshotsHandler = new Destination<ChannelMessage>() {
                @Override public void send(ChannelMessage t) {
                    StackSnapshot snapshot = (StackSnapshot) t.getPayload();

                    StringUtils.StringUtilsBuilder message = new StringUtils.StringUtilsBuilder();
                    message.appendLine("Stack trace snapshot");

                    StackTrace[] traces = snapshot.getTraces();
                    for (StackTrace trace : traces) {

                        message.appendLine("[{}] {} - {}",
                                trace.getThreadID(),
                                trace.getThreadName(),
                                trace.getThreadState());
                        StackTraceItem[] items = trace.getItems();
                        for (StackTraceItem item : items) {
                            message.appendLine("   " + item.toString());
                        }
                        message.appendLine();
                    }

                    DefaultLogEvent event = LogEventBuilder.start()
                                                           .setPid(snapshot.getPid())
                                                           .setMessage(message.toString())
                                                           .setSourceApplication(snapshot.buildKey())
                                                           .setLevel(Logger.info)
                                                           .setChannel(configuration.getChannel())
                                                           .setLoggerName("StackCapture")
                                                           .toLogEvent();

                    socketHubInterface.send(event);
                }
            };

            channelSubscriptions.subscribe(Channels.stackSnapshots, snapshotsHandler);
        }


        strobeRequestHandler = new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                StackStrobeRequest request = (StackStrobeRequest) t.getPayload();
                controller.executeStrobe(request);
            }
        };

        channelSubscriptions.subscribe(Channels.stackStrobeRequests, strobeRequestHandler);

        controller.start();

    }

    public void stop() {
        channelSubscriptions.unsubscribe(Channels.stackStrobeRequests, strobeRequestHandler);

        if (snapshotsHandler != null) {
            channelSubscriptions.unsubscribe(Channels.stackSnapshots, snapshotsHandler);
        }

        controller.stop();

    }

}
