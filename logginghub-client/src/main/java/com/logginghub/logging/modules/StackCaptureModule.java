package com.logginghub.logging.modules;

import java.net.InetSocketAddress;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class StackCaptureModule implements Module<StackCaptureConfiguration> {

    private LoggingMessageSender loggingMessageSender;

    private StackCaptureController controller;

    private ChannelMessagingService channelSubscriptions;

    // TODO : allow this to be configurable via the Container
    public StackCaptureModule(LoggingMessageSender loggingMessageSender, ChannelMessagingService channelSubscriptions) {
        this.loggingMessageSender = loggingMessageSender;
        this.channelSubscriptions = channelSubscriptions;
        this.controller = new StackCaptureController();
    }

    public void setLoggingMessageSender(LoggingMessageSender loggingMessageSender) {
        this.loggingMessageSender = loggingMessageSender;
    }

    public void setChannelSubscriptions(ChannelMessagingService channelSubscriptions) {
        this.channelSubscriptions = channelSubscriptions;
    }

    public void configure(StackCaptureConfiguration configuration, ServiceDiscovery discovery) {
        this.controller.configure(loggingMessageSender,
                                  TimeUtils.parseInterval(configuration.getSnapshotInterval()),
                                  configuration.getEnvironment(),
                                  configuration.getHost(),
                                  configuration.getInstanceType(),
                                  configuration.getInstanceNumber());
    }

    public void start() {
        stop();

        channelSubscriptions.subscribe(Channels.strobeRequests, new Destination<ChannelMessage>() {
            public void send(ChannelMessage t) {
                StackStrobeRequest request = (StackStrobeRequest) t.getPayload();
                controller.executeStrobe(request);
            }
        });

        controller.start();
    }

    public void stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient("StackCaptureClient");
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));
        client.setAutoGlobalSubscription(false);
        client.setAutoSubscribe(false);
        client.connect();

        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

        StackCaptureModule capture = new StackCaptureModule(client, client);
        StackCaptureConfiguration config = new StackCaptureConfiguration();
        config.setSnapshotInterval("0");
        config.setEnvironment("local");
        config.setInstanceType("StackCaptureModule");
        config.setInstanceNumber(1);
        capture.configure(config, null);
        capture.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
