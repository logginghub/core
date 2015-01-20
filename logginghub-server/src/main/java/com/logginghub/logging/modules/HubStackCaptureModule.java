package com.logginghub.logging.modules;

import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.modules.StackCaptureController;
import com.logginghub.logging.transaction.configuration.HubStackCaptureConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class HubStackCaptureModule implements Module<HubStackCaptureConfiguration> {

    private StackCaptureController controller;
    private ChannelMessagingService channelSubscriptions;

    public HubStackCaptureModule() {}

    public void configure(HubStackCaptureConfiguration configuration, ServiceDiscovery discovery) {

        LoggingMessageSender loggingMessageSender = discovery.findService(LoggingMessageSender.class, configuration.getDestinationRef());

        this.controller = new StackCaptureController();
        controller.configure(loggingMessageSender,
                             TimeUtils.parseInterval(configuration.getSnapshotInterval()),
                             configuration.getEnvironment(),
                             configuration.getHost(),
                             configuration.getInstanceType(),
                             configuration.getInstanceNumber());

        channelSubscriptions = discovery.findService(ChannelMessagingService.class);
    }

    public void start() {
        stop();

        channelSubscriptions.subscribe(Channels.strobeRequests, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                StackStrobeRequest request = (StackStrobeRequest) t.getPayload();
                controller.executeStrobe(request);
            }
        });

        controller.start();

    }

    public void stop() {
        controller.stop();

        // TODO : remove the subscription
    }

}
