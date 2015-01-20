package com.logginghub.logging.sample;

import java.net.InetSocketAddress;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.VLPorts;

public class ConsumingEvents {

    public static void main(String[] args) throws LoggingMessageSenderException, InterruptedException {

        // Giving the client a name helps if you have multiple clients in one jvm
        SocketClient client = new SocketClient("ExampleClient");

        // Add each hub you want to connect to as a connection point - takes hostnames or ip
        // addresses
        client.addConnectionPoint(new InetSocketAddress("localhost", VLPorts.getSocketHubDefaultPort()));

        // The manager deals with automatically reconnecting to the hub so you dont have to worry
        // about it
        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

        // Adding an event listener allows you to consume the log events from the global event
        // channel
        client.addLogEventListener(new LogEventListener() {
            public void onNewLogEvent(LogEvent event) {
                System.out.println("[Global]" + event.getMessage());
            }
        });

    }

}
