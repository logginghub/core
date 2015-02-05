package com.logginghub.logging.modules;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.net.InetSocketAddress;

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
                TimeUtils.parseInterval(configuration.getRequestInterval()),
                configuration.isRespondToRequests(),
                configuration.getInstanceKey());
    }

    public void start() {
        stop();

        channelSubscriptions.subscribe(Channels.stackStrobeRequests, new Destination<ChannelMessage>() {
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
        config.setInstanceKey(new InstanceKey("local", "localhost", "123.123.123.123", 1, "StackCapturedModule", null));
        config.setSnapshotInterval("0");
        capture.configure(config, null);
        capture.start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
